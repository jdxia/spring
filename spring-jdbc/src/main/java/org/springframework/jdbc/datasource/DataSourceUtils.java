/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.lang.Nullable;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * Helper class that provides static methods for obtaining JDBC Connections from
 * a {@link javax.sql.DataSource}. Includes special support for Spring-managed
 * transactional Connections, e.g. managed by {@link DataSourceTransactionManager}
 * or {@link org.springframework.transaction.jta.JtaTransactionManager}.
 *
 * <p>Used internally by Spring's {@link org.springframework.jdbc.core.JdbcTemplate},
 * Spring's JDBC operation objects and the JDBC {@link DataSourceTransactionManager}.
 * Can also be used directly in application code.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getConnection
 * @see #releaseConnection
 * @see DataSourceTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * @see org.springframework.transaction.support.TransactionSynchronizationManager
 */
public abstract class DataSourceUtils {

	/**
	 * Order value for TransactionSynchronization objects that clean up JDBC Connections.
	 */
	public static final int CONNECTION_SYNCHRONIZATION_ORDER = 1000;

	private static final Log logger = LogFactory.getLog(DataSourceUtils.class);


	/**
	 * Obtain a Connection from the given DataSource. Translates SQLExceptions into
	 * the Spring hierarchy of unchecked generic data access exceptions, simplifying
	 * calling code and making any exception that is thrown more meaningful.
	 * <p>Is aware of a corresponding Connection bound to the current thread, for example
	 * when using {@link DataSourceTransactionManager}. Will bind a Connection to the
	 * thread if transaction synchronization is active, e.g. when running within a
	 * {@link org.springframework.transaction.jta.JtaTransactionManager JTA} transaction).
	 * @param dataSource the DataSource to obtain Connections from
	 * @return a JDBC Connection from the given DataSource
	 * @throws org.springframework.jdbc.CannotGetJdbcConnectionException
	 * if the attempt to get a Connection failed
	 * @see #releaseConnection
	 */
	public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
		// mybatis获取链接会从这里获取

		try {
			// 核心
			return doGetConnection(dataSource);
		}
		catch (SQLException ex) {
			throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
		}
		catch (IllegalStateException ex) {
			throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection: " + ex.getMessage());
		}
	}

	/**
	 * Actually obtain a JDBC Connection from the given DataSource.
	 * Same as {@link #getConnection}, but throwing the original SQLException.
	 * <p>Is aware of a corresponding Connection bound to the current thread, for example
	 * when using {@link DataSourceTransactionManager}. Will bind a Connection to the thread
	 * if transaction synchronization is active (e.g. if in a JTA transaction).
	 * <p>Directly accessed by {@link TransactionAwareDataSourceProxy}.
	 * @param dataSource the DataSource to obtain Connections from
	 * @return a JDBC Connection from the given DataSource
	 * @throws SQLException if thrown by JDBC methods
	 * @see #doReleaseConnection
	 */
	public static Connection doGetConnection(DataSource dataSource) throws SQLException {
		Assert.notNull(dataSource, "No DataSource specified");

		// 获取当前线程的数据库连接持有者。这里是事务中的连接时同一个db连接
		ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
		// 如果存在持有者 && (存在连接 || 和事务同步状态)
		if (conHolder != null && (conHolder.hasConnection() || conHolder.isSynchronizedWithTransaction())) {
			// 标记引用次数加一
			conHolder.requested();
			// 如果当前线程存在持有者，并且与事务同步了，如果仍然没有DB连接，那么说明当前线程就是不存在数据库连接，则获取连接绑定到持有者上。
			if (!conHolder.hasConnection()) {
				logger.debug("Fetching resumed JDBC Connection from DataSource");
				// 持有者不存在连接则获取连接
				conHolder.setConnection(fetchConnection(dataSource));
			}
			// 返回持有者所持有的连接
			return conHolder.getConnection();
		}
		// Else we either got no holder or an empty thread-bound holder here.

		// 到这里没返回，则说明 没有持有者 || 持有者没有同步绑定
		logger.debug("Fetching JDBC Connection from DataSource");
		// 获取到 DB 连接
		Connection con = fetchConnection(dataSource);

		// 如果当前线程的事务同步处于活动状态
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			try {
				// Use same Connection for further JDBC actions within the transaction.
				// Thread-bound object will get removed by synchronization at transaction completion.
				// 如果持有者为null则创建一个，否则将刚才创建的DB连接赋值给持有者
				ConnectionHolder holderToUse = conHolder;
				if (holderToUse == null) {
					holderToUse = new ConnectionHolder(con);
				}
				else {
					holderToUse.setConnection(con);
				}
				/**
				 * 记录数据库连接： 引用次数加1
				 *
				 * 由于一个事务中存在多个sql 执行，每个sql 执行前都会获取一次DB 连接，所以这里使用 holderToUse.requested();
				 * 来记录当前事务中的数据库连接引用的次数。执行完毕后将会将引用次数减一。在最后的sql 执行结束后会将引用次数减一。
				 */
				holderToUse.requested();
				// 设置事务和持有者同步
				TransactionSynchronizationManager.registerSynchronization(
						new ConnectionSynchronization(holderToUse, dataSource));
				holderToUse.setSynchronizedWithTransaction(true);
				if (holderToUse != conHolder) {
					TransactionSynchronizationManager.bindResource(dataSource, holderToUse);
				}
			}
			catch (RuntimeException ex) {
				// Unexpected exception from external delegation call -> close Connection and rethrow.
				releaseConnection(con, dataSource);
				throw ex;
			}
		}

		return con;
	}

	/**
	 * Actually fetch a {@link Connection} from the given {@link DataSource},
	 * defensively turning an unexpected {@code null} return value from
	 * {@link DataSource#getConnection()} into an {@link IllegalStateException}.
	 * @param dataSource the DataSource to obtain Connections from
	 * @return a JDBC Connection from the given DataSource (never {@code null})
	 * @throws SQLException if thrown by JDBC methods
	 * @throws IllegalStateException if the DataSource returned a null value
	 * @see DataSource#getConnection()
	 */
	private static Connection fetchConnection(DataSource dataSource) throws SQLException {
		Connection con = dataSource.getConnection();
		if (con == null) {
			throw new IllegalStateException("DataSource returned null from getConnection(): " + dataSource);
		}
		return con;
	}

	/**
	 * Prepare the given Connection with the given transaction semantics.
	 * @param con the Connection to prepare
	 * @param definition the transaction definition to apply
	 * @return the previous isolation level, if any
	 * @throws SQLException if thrown by JDBC methods
	 * @see #resetConnectionAfterTransaction
	 * @see Connection#setTransactionIsolation
	 * @see Connection#setReadOnly
	 */
	@Nullable  	// 把definition和connection进行一些准备工作~
	public static Integer prepareConnectionForTransaction(Connection con, @Nullable TransactionDefinition definition)
			throws SQLException {

		Assert.notNull(con, "No Connection specified");

		boolean debugEnabled = logger.isDebugEnabled();
		// Set read-only flag.
		// 设置数据量连接为 read-only
		if (definition != null && definition.isReadOnly()) {
			try {
				if (debugEnabled) {
					logger.debug("Setting JDBC Connection [" + con + "] read-only");
				}
				con.setReadOnly(true);
			}
			catch (SQLException | RuntimeException ex) {
				Throwable exToCheck = ex;
				while (exToCheck != null) {
					if (exToCheck.getClass().getSimpleName().contains("Timeout")) {
						// Assume it's a connection timeout that would otherwise get lost: e.g. from JDBC 4.0
						throw ex;
					}
					exToCheck = exToCheck.getCause();
				}
				// "read-only not supported" SQLException -> ignore, it's just a hint anyway
				logger.debug("Could not set JDBC Connection read-only", ex);
			}
		}

		// Apply specific isolation level, if any.
		// 设置数据库的隔离级别
		Integer previousIsolationLevel = null;
		if (definition != null && definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
			if (debugEnabled) {
				logger.debug("Changing isolation level of JDBC Connection [" + con + "] to " +
						definition.getIsolationLevel());
			}
			int currentIsolation = con.getTransactionIsolation();
			if (currentIsolation != definition.getIsolationLevel()) {
				previousIsolationLevel = currentIsolation;
				con.setTransactionIsolation(definition.getIsolationLevel());
			}
		}

		return previousIsolationLevel;
	}

	/**
	 * Reset the given Connection after a transaction,
	 * regarding read-only flag and isolation level.
	 * @param con the Connection to reset
	 * @param previousIsolationLevel the isolation level to restore, if any
	 * @param resetReadOnly whether to reset the connection's read-only flag
	 * @since 5.2.1
	 * @see #prepareConnectionForTransaction
	 * @see Connection#setTransactionIsolation
	 * @see Connection#setReadOnly
	 */
	public static void resetConnectionAfterTransaction(
			Connection con, @Nullable Integer previousIsolationLevel, boolean resetReadOnly) {

		Assert.notNull(con, "No Connection specified");
		boolean debugEnabled = logger.isDebugEnabled();
		try {
			// Reset transaction isolation to previous value, if changed for the transaction.
			if (previousIsolationLevel != null) {
				if (debugEnabled) {
					logger.debug("Resetting isolation level of JDBC Connection [" +
							con + "] to " + previousIsolationLevel);
				}
				con.setTransactionIsolation(previousIsolationLevel);
			}

			// Reset read-only flag if we originally switched it to true on transaction begin.
			if (resetReadOnly) {
				if (debugEnabled) {
					logger.debug("Resetting read-only flag of JDBC Connection [" + con + "]");
				}
				con.setReadOnly(false);
			}
		}
		catch (Throwable ex) {
			logger.debug("Could not reset JDBC Connection after transaction", ex);
		}
	}

	/**
	 * Reset the given Connection after a transaction,
	 * regarding read-only flag and isolation level.
	 * @param con the Connection to reset
	 * @param previousIsolationLevel the isolation level to restore, if any
	 * @deprecated as of 5.1.11, in favor of
	 * {@link #resetConnectionAfterTransaction(Connection, Integer, boolean)}
	 */
	@Deprecated
	public static void resetConnectionAfterTransaction(Connection con, @Nullable Integer previousIsolationLevel) {
		Assert.notNull(con, "No Connection specified");
		try {
			// Reset transaction isolation to previous value, if changed for the transaction.
			if (previousIsolationLevel != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Resetting isolation level of JDBC Connection [" +
							con + "] to " + previousIsolationLevel);
				}
				con.setTransactionIsolation(previousIsolationLevel);
			}

			// Reset read-only flag.
			if (con.isReadOnly()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Resetting read-only flag of JDBC Connection [" + con + "]");
				}
				con.setReadOnly(false);
			}
		}
		catch (Throwable ex) {
			logger.debug("Could not reset JDBC Connection after transaction", ex);
		}
	}

	/**
	 * Determine whether the given JDBC Connection is transactional, that is,
	 * bound to the current thread by Spring's transaction facilities.
	 * @param con the Connection to check
	 * @param dataSource the DataSource that the Connection was obtained from
	 * (may be {@code null})
	 * @return whether the Connection is transactional
	 */
	// 该JDBC Connection 是否是当前事务内的链接~
	public static boolean isConnectionTransactional(Connection con, @Nullable DataSource dataSource) {
		if (dataSource == null) {
			return false;
		}
		ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
		return (conHolder != null && connectionEquals(conHolder, con));
	}

	/**
	 * Apply the current transaction timeout, if any,
	 * to the given JDBC Statement object.
	 * @param stmt the JDBC Statement object
	 * @param dataSource the DataSource that the Connection was obtained from
	 * @throws SQLException if thrown by JDBC methods
	 * @see java.sql.Statement#setQueryTimeout
	 */
	// Statement 给他设置超时时间  不传timeout表示不超时
	public static void applyTransactionTimeout(Statement stmt, @Nullable DataSource dataSource) throws SQLException {
		applyTimeout(stmt, dataSource, -1);
	}

	/**
	 * Apply the specified timeout - overridden by the current transaction timeout,
	 * if any - to the given JDBC Statement object.
	 * @param stmt the JDBC Statement object
	 * @param dataSource the DataSource that the Connection was obtained from
	 * @param timeout the timeout to apply (or 0 for no timeout outside of a transaction)
	 * @throws SQLException if thrown by JDBC methods
	 * @see java.sql.Statement#setQueryTimeout
	 */
	public static void applyTimeout(Statement stmt, @Nullable DataSource dataSource, int timeout) throws SQLException {
		Assert.notNull(stmt, "No Statement specified");
		ConnectionHolder holder = null;
		if (dataSource != null) {
			holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
		}
		if (holder != null && holder.hasTimeout()) {
			// Remaining transaction timeout overrides specified value.
			stmt.setQueryTimeout(holder.getTimeToLiveInSeconds());
		}
		else if (timeout >= 0) {
			// No current transaction timeout -> apply specified value.
			stmt.setQueryTimeout(timeout);
		}
	}

	/**
	 * Close the given Connection, obtained from the given DataSource,
	 * if it is not managed externally (that is, not bound to the thread).
	 * @param con the Connection to close if necessary
	 * (if this is {@code null}, the call will be ignored)
	 * @param dataSource the DataSource that the Connection was obtained from
	 * (may be {@code null})
	 * @see #getConnection
	 */
	// 此处可能是归还给连接池，也有可能是close~（和连接池参数有关）
	public static void releaseConnection(@Nullable Connection con, @Nullable DataSource dataSource) {
		try {
			doReleaseConnection(con, dataSource);
		}
		catch (SQLException ex) {
			logger.debug("Could not close JDBC Connection", ex);
		}
		catch (Throwable ex) {
			logger.debug("Unexpected exception on closing JDBC Connection", ex);
		}
	}

	/**
	 * Actually close the given Connection, obtained from the given DataSource.
	 * Same as {@link #releaseConnection}, but throwing the original SQLException.
	 * <p>Directly accessed by {@link TransactionAwareDataSourceProxy}.
	 * @param con the Connection to close if necessary
	 * (if this is {@code null}, the call will be ignored)
	 * @param dataSource the DataSource that the Connection was obtained from
	 * (may be {@code null})
	 * @throws SQLException if thrown by JDBC methods
	 * @see #doGetConnection
	 */
	public static void doReleaseConnection(@Nullable Connection con, @Nullable DataSource dataSource) throws SQLException {
		/**
		 * 数据库的连接释放并不是直接调用了 Connection 的API 中的close 方法。
		 * 考虑到存在事务的情况，如果当前线程存在事务，那么说明在当前线程中存在共用数据库连接
		 * （存在事务则说明不止一个sql 语句被执行，则会共用同一个数据库连接, 所以如果当前Sql执行完毕，不能立即关闭数据库连接，而是将引用次数减一），
		 * 这种情况下直接使用 ConnectionHolder 中的released 方法进行连接数减一，而不是真正的释放连接。
		 */

		if (con == null) {
			return;
		}
		if (dataSource != null) {
			// 当前线程存在事务的情况下说明存在共用数据库连接直接使用ConnectionHolder中的released方法进行连接数减一而不是真正的释放连接
			ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
			if (conHolder != null && connectionEquals(conHolder, con)) {
				// It's the transactional Connection: Don't close it.
				conHolder.released();
				return;
			}
		}
		doCloseConnection(con, dataSource);
	}

	/**
	 * Close the Connection, unless a {@link SmartDataSource} doesn't want us to.
	 * @param con the Connection to close if necessary
	 * @param dataSource the DataSource that the Connection was obtained from
	 * @throws SQLException if thrown by JDBC methods
	 * @see Connection#close()
	 * @see SmartDataSource#shouldClose(Connection)
	 */
	public static void doCloseConnection(Connection con, @Nullable DataSource dataSource) throws SQLException {
		if (!(dataSource instanceof SmartDataSource) || ((SmartDataSource) dataSource).shouldClose(con)) {
			con.close();
		}
	}

	/**
	 * Determine whether the given two Connections are equal, asking the target
	 * Connection in case of a proxy. Used to detect equality even if the
	 * user passed in a raw target Connection while the held one is a proxy.
	 * @param conHolder the ConnectionHolder for the held Connection (potentially a proxy)
	 * @param passedInCon the Connection passed-in by the user
	 * (potentially a target Connection without proxy)
	 * @return whether the given Connections are equal
	 * @see #getTargetConnection
	 */
	private static boolean connectionEquals(ConnectionHolder conHolder, Connection passedInCon) {
		if (!conHolder.hasConnection()) {
			return false;
		}
		Connection heldCon = conHolder.getConnection();
		// Explicitly check for identity too: for Connection handles that do not implement
		// "equals" properly, such as the ones Commons DBCP exposes).
		return (heldCon == passedInCon || heldCon.equals(passedInCon) ||
				getTargetConnection(heldCon).equals(passedInCon));
	}

	/**
	 * Return the innermost target Connection of the given Connection. If the given
	 * Connection is a proxy, it will be unwrapped until a non-proxy Connection is
	 * found. Otherwise, the passed-in Connection will be returned as-is.
	 * @param con the Connection proxy to unwrap
	 * @return the innermost target Connection, or the passed-in one if no proxy
	 * @see ConnectionProxy#getTargetConnection()
	 */
	// 如果链接是代理，会拿到最底层的connection
	public static Connection getTargetConnection(Connection con) {
		Connection conToUse = con;
		while (conToUse instanceof ConnectionProxy) {
			conToUse = ((ConnectionProxy) conToUse).getTargetConnection();
		}
		return conToUse;
	}

	/**
	 * Determine the connection synchronization order to use for the given
	 * DataSource. Decreased for every level of nesting that a DataSource
	 * has, checked through the level of DelegatingDataSource nesting.
	 * @param dataSource the DataSource to check
	 * @return the connection synchronization order to use
	 * @see #CONNECTION_SYNCHRONIZATION_ORDER
	 */
	private static int getConnectionSynchronizationOrder(DataSource dataSource) {
		int order = CONNECTION_SYNCHRONIZATION_ORDER;
		DataSource currDs = dataSource;
		while (currDs instanceof DelegatingDataSource) {
			order--;
			currDs = ((DelegatingDataSource) currDs).getTargetDataSource();
		}
		return order;
	}


	/**
	 * Callback for resource cleanup at the end of a non-native JDBC transaction
	 * (e.g. when participating in a JtaTransactionManager transaction).
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	private static class ConnectionSynchronization extends TransactionSynchronizationAdapter {

		private final ConnectionHolder connectionHolder;

		private final DataSource dataSource;

		private int order;

		private boolean holderActive = true;

		public ConnectionSynchronization(ConnectionHolder connectionHolder, DataSource dataSource) {
			this.connectionHolder = connectionHolder;
			this.dataSource = dataSource;
			this.order = getConnectionSynchronizationOrder(dataSource);
		}

		@Override
		public int getOrder() {
			return this.order;
		}

		@Override
		public void suspend() {
			if (this.holderActive) {
				TransactionSynchronizationManager.unbindResource(this.dataSource);
				if (this.connectionHolder.hasConnection() && !this.connectionHolder.isOpen()) {
					// Release Connection on suspend if the application doesn't keep
					// a handle to it anymore. We will fetch a fresh Connection if the
					// application accesses the ConnectionHolder again after resume,
					// assuming that it will participate in the same transaction.
					releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
					this.connectionHolder.setConnection(null);
				}
			}
		}

		@Override
		public void resume() {
			if (this.holderActive) {
				TransactionSynchronizationManager.bindResource(this.dataSource, this.connectionHolder);
			}
		}

		@Override
		public void beforeCompletion() {
			// Release Connection early if the holder is not open anymore
			// (that is, not used by another resource like a Hibernate Session
			// that has its own cleanup via transaction synchronization),
			// to avoid issues with strict JTA implementations that expect
			// the close call before transaction completion.
			if (!this.connectionHolder.isOpen()) {
				TransactionSynchronizationManager.unbindResource(this.dataSource);
				this.holderActive = false;
				if (this.connectionHolder.hasConnection()) {
					releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
				}
			}
		}

		@Override
		public void afterCompletion(int status) {
			// If we haven't closed the Connection in beforeCompletion,
			// close it now. The holder might have been used for other
			// cleanup in the meantime, for example by a Hibernate Session.
			if (this.holderActive) {
				// The thread-bound ConnectionHolder might not be available anymore,
				// since afterCompletion might get called from a different thread.
				TransactionSynchronizationManager.unbindResourceIfPossible(this.dataSource);
				this.holderActive = false;
				if (this.connectionHolder.hasConnection()) {
					releaseConnection(this.connectionHolder.getConnection(), this.dataSource);
					// Reset the ConnectionHolder: It might remain bound to the thread.
					this.connectionHolder.setConnection(null);
				}
			}
			this.connectionHolder.reset();
		}
	}

}

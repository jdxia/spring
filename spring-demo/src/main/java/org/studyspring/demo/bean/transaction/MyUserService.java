package org.studyspring.demo.bean.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Component
public class MyUserService {

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	protected DataSourceTransactionManager transactionManager;

	public void sayHello() {
		System.out.println("hello");
	}


	/**
	 * 表结构参考
	 * CREATE TABLE `user` (
	 * `id` bigint NOT NULL AUTO_INCREMENT,
	 * `username` varchar(32) NOT NULL COMMENT '用户名称',
	 * `birthday` date DEFAULT NULL COMMENT '生日',
	 * `sex` char(1) DEFAULT NULL COMMENT '性别',
	 * `address` varchar(256) DEFAULT NULL COMMENT '地址',
	 * PRIMARY KEY (`id`)
	 * ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
	 */

	@Transactional(timeout = 3, noRollbackFor = TestException.class)
	public void test1() {

		/**
		 * 这个方法检查当前线程是否已经激活了事务同步。事务同步是Spring框架用来在事务执行的不同阶段（如提交前、提交后、回滚等）注册回调的机制
		 * TransactionSynchronizationManager.isSynchronizationActive()
		 *
		 * 这个方法检查是否有"实际的"事务处于活动状态。"实际的"事务意味着已经开始了一个数据库事务（或者类似的资源事务）。
		 * 在某些情况下，事务同步可能是活跃的，但实际的数据库事务可能还没有开始（比如在事务传播行为为SUPPORTS且没有现有事务的情况下）
		 * TransactionSynchronizationManager.isActualTransactionActive()
		 */
		System.out.println("当前是否开启事务: " + TransactionSynchronizationManager.isActualTransactionActive());
		System.out.println("事务名: " + TransactionSynchronizationManager.getCurrentTransactionName());

		//获取当前事务的同步管理器
//		TransactionSynchronizationManager.getSynchronizations();

		eventPublisher.publishEvent(new UserInsertEvent(11));

		System.out.println("事务开始真正执行sql");

		jdbcTemplate.execute("INSERT INTO user (username, birthday, sex, address) \n" +
				"VALUES ('张三', '1990-01-01', '男', '北京市朝阳区');\n");

//		if (Boolean.TRUE) {
//			throw new TestException("111");
//		}

		jdbcTemplate.execute("INSERT INTO user (username, birthday, sex, address) \n" +
				"VALUES ('李四', '1990-01-01', '男', '北京市朝阳区');\n");

		// 事务提交完成之后才可以发
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				System.out.println("afterCommit 1");
			}
		});

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				System.out.println("afterCommit 2");
			}
		});

		System.out.println("事务执行sql完成, 准备commit");
	}

	@Transactional(rollbackFor = Exception.class, timeout = 3)
	public void test2() {
		jdbcTemplate.execute("INSERT INTO user (username, birthday, sex, address) \n" +
				"VALUES ('李四', '2000-01-01', '男', '北京市东城区');\n");
	}

	public void test3() {

		DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
		defaultTransactionDefinition.setTimeout(3);
		TransactionStatus status = transactionManager.getTransaction(defaultTransactionDefinition);

		System.out.println("当前是否开启事务: " + TransactionSynchronizationManager.isActualTransactionActive() + " 事务名: " + TransactionSynchronizationManager.getCurrentTransactionName());

		try {
			System.out.println("事务开始真正执行sql");

			jdbcTemplate.execute("INSERT INTO user (username, birthday, sex, address) \n" +
					"VALUES ('张三', '1990-01-01', '男', '北京市朝阳区');\n");

			jdbcTemplate.execute("INSERT INTO user (username, birthday, sex, address) \n" +
					"VALUES ('李四', '1990-01-01', '男', '北京市朝阳区');\n");

			TimeUnit.SECONDS.sleep(5);

			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()  {

				@Override
				public void afterCommit() {
					System.out.println(" =======> after commit");
				}
			});

			System.out.println("事务执行sql完成, 准备commit");

			transactionManager.commit(status);

			System.out.println("事务commit成功");

		} catch (Throwable e) {
			System.out.println("事务发生异常");
			e.printStackTrace();
			transactionManager.rollback(status);
		} finally {
			if (!status.isCompleted()) {
				System.out.println("事务准备回滚");
				transactionManager.rollback(status);
			}
		}
	}

}

class TestException extends RuntimeException implements Serializable {
	private static final long serialVersionUID = -74214283543270188L;

	public TestException(String message) {
		super(message);
	}
}

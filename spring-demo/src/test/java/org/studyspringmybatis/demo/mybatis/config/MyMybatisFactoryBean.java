package org.studyspringmybatis.demo.mybatis.config;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MyMybatisFactoryBean implements FactoryBean {

	private Class mapperInterface;

	private SqlSession sqlSession;

	public MyMybatisFactoryBean(Class mapperInterface) {
		this.mapperInterface = mapperInterface;
	}

	// 这边要去spring容器里面拿到 sqlSessionFactory
	// sqlSessionFactory 是需要根据配置文件生成的,这边需要自己写 sqlSessionFactory 创建逻辑来定义这个 sqlSessionFactory bean
	@Autowired
	public void setSqlSession(SqlSessionFactory sqlSessionFactory) {
		// 生成这个mapper的代理对象, 存到mybatis里面
		sqlSessionFactory.getConfiguration().addMapper(mapperInterface);

		this.sqlSession = sqlSessionFactory.openSession();
	}

	@Override
	public Object getObject() throws Exception {

		// 从mybatis里面取出这个mapper的代理对象, 然后这个对象放到 ioc的FactoryBeanObjectCache里面
		return sqlSession.getMapper(mapperInterface);

		/**
		     // 不用这种方式了, 让mybatis来帮我们生成
			// 生产一个代理对象
			Object proxyInstance = Proxy.newProxyInstance(
					MyMybatisFactoryBean.class.getClassLoader(),
					new Class[]{mapperInterface}, // 使用具体泛型类型的Class对象数组
					new InvocationHandler() {
						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							System.out.println("hello, this is MyMybatisFactoryBean");
							return null;
						}
					});

			return proxyInstance;
		 */
	}

	@Override
	public Class getObjectType() {
		return mapperInterface;
	}
}

package org.studyspringmybatis.demo.config;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.studyspringmybatis.demo.mybatis.config.MyMapperScan;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@EnableTransactionManagement
@EnableAsync
@EnableAspectJAutoProxy
@ComponentScan("org.studyspringmybatis.demo.**")
@MyMapperScan("org.studyspringmybatis.demo.mybatis.mapper")  // 看下这个自定义注解, 里面@Import({MyImportBeanDefinitionRegistrar.class})
public class AppConfig {

	@Bean
	public SqlSessionFactory sqlSessionFactory() throws IOException {
		InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

		return sqlSessionFactory;
	}

}


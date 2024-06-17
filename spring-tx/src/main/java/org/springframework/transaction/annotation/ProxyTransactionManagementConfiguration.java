/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.transaction.annotation;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * {@code @Configuration} class that registers the Spring infrastructure beans
 * necessary to enable proxy-based annotation-driven transaction management.
 *
 * @author Chris Beams
 * @author Sebastien Deleuze
 * @since 3.1
 * @see EnableTransactionManagement
 * @see TransactionManagementConfigurationSelector
 */
@Configuration(proxyBeanMethods = false) // 配置类, proxyBeanMethods = false应该是不需要生成配置类代理对象
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ProxyTransactionManagementConfiguration extends AbstractTransactionManagementConfiguration {

	// 向spring里面添加了一个 Advisor, 事务的增强器，该方法是否开始事务，是否需要代理该类都在该类中判断
	@Bean(name = TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME)
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisor(
			TransactionAttributeSource transactionAttributeSource, TransactionInterceptor transactionInterceptor) {

		// 事务增强器
		BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
		// 可以看到下面两个bean都是 BeanFactoryTransactionAttributeSourceAdvisor 的属性
		// 向事务增强器中注⼊ 属性解析器 transactionAttributeSource
		advisor.setTransactionAttributeSource(transactionAttributeSource);
		// 向事务增强器中注⼊ 事务拦截器 transactionInterceptor
		advisor.setAdvice(transactionInterceptor);
		if (this.enableTx != null) {
			advisor.setOrder(this.enableTx.<Integer>getNumber("order"));
		}
		return advisor;
	}

	// 保存了事务相关的一些信息资源
	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE) // 属性解析器 transactionAttributeSource
	public TransactionAttributeSource transactionAttributeSource() {
		// AnnotationTransactionAttributeSource 中定义了一个PointCut
		// 并且 AnnotationTransactionAttributeSource 可以用来解析 @Transactional 注解, 并解析完得到一个 RuleBasedTransactionAttribute 对象
		return new AnnotationTransactionAttributeSource();
	}

	// 事务拦截器，事务生成代理类时使用的代理拦截器，编写了事务的规则
	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE) // 事务拦截器 transactionInterceptor, 里面的invoke是事务的核心
	public TransactionInterceptor transactionInterceptor(TransactionAttributeSource transactionAttributeSource) {
		TransactionInterceptor interceptor = new TransactionInterceptor();
		interceptor.setTransactionAttributeSource(transactionAttributeSource);
		if (this.txManager != null) {
			interceptor.setTransactionManager(this.txManager);
		}
		return interceptor;
	}

}

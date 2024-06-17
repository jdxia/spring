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

package org.springframework.transaction.event;

import java.lang.reflect.Method;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListenerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;

/**
 * {@link EventListenerFactory} implementation that handles {@link TransactionalEventListener}
 * annotated methods.
 *
 * @author Stephane Nicoll
 * @since 4.2
 */
public class TransactionalEventListenerFactory implements EventListenerFactory, Ordered {

	// 执行时机还是比较早的~~~（默认的工厂是最低优先级）
	private int order = 50;


	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}


	// 很显然，它要求此方法必须标注@TransactionalEventListener这个注解
	// 备注：@TransactionalEventListener继承自@EventListener
	@Override
	public boolean supportsMethod(Method method) {
		return AnnotatedElementUtils.hasAnnotation(method, TransactionalEventListener.class);
	}

	// 这里使用的是ApplicationListenerMethodTransactionalAdapter，而非ApplicationListenerMethodAdapter
	// 虽然ApplicationListenerMethodTransactionalAdapter是它的子类
	@Override
	public ApplicationListener<?> createApplicationListener(String beanName, Class<?> type, Method method) {
		/**
		 * 通过这个工厂，会把每个标注有@TransactionalEventListener注解的方法最终都包装成一个ApplicationListenerMethodTransactionalAdapter，
		 * 它是一个ApplicationListener，最终注册进事件发射器的容器里面
		 *
		 * 往下看
		 */
		return new ApplicationListenerMethodTransactionalAdapter(beanName, type, method);
	}

}

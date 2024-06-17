/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.cache.interceptor;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.lang.Nullable;

/**
 * Advisor driven by a {@link CacheOperationSource}, used to include a
 * cache advice bean for methods that are cacheable.
 *
 * @author Costin Leau
 * @since 3.1
 */
@SuppressWarnings("serial")
public class BeanFactoryCacheOperationSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor {
	/**
	 * 继承: AbstractBeanFactoryPointcutAdvisor
	 *
	 * 作用是在每个bean的初始化时
	 * 			(每个bean都会被加载成 advised 对象 -> 有 targetSource 和 Advisor[] 数组),
	 * 			每个bean被调用方法的时候都是先遍历advisor的方法，然后在调用原生bean(也就是targetSource)的方法，实现了aop的效果
	 */

	@Nullable
	private CacheOperationSource cacheOperationSource;

	private final CacheOperationSourcePointcut pointcut = new CacheOperationSourcePointcut() {
		@Override
		@Nullable
		protected CacheOperationSource getCacheOperationSource() {
			return cacheOperationSource;
		}
	};


	/**
	 * Set the cache operation attribute source which is used to find cache
	 * attributes. This should usually be identical to the source reference
	 * set on the cache interceptor itself.
	 */
	public void setCacheOperationSource(CacheOperationSource cacheOperationSource) {
		this.cacheOperationSource = cacheOperationSource;
	}

	/**
	 * Set the {@link ClassFilter} to use for this pointcut.
	 * Default is {@link ClassFilter#TRUE}.
	 */
	public void setClassFilter(ClassFilter classFilter) {
		this.pointcut.setClassFilter(classFilter);
	}

	/**
	 * getPointcut() 也就是 CacheOperationSourcePointcut
	 *
	 * 然后调用CacheOperationSourcePointcut.matches()方法, 用来匹配对应的bean,
	 * 假设bean在BeanFactoryCacheOperationSourceAdvisor的扫描中 matchs() 方法返回了true,
	 * 结果就是在每个bean的方法被调用的时候 CacheInterceptor 中的 invoke() 方法就会被调用
	 */
	@Override
	public Pointcut getPointcut() {
		return this.pointcut;
	}

}

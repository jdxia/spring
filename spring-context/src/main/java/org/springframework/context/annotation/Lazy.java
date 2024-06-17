/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates whether a bean is to be lazily initialized.
 *
 * <p>May be used on any class directly or indirectly annotated with {@link
 * org.springframework.stereotype.Component @Component} or on methods annotated with
 * {@link Bean @Bean}.
 *
 * <p>If this annotation is not present on a {@code @Component} or {@code @Bean} definition,
 * eager initialization will occur. If present and set to {@code true}, the {@code @Bean} or
 * {@code @Component} will not be initialized until referenced by another bean or explicitly
 * retrieved from the enclosing {@link org.springframework.beans.factory.BeanFactory
 * BeanFactory}. If present and set to {@code false}, the bean will be instantiated on
 * startup by bean factories that perform eager initialization of singletons.
 *
 * <p>If Lazy is present on a {@link Configuration @Configuration} class, this
 * indicates that all {@code @Bean} methods within that {@code @Configuration}
 * should be lazily initialized. If {@code @Lazy} is present and false on a {@code @Bean}
 * method within a {@code @Lazy}-annotated {@code @Configuration} class, this indicates
 * overriding the 'default lazy' behavior and that the bean should be eagerly initialized.
 *
 * <p>In addition to its role for component initialization, this annotation may also be placed
 * on injection points marked with {@link org.springframework.beans.factory.annotation.Autowired}
 * or {@link javax.inject.Inject}: In that context, it leads to the creation of a
 * lazy-resolution proxy for all affected dependencies, as an alternative to using
 * {@link org.springframework.beans.factory.ObjectFactory} or {@link javax.inject.Provider}.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.0
 * @see Primary
 * @see Bean
 * @see Configuration
 * @see org.springframework.stereotype.Component
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lazy {
	/**
	 * 指示一个bean是否应懒加载初始化。
	 * 可直接或间接地用于带 org.springframework.stereotype.Component @Component 注解的类，
	 * 或者用于带有 Bean @Bean 注解的方法。
	 *
	 * 如果此注解不在 @Component 或 @Bean 定义上，将会立即初始化。
	 * 如果存在并且设置为 true，除非被另一个bean引用或从包围的 org.springframework.beans.factory.BeanFactory BeanFactory 中显式检索，
	 * 否则 @Bean 或 @Component 不会初始化。如果存在并设置为 false，那么执行积极初始化单例的bean工厂将在启动时实例化bean。
	 * 如果Lazy存在于 Configuration @Configuration 类上，表示该 @Configuration 中的所有 @Bean 方法都应懒加载。
	 * 如果在一个带有 @Lazy 注解的 @Configuration 类的 @Bean 方法上 @Lazy 设置为false，则表示覆盖了“默认懒惰”行为，该bean应立即初始化。
	 *
	 * 除了其在组件初始化中的作用外，此注解也可用于带有 org.springframework.beans.factory.annotation.Autowired 或 javax.inject.Inject 的注入点：
	 * 在这种情况下，它会导致为所有受影响的依赖关系创建一个懒解析代理，作为使用 org.springframework.beans.factory.ObjectFactory 或 javax.inject.Provider 的替代方法。
	 */


	/**
	 * 是否应进行懒加载初始化。
	 */
	/**
	 * Whether lazy initialization should occur.
	 */
	boolean value() default true;

}

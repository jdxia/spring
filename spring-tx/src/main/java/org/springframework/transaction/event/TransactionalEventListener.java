/*
 * Copyright 2002-2019 the original author or authors.
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AliasFor;

/**
 * An {@link EventListener} that is invoked according to a {@link TransactionPhase}.
 *
 * <p>If the event is not published within an active transaction, the event is discarded
 * unless the {@link #fallbackExecution} flag is explicitly set. If a transaction is
 * running, the event is processed according to its {@code TransactionPhase}.
 *
 * <p>Adding {@link org.springframework.core.annotation.Order @Order} to your annotated
 * method allows you to prioritize that listener amongst other listeners running before
 * or after transaction completion.
 *
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 4.2
 */
// @since 4.2 注解的方式提供的相对较晚，其实API的方式在第一个版本就已经提供了。
// 值得注意的是，在这个注解上面有一个注解：`@EventListener`，所以表明其实这个注解也是个事件监听器。
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EventListener //有类似于注解继承的效果
public @interface TransactionalEventListener {

	/**
	 * Phase to bind the handling of an event to.
	 * <p>The default phase is {@link TransactionPhase#AFTER_COMMIT}.
	 * <p>If no transaction is in progress, the event is not processed at
	 * all unless {@link #fallbackExecution} has been enabled explicitly.
	 */
	/**
	 * 这个注解取值有：
	 *  BEFORE_COMMIT(指定目标方法在事务commit之前执行)
	 *  AFTER_COMMIT(指定目标方法在事务commit之后执行)、
	 *  AFTER_ROLLBACK(指定目标方法在事务rollback之后执行)
	 *  AFTER_COMPLETION(指定目标方法在事务完成时执行，这里的完成是指无论事务是成功提交还是事务回滚了)
	 *  各个值都代表什么意思表达什么功能，非常清晰
	 *  需要注意的是：AFTER_COMMIT + AFTER_COMPLETION是可以同时生效的
	 *  AFTER_ROLLBACK + AFTER_COMPLETION是可以同时生效的
	 */
	TransactionPhase phase() default TransactionPhase.AFTER_COMMIT;

	/**
	 * Whether the event should be processed if no transaction is running.
	 */
	// 表明若没有事务的时候，对应的event是否需要执行，默认值为false表示，没事务就不执行了。
	boolean fallbackExecution() default false;

	/**
	 * Alias for {@link #classes}.
	 */
	// 这里巧妙用到了@AliasFor的能力，放到了@EventListener身上
	// 注意：一般建议都需要指定此值，否则默认可以处理所有类型的事件，范围太广了。
	@AliasFor(annotation = EventListener.class, attribute = "classes")
	Class<?>[] value() default {};

	/**
	 * The event classes that this listener handles.
	 * <p>If this attribute is specified with a single value, the annotated
	 * method may optionally accept a single parameter. However, if this
	 * attribute is specified with multiple values, the annotated method
	 * must <em>not</em> declare any parameters.
	 */
	@AliasFor(annotation = EventListener.class, attribute = "classes")
	Class<?>[] classes() default {};

	/**
	 * Spring Expression Language (SpEL) attribute used for making the event
	 * handling conditional.
	 * <p>The default is {@code ""}, meaning the event is always handled.
	 * @see EventListener#condition
	 */
	String condition() default "";

}

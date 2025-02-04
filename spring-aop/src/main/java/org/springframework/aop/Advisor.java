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

package org.springframework.aop;

import org.aopalliance.aop.Advice;

/**
 * Base interface holding AOP <b>advice</b> (action to take at a joinpoint)
 * and a filter determining the applicability of the advice (such as
 * a pointcut). <i>This interface is not for use by Spring users, but to
 * allow for commonality in support for different types of advice.</i>
 *
 * <p>Spring AOP is based around <b>around advice</b> delivered via method
 * <b>interception</b>, compliant with the AOP Alliance interception API.
 * The Advisor interface allows support for different types of advice,
 * such as <b>before</b> and <b>after</b> advice, which need not be
 * implemented using interception.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface Advisor {
	/**
	 * Advice是通知，Advisor是增强器，每个Advisor都会持有一个Advice
	 * Advisor 包含Advice和Pointcut。个人认为是 Spring AOP完成增强动作的最小单元。
	 *
	 * Advisor：代表一般切面，它仅包含一个Advice。这个切面太宽泛，一般不会直接使用。
	 * PointcutAdvisor：代表具有切点的切面，它包含Advice和Pointcut两个类。
	 * IntroductionAdvisor：代表引介切面。引介切面是对应引介增强的特殊的切面，它应用于类层面上。
	 *
	 * Advisor两个子接口PointcutAdvisor、IntroductionAdvisor :
	 * IntroductionAdvisor与PointcutAdvisor 最本质上的区别就是，
	 * 		IntroductionAdvisor 只能应用于类级别的拦截,只能使用Introduction型的Advice。
	 * 		而不能像 PointcutAdvisor 那样，可以使用任何类型的Pointcut,以及几乎任何类型的Advice
	 */

	/**
	 * Common placeholder for an empty {@code Advice} to be returned from
	 * {@link #getAdvice()} if no proper advice has been configured (yet).
	 * @since 5.0
	 */
	Advice EMPTY_ADVICE = new Advice() {};


	/**
	 * Return the advice part of this aspect. An advice may be an
	 * interceptor, a before advice, a throws advice, etc.
	 * @return the advice that should apply if the pointcut matches
	 * @see org.aopalliance.intercept.MethodInterceptor
	 * @see BeforeAdvice
	 * @see ThrowsAdvice
	 * @see AfterReturningAdvice
	 */
	Advice getAdvice(); // 这个接口，可以获取Advisor持有的Advice

	/**
	 * Return whether this advice is associated with a particular instance
	 * (for example, creating a mixin) or shared with all instances of
	 * the advised class obtained from the same Spring bean factory.
	 * <p><b>Note that this method is not currently used by the framework.</b>
	 * Typical Advisor implementations always return {@code true}.
	 * Use singleton/prototype bean definitions or appropriate programmatic
	 * proxy creation to ensure that Advisors have the correct lifecycle model.
	 * @return whether this advice is associated with a particular target instance
	 */
	boolean isPerInstance();

}

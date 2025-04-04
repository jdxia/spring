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

package org.springframework.context.annotation;

import java.util.function.Predicate;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;

/**
 * 实现了确定基于给定选择条件应该导入哪些 @Configuration 类的类型的接口，
 * 通常是一个或多个注解属性。
 *
 * 一个 ImportSelector 可以实现以下任意一个 Aware 接口，并在调用 selectImports
 * 方法之前调用其对应的方法：
 *    EnvironmentAware
 *    BeanFactoryAware
 *    BeanClassLoaderAware
 *    ResourceLoaderAware
 *
 * 另外，该类也可以提供一个带有以下支持的参数类型的单个构造函数：
 *    Environment
 *    BeanFactory
 *    ClassLoader
 *    ResourceLoader
 *
 * ImportSelector 实现通常与普通的 @Import 注解一样进行处理。
 * 然而，还可以推迟选择要导入的内容，直到所有 @Configuration
 * 类都被处理完毕（详见 DeferredImportSelector）。
 *
 */
public interface ImportSelector {


	/**
	 * 根据导入的 @Configuration 类的 AnnotationMetadata（注解元数据），
	 * 选择并返回应该导入的类名称。
	 *
	 * @return 返回类名的数组，如果没有则返回空数组。
	 */
	String[] selectImports(AnnotationMetadata importingClassMetadata);

	/**
	 * 返回一个用于从导入的候选类中排除类的断言函数，
	 * 该函数会递归地应用于通过此选择器的导入项找到的所有类。
	 *
	 * 如果对于给定的完全限定类名，该断言函数返回 true，
	 * 则该类将不被视为被导入的配置类，从而跳过类文件加载和元数据检查。
	 *
	 * @return 返回一个用于完全限定的候选类名的筛选断言函数，该函数适用于递归导入的配置类。
	 * 如果没有筛选断言函数，则返回 null。
	 * @since 5.2.4
	 */
	@Nullable
	default Predicate<String> getExclusionFilter() {
		return null;
	}

}

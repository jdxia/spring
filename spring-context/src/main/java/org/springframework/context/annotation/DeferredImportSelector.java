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

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;

/**
 * 一种在所有 @Configuration bean 处理完毕后运行的 ImportSelector 变体。
 * 这种类型的选择器在所选的导入项带有条件时特别有用。
 *
 * 实现类可以扩展 org.springframework.core.Ordered 接口或使用
 * org.springframework.core.annotation.Order 注解来指定与
 * 其他 DeferredImportSelectors 的优先级。
 *
 * 实现类还可以提供一个导入组（import group），
 * 它可以在不同的选择器之间提供额外的排序和过滤逻辑。
 */
public interface DeferredImportSelector extends ImportSelector {
	/**
	 * DeferredImportSelector 具有延迟导入的能力，可以在所有的 @Configuration 类都被处理完毕之后再进行选择和导入。
	 * 这样可以在整个配置加载过程完成后再根据某些条件或规则来决定要导入哪些类，从而实现更加动态和灵活的自动配置机制
	 *
	 */

	/**
	 * 返回一个特定的导入组。
	 * 默认实现会在不需要分组的情况下返回 null。
	 *
	 * @return 导入组的类，如果没有则返回 null。
	 * @since 5.0
	 */
	@Nullable
	default Class<? extends Group> getImportGroup() {
		return null;
	}


	/**
	 * 用于将来自不同导入选择器的结果进行分组的接口。
	 *
	 * @since 5.0
	 */
	interface Group {

		/**
		 * 使用指定的 DeferredImportSelector 处理导入的 @Configuration 类的 AnnotationMetadata。
		 */
		void process(AnnotationMetadata metadata, DeferredImportSelector selector);

		/**
		 * 返回此组应该导入的类的条目
		 */
		Iterable<Entry> selectImports();


		/**
		 * 一个条目，包含导入的配置类的 AnnotationMetadata 和要导入的类名。
		 */
		class Entry {

			private final AnnotationMetadata metadata;

			private final String importClassName;

			public Entry(AnnotationMetadata metadata, String importClassName) {
				this.metadata = metadata;
				this.importClassName = importClassName;
			}

			/**
			 * 返回导入的配置类的 AnnotationMetadata【注解元数据】
			 */
			public AnnotationMetadata getMetadata() {
				return this.metadata;
			}

			/**
			 * 返回要导入的类的完全限定名称。
			 */
			public String getImportClassName() {
				return this.importClassName;
			}

			@Override
			public boolean equals(@Nullable Object other) {
				if (this == other) {
					return true;
				}
				if (other == null || getClass() != other.getClass()) {
					return false;
				}
				Entry entry = (Entry) other;
				return (this.metadata.equals(entry.metadata) && this.importClassName.equals(entry.importClassName));
			}

			@Override
			public int hashCode() {
				return (this.metadata.hashCode() * 31 + this.importClassName.hashCode());
			}

			@Override
			public String toString() {
				return this.importClassName;
			}
		}
	}

}

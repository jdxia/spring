package org.studyspringmybatis.demo.mybatis.config;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import org.studyspring.demo.bean.aop.B;
import org.studyspringmybatis.demo.mybatis.mapper.UserMapper;

import java.io.IOException;
import java.util.Map;

/**
 * ImportBeanDefinitionRegistrar 是spring留的一个扩展点
 * 源码:
 * 	ConfigurationClassPostProcessor#processConfigBeanDefinitions 方法是spring容器加载bean的入口, 方法里面的 this.reader.loadBeanDefinitions(configClasses);
 * 	往里面追, 会有 loadBeanDefinitionsFromRegistrars(configClass.getImportBeanDefinitionRegistrars());
 *
 * 注册bean的后置处理器, MyMapperScan注解里面import了这个类
 */
public class MyImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	/**
	 * AnnotationMetadata 注解的元信息
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
		// 获取扫描路径
		Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(MyMapperScan.class.getName());
		String scanPath = (String) annotationAttributes.get("value");

		System.out.println("扫描路径是: " + scanPath);

		// 创建一个扫描器
		MyBeanDefinitionScanner myBeanDefinitionScanner = new MyBeanDefinitionScanner(registry);

		//spring默认只扫描 @Component 注解的类, 如果你要扫描其他注解的话, 需要自定义扫描器
		myBeanDefinitionScanner.addIncludeFilter(new TypeFilter() {
			/**
			 * metadataReader: 通过这个 Reader ，可以读取到正在扫描的类的信息（包括类的信息、类上标注的注解等）
			 * metadataReaderFactory: 借助这个 Factory ，可以获取到其他类的 Reader ，进而获取到那些类的信息, 可以这样理解：借助 ReaderFactory 可以获取到 Reader ，借助 Reader 可以获取到指定类的信息
			 */
			@Override
			public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
				// 所有一切都返回true
				return Boolean.TRUE;
			}
		});

		// 扫描路径下的所有的类, 会把所有的类都扫描出来, spring默认不关心接口
		// 所以我们要重新写一个扫描器继承spring的, 然后只扫接口
		myBeanDefinitionScanner.scan(scanPath);

		/**
		 	// 下面几行代码可以不要了, 下面是手写一个个注册mybatis的mapper, 但是我们可以通过上面的扫描器, 扫描到所有的mapper, 然后注册
		 	// 不用手动注册了, 自动化去做

			AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
			beanDefinition.setBeanClass(MyMybatisFactoryBean.class);
			// 给构造方法指定一个值, 就是入参
			beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(UserMapper.class);
			registry.registerBeanDefinition("userMapper", beanDefinition);   // userMapper ===> 对应userMapper的代理对象

			// 如果你这边还有orderMapper这边的话, 也可以注册一个

		 */


	}
}

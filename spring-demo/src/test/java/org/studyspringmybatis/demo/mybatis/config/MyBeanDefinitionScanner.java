package org.studyspringmybatis.demo.mybatis.config;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

import java.util.Set;

// ClassPathBeanDefinitionScanner : spring提供的扫描器
public class MyBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

	public MyBeanDefinitionScanner(BeanDefinitionRegistry registry) {
		super(registry);
	}

	@Override
	protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
		// 这边可以断点看, 自己扫描器扫描到几个 beanDefinition
		Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);

		// 虽然扫描到了, 但是这个BeanDefinition不符合我们的要求
		for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
			// 用 GenericBeanDefinition 是为了可以用 setAutowireMode 方法
			GenericBeanDefinition beanDefinition = (GenericBeanDefinition) beanDefinitionHolder.getBeanDefinition();

			//下面2步 顺序不能反, 不然 你的beanClassName先改成MyMybatisFactoryBean, 你的构造器参数就不是接口的类名了

			// 构造器参数里面放入一个值, 就是接口的类名, 也就是FactoryBean的构造器参数
			beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition.getBeanClassName());
			// 设置Bean的类名称, 这个类是我们自己定义的FactoryBean
			beanDefinition.setBeanClassName(MyMybatisFactoryBean.class.getName());

			// 可以不用这个, 那要在对应的方法上加 @Autowired注解, 比如MyMybatisFactoryBean的setSqlSession方法
//			beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

		}

		return beanDefinitionHolders;
	}

	@Override
	protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		// 只扫描接口, 默认spring是不扫描接口的
		return beanDefinition.getMetadata().isInterface();
	}
}

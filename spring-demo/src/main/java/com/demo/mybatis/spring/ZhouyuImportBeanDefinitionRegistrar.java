package com.demo.mybatis.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;


public class ZhouyuImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {

		// 扫描Mapper
		String path = (String) importingClassMetadata.getAnnotationAttributes(ZhouyuMapperScan.class.getName()).get("value");

		ZhouyuClassPathMapperScanner scanner = new ZhouyuClassPathMapperScanner(registry);
		scanner.scan(path);
	}
}

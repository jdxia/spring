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

package org.springframework.context.support;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

/**
 * Convenient base class for {@link org.springframework.context.ApplicationContext}
 * implementations, drawing configuration from XML documents containing bean definitions
 * understood by an {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}.
 *
 * <p>Subclasses just have to implement the {@link #getConfigResources} and/or
 * the {@link #getConfigLocations} method. Furthermore, they might override
 * the {@link #getResourceByPath} hook to interpret relative paths in an
 * environment-specific fashion, and/or {@link #getResourcePatternResolver}
 * for extended pattern resolution.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getConfigResources
 * @see #getConfigLocations
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 */
public abstract class AbstractXmlApplicationContext extends AbstractRefreshableConfigApplicationContext {

	private boolean validating = true;


	/**
	 * Create a new AbstractXmlApplicationContext with no parent.
	 */
	public AbstractXmlApplicationContext() {
	}

	/**
	 * Create a new AbstractXmlApplicationContext with the given parent context.
	 * @param parent the parent context
	 */
	public AbstractXmlApplicationContext(@Nullable ApplicationContext parent) {
		super(parent);
	}


	/**
	 * Set whether to use XML validation. Default is {@code true}.
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}


	/**
	 * Loads the bean definitions via an XmlBeanDefinitionReader.
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 * @see #initBeanDefinitionReader
	 * @see #loadBeanDefinitions
	 */
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
		// Create a new XmlBeanDefinitionReader for the given BeanFactory.
		// DefaultListableBeanFactory 实现了 BeanDefinitionRegistry 接口，所以在初始化 XmlBeanDefinitionReader 时
		// 将该 beanFactory 传入 XmlBeanDefinitionReader 的构造方法中。
		// 从名字也能看出来它的功能，这是一个用于从 .xml文件 中读取 BeanDefinition 的读取器
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

		// Configure the bean definition reader with this context's
		// resource loading environment.
		beanDefinitionReader.setEnvironment(this.getEnvironment());
		// 为 beanDefinition 读取器设置 资源加载器，由于本类的基类 AbstractApplicationContext
		// 继承了 DefaultResourceLoader，因此，本容器自身也是一个资源加载器
		beanDefinitionReader.setResourceLoader(this);
		// 为 beanDefinitionReader 设置用于解析的 SAX 实例解析器，SAX（simple API for XML）是另一种XML解析方法。
		// 相比于DOM，SAX速度更快，占用内存更小。它逐行扫描文档，一边扫描一边解析。相比于先将整个XML文件扫描进内存，
		// 再进行解析的DOM，SAX可以在解析文档的任意时刻停止解析，但操作也比DOM复杂。
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

		// Allow a subclass to provide custom initialization of the reader,
		// then proceed with actually loading the bean definitions.
		// 初始化 beanDefinition 读取器，该方法同时启用了 XML 的校验机制
		initBeanDefinitionReader(beanDefinitionReader);
		// 用传进来的 XmlBeanDefinitionReader 读取器读取 .xml 文件中配置的 bean
		loadBeanDefinitions(beanDefinitionReader);
	}

	/**
	 * Initialize the bean definition reader used for loading the bean
	 * definitions of this context. Default implementation is empty.
	 * <p>Can be overridden in subclasses, e.g. for turning off XML validation
	 * or using a different XmlBeanDefinitionParser implementation.
	 * @param reader the bean definition reader used by this context
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader#setDocumentReaderClass
	 */
	protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
		reader.setValidating(this.validating);
	}

	/**
	 * Load the bean definitions with the given XmlBeanDefinitionReader.
	 * <p>The lifecycle of the bean factory is handled by the {@link #refreshBeanFactory}
	 * method; hence this method is just supposed to load and/or register bean definitions.
	 * @param reader the XmlBeanDefinitionReader to use
	 * @throws BeansException in case of bean registration errors
	 * @throws IOException if the required XML document isn't found
	 * @see #refreshBeanFactory
	 * @see #getConfigLocations
	 * @see #getResources
	 * @see #getResourcePatternResolver
	 */
	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
		/**
		 * ClassPathXmlApplicationContext 与 FileSystemXmlApplicationContext
		 * 在这里的调用出现分歧，各自按不同的方式加载解析 Resource 资源
		 * 最后在具体的解析和 BeanDefinition 定位上又会殊途同归
		 */

		// 获取存放了 BeanDefinition 的所有 Resource，FileSystemXmlApplicationContext 中未对
		// getConfigResources() 进行重写，所以调用父类的，return null。
		// 而 ClassPathXmlApplicationContext 对该方法进行了重写，返回设置的值
		Resource[] configResources = getConfigResources();
		//第一个if是看有没有系统指定的配置文件，如果没有的话就走第二个if
		if (configResources != null) {
			// 这里调用的是其父类 AbstractBeanDefinitionReader 中的方法，解析加载 BeanDefinition对象
			reader.loadBeanDefinitions(configResources);
		}
		// 调用其父类 AbstractRefreshableConfigApplicationContext 中的实现，优先返回
		// FileSystemXmlApplicationContext 构造方法中调用 setConfigLocations() 方法设置的资源路径
		String[] configLocations = getConfigLocations();
		// 加载我们最开始传入的classpath:application-ioc.xml
		if (configLocations != null) {
			// 这里调用其父类 AbstractBeanDefinitionReader 的方法从配置位置加载 BeanDefinition
			reader.loadBeanDefinitions(configLocations);
		}
	}

	/**
	 * Return an array of Resource objects, referring to the XML bean definition
	 * files that this context should be built with.
	 * <p>The default implementation returns {@code null}. Subclasses can override
	 * this to provide pre-built Resource objects rather than location Strings.
	 * @return an array of Resource objects, or {@code null} if none
	 * @see #getConfigLocations()
	 */
	@Nullable
	protected Resource[] getConfigResources() {
		return null;
	}

}

package com.study.xmlapp;


import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContextEvent;
import java.io.File;

public class XmlApplicationContextTest {

	public static void main(String[] args) {
		/**
		 * <b>当一个请求来的时候, 已经初始化完成后</b>
		 * <p>
		 * tomcat 处理请求的时候, 调用servlet 的 service方法, {@link javax.servlet.GenericServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}
		 * 最终是子类来实现这个方法 {@link javax.servlet.http.HttpServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}
		 * 然后调用 里面的 this.service(request, response)
		 * get请求调用doGet, post请求调用doPost, 这些方法都会被子类重写
		 * {@link org.springframework.web.servlet.FrameworkServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
		 * 一路追下去,就会到
		 * {@link org.springframework.web.servlet.DispatcherServlet#doDispatch(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
		 *
		 */

		Tomcat tomcat = new Tomcat();
		Connector conn = new Connector();
		conn.setPort(8272);
		tomcat.setConnector(conn);

		/**
		 * 根应用程序上下文（Root Application Context）：它通常在应用程序启动时被创建，包含全局的Bean定义，例如服务层（Service）、数据访问层（DAO）等。
		 *
		 * Web应用程序上下文（Web Application Context）：它是根上下文的子上下文，主要用于Web相关的Bean定义，例如控制器（Controller）、视图解析器（View Resolver）等
		 */

		Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());
		/**
		 * <p> 父子容器 </p>
		 *
		 * 这个 ContextLoaderListener 就是用来创建父容器的
		 * tomcat会看有没有配置 listener 配置的话, 他会来执行这个 listener 的初始化方法
		 *
		 * Servlet规范中ServletContext表示web应用的上下文环境，而web应用对应tomcat的概念是Context，所以从设计上，ServletContext自然会成为tomcat的Context具体实现的一个成员变量。
		 * tomcat内部实现也是这样完成的，ServletContext对应tomcat实现是 org.apache.catalina.core.ApplicationContext，Context容器对应tomcat实现是 org.apache.catalina.core.StandardContext。
		 * ApplicationContext是StandardContext的一个成员变量。
		 *
		 * tomcat的StandardContext和tomcat的 ApplicationContext 是相互持有，
		 * StandardContext中存在成员变量protected ApplicationContext context = null;
		 * 而tomcat的ApplicationContext中也存在StandardContext。
		 *
		 * tomcat的 ApplicationContext 实现了ServletContext接口，并实现了setAttribute(String name, Object value)方法，
		 * 而Spring里的ApplicationContext初始化后通过 setAttribute 方法被置入到tomcat的ApplicationContext的Map<String,String> readOnlyAttributes中。
		 *
		 * Servlet规范中ServletContext是tomcat的Context实现的一个成员变量，而Spring的ApplicationContext是Servlet规范中ServletContext的一个属性
		 */
		ctx.addApplicationListener("org.springframework.web.context.ContextLoaderListener");
		ctx.addParameter("contextConfigLocation", "classpath:com/study/xmlapp/spring-application.xml"); // 父容器 service

		// web.xml配置, 现在在这里配置了
		/**
		 * <p> 初始化逻辑 </p>
		 *
		 * 源码先从 DispatcherServlet 的父类 HttpServletBean 看, tomcat肯定会调用 {@link org.springframework.web.servlet.HttpServletBean#init} 方法
		 * 初始化了 servlet容器, 是子容器
		 *
		 * <p>
		 * Tomcat&Jetty在启动时给每个Web应用创建一个全局的上下文环境，这个上下文就是ServletContext，其为后面的Spring容器提供宿主环境。
		 *
		 * Tomcat&Jetty在启动过程中触发容器初始化事件，Spring的 ContextLoaderListener 会监听到这个事件，它的 contextInitialized 方法会被调用，
		 * {@link ContextLoaderListener#contextInitialized(ServletContextEvent)}
		 * 在这个方法中，Spring会初始化全局的Spring根容器，这个就是Spring的IoC容器，
		 * IoC容器初始化完毕后，Spring将其存储到ServletContext中，便于以后来获取。
		 *
		 * Tomcat&Jetty在启动过程中还会扫描Servlet，一个Web应用中的Servlet可以有多个，以SpringMVC中的 DispatcherServlet 为例，这个Servlet实际上是一个标准的前端控制器，用以转发、匹配、处理每个Servlet请求。
		 *
		 * Servlet一般会延迟加载，当第一个请求达到时，Tomcat&Jetty 发现DispatcherServlet还没有被实例化，就调用 DispatcherServlet 的init方法，DispatcherServlet 在初始化的时候会建立自己的容器，叫做SpringMVC 容器，用来持有Spring MVC相关的Bean。
		 * 同时，Spring MVC还会通过ServletContext拿到Spring根容器，并将Spring根容器设为SpringMVC容器的父容器，
		 * 请注意，Spring MVC容器可以访问父容器中的Bean，但是父容器不能访问子容器的Bean， 也就是说Spring根容器不能访问SpringMVC容器里的B
		 *
		 * <p>
		 * Servlet 规范里定义了 ServletContext 这个接口来对应一个 Web 应用。Web 应用部署好后，Servlet 容器在启动时会加载 Web 应用，并为每个 Web 应用创建唯一的 ServletContext 对象。
		 * 你可以把 ServletContext 看成是一个全局对象，一个 Web 应用可能有多个 Servlet，这些 Servlet 可以通过全局的 ServletContext 来共享数据，这些数据包括 Web 应用的初始化参数、Web 应用目录下的文件资源等。
		 * 由于 ServletContext 持有所有 Servlet 实例，你还可以通过它来实现 Servlet 请求的转发。
		 */
		Wrapper mvc = tomcat.addServlet("", "mvc", "org.springframework.web.servlet.DispatcherServlet");
		mvc.addMapping("/");
		mvc.addInitParameter("contextConfigLocation", "classpath:com/study/xmlapp/spring-mvc.xml");  // 子容器 servlet
		mvc.setAsyncSupported(true);
		mvc.setLoadOnStartup(1);

		/**
		 * /tmp 这是文件上传时存储临时文件的目录。当上传的文件超过指定大小时，Servlet 容器会将其写入这个临时目录
		 * 2097152 以字节为单位。这里设置的是 2MB (2 * 1024 * 1024 字节)。超出这个大小的文件会被拒绝上传。
		 * 4194304 以字节为单位。这里设置的是 4MB (4 * 1024 * 1024 字节)。它包括所有上传的文件及其他表单数据的总和。超出这个大小的请求会被拒绝。
		 * 0 这是写入临时文件之前文件数据的最大字节数。设置为 0 表示无限制，所有文件都会直接写入到临时目录，而不会在内存中暂存
		 */
		MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/tmp", 2097152, 4194304, 0);
		mvc.setMultipartConfigElement(multipartConfigElement);


		try {
			tomcat.start();
			tomcat.getServer().await();
		} catch (LifecycleException e) {
			e.printStackTrace();
		}
	}
}


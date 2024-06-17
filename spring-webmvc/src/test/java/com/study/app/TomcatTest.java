package com.study.app;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.MultipartConfigElement;
import java.io.File;

@RestController
public class TomcatTest {

	// http://127.0.0.1:8272/ping?msg=11
	@GetMapping("/ping")
	public String ping(String msg) {
		return "pong: " + msg;
	}

	public static void main(String[] args) {
		Tomcat tomcat = new Tomcat();
		Connector conn = new Connector();
		conn.setPort(8272);
		tomcat.setConnector(conn);

		/**
		 * DispatcherServlet 是springmvc的核心, 核心方法是里面的 doService 方法
		 * 我这边是一开始就创建 DispatcherServlet, 类似web.xml里面的 load-on-startup为1, 可以为0就是启动的时候不创建 DispatcherServlet, 等有请求过来的时候发现没有这个对象再创建
		 *
		 */
		DispatcherServlet dispatcherServlet = new DispatcherServlet();

		AnnotationConfigWebApplicationContext annotationConfigWebApplicationContext = new AnnotationConfigWebApplicationContext();
		annotationConfigWebApplicationContext.register(TomcatTest.class);
		annotationConfigWebApplicationContext.refresh();
		dispatcherServlet.setApplicationContext(annotationConfigWebApplicationContext);


		Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());
		Wrapper mvc = Tomcat.addServlet(ctx, "mvc", dispatcherServlet);
		mvc.addMapping("/*");
		mvc.setLoadOnStartup(1); // 设置 loadOnStartup 为 1
		mvc.setAsyncSupported(true);

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


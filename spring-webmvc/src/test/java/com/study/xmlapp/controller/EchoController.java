package com.study.xmlapp.controller;


import com.study.xmlapp.service.EchoService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;


@RestController
@RequestMapping("/")
public class EchoController {  // 也可以实现 Controller 接口

	@Resource
	private EchoService echoService;

	// Get http://127.0.0.1:8272/echo?msg=11

	/**
	 * url的解析逻辑是在 DispatcherServlet 的父类 HttpServletBean#init 里面的
	 * <p> <b>initWebApplicationContext 方法里面的 => configureAndRefreshWebApplicationContext 方法里面 => ContextRefreshListener 监听器</b>
	 * <p> <b> ContextRefreshListener 监听器一路往下走 onApplicationEvent => DispatcherServlet#onRefresh 里面的 initStrategies 方法往下 => initHandlerMappings(context) </b>
	 * <p> <b> initHandlerMappings 就是开始核心, 这个方法里面的 getDefaultStrategies 方法会初始化 HandlerMapping 类型的bean </b>
	 * <p> <b> getDefaultStrategies 方法里面看下, HandlerMapping 类型的bean 会从 DispatcherServlet.properties 里面取, 有个 RequestMappingHandlerMapping </b>
	 * <p> <b> RequestMappingHandlerMapping 通过createBean创建出来, 会走他自己的 afterPropertiesSet, 调用父类的 afterPropertiesSet => initHandlerMethods </b>
	 * <p> <b> initHandlerMethods 方法里面 processCandidateBean 方法 => detectHandlerMethods(beanName) 方法 => registerHandlerMethod 方法, 会把 url和注解以及方法解析出来 </b>
	 *
	 * <p> <b> 最终解析的是2个map, (key是url, List(注解信息)) 还有 (注解信息对象, method封装的对象), 2个map一起来完成解析执行</b>
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/echo")
//	@GetMapping("/echo")
	@ResponseBody
	public String echo(String msg) {

		System.out.println("===>url echo: " + msg);
		return echoService.echo(msg);
	}

	/**
	 * Post http://127.0.0.1:8272/file
	 * form-data 形式上传一个文件, 参数名是file, 类型是文件, 文件不要太大, 小的就行
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/file")
	@ResponseBody
	public String testFile(MultipartFile file) {

		System.out.println("url /file");
		return file.getSize() + " " + file.getOriginalFilename();
	}


	/**
	 * <p> 重点
	 * 异步响应还要考虑写入response的阻塞问题 应该是大响应体了 加个线程阻塞写入得了
	 *
	 * 哪怕是返回值使用了 DeferredResult，依然是一个阻塞写入 用的是tomcat线程，除非使用 3.1 中的WriteListener, 避免慢客户端吃线程
	 *
	 * 如果担心别人用慢速客户端来攻击响应, Nginx 默认会开启代理缓冲（proxy_buffering on;）注意 proxy_max_temp_file_size 大小
	 * 但是这开启有个缺点,
	 * 拖慢前端 SSR (服务端渲染) 的首字节时间 (TTFB),
	 * Server-Sent Events (SSE)会变卡顿,
	 * 大文件下载引发磁盘 I/O 飙升 (当大文件塞满 Nginx 的内存缓冲区，而慢客户端又没来得及消费完时，Nginx 就会把多余的数据溢写到本地磁盘的临时文件中（默认路径通常是 proxy_temp_path）。大量并发的大文件下载会导致疯狂的磁盘读写)
	 *
	 * 可以通过在接口添加 X-Accel-Buffering=no的响应header，来告诉nginx不要对响应数据进行缓存
	 *
	 * 如果是sse, 由担心慢速的话, 可以限制单ip的连接数还有发送超时和读取超时 send_timeout 10s; client_body_timeout 10s;
	 */
	@GetMapping("/api/download/nio")
	public void downloadViaNio(HttpServletRequest request, HttpServletResponse response) throws IOException {

		/**
		 * 应用不需要主动去推数据并死等，而是注册一个回调。
		 * 当底层的 TCP 发送缓冲区有空闲空间时，容器（Tomcat）会触发回调通知应用：“现在可以写了”，应用才去写一点数据，写满缓冲区后立即返回，交出线程控制权。
		 *
		 * 因为 Spring MVC 的 Controller 层高度封装了响应模型，要使用原生的 WriteListener，通常需要绕过 Spring MVC 的序列化机制，直接操作 HttpServletResponse
		 */

		// 1. 开启异步上下文，释放 Tomcat 工作线程
		AsyncContext asyncContext = request.startAsync();
		// 设置一个合理的异步超时时间 ，防止意外死锁
		asyncContext.setTimeout(60_000L);

		ServletOutputStream outputStream = response.getOutputStream();

		// todo 这个路径针对性的改下, 可以用大文件
		InputStream inputStream = Files.newInputStream(Paths.get("/Users/xjd/Desktop/study/javaframework/spring/spring-webmvc/src/test/resources/log4j2-test.xml"));

		// 2. 注册 WriteListener
		outputStream.setWriteListener(new WriteListener() {
			final byte[] buffer = new byte[8192];

			@Override
			public void onWritePossible() throws IOException {
				// 当且仅当底层网络缓冲区有空间时，容器才会调用此方法
				// isReady() 必须被检查，它不仅返回当前是否可写，还会重置内部的读写状态
				while (outputStream.isReady()) {
					int read = inputStream.read(buffer);
					if (read != -1) {
						// 这里的 write 保证是纯非阻塞的
						outputStream.write(buffer, 0, read);
					} else {
						// 文件读完，完成响应
						inputStream.close();
						asyncContext.complete();
						return;
					}
				}
				// 如果退出循环但文件没读完，说明 outputStream.isReady() 返回了 false (缓冲区满了)
				// 此时直接结束方法，释放当前线程。当缓冲区再次空闲时，容器会再次调用 onWritePossible()
			}

			@Override
			public void onError(Throwable t) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// ignore
				}
				asyncContext.complete();
				t.printStackTrace();
			}
		});
	}


}

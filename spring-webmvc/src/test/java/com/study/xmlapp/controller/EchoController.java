package com.study.xmlapp.controller;


import com.study.xmlapp.service.EchoService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;


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

		System.out.println("url echo: " + msg);
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


}
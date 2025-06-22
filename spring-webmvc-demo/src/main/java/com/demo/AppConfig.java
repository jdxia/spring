package com.demo;

import com.demo.controller.ZhouyuHandlerExceptionResolver;
import com.demo.controller.ZhouyuHandlerInterceptor1;
import com.demo.controller.ZhouyuHandlerInterceptor2;
import com.demo.controller.ZhouyuWebHandlerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.MappedInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.web.servlet.function.RouterFunctions.route;


@Configuration
@ComponentScan("com.demo")  // 修正包扫描路径，从com.zhouyu改为com.demo
@EnableWebMvc
public class AppConfig {

	@Bean
	public MappedInterceptor mappedInterceptor() {
		return new MappedInterceptor(new String[]{"/test"}, new ZhouyuHandlerInterceptor1());
	}

//	@Bean
//	public RouterFunction<ServerResponse> routerFunction() {
//		return route()
//				.GET("/app/person", request -> ServerResponse.ok().body("hello route user"))
//				.POST("/app/person", request -> ServerResponse.ok().body("hello route user"))
//				.build();
//	}



	@Bean
	public WebMvcConfigurer webMvcConfigurer() {
		return new WebMvcConfigurer() {

			@Override
			public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
				resolvers.add(new ZhouyuHandlerExceptionResolver());
			}

			@Override
			public void addInterceptors(InterceptorRegistry registry) {
//				registry.addInterceptor(new ZhouyuHandlerInterceptor1());
				registry.addWebRequestInterceptor(new ZhouyuWebHandlerInterceptor());
			}

			@Override
			public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
				StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
				stringHttpMessageConverter.setDefaultCharset(StandardCharsets.UTF_8);

				converters.add(1, stringHttpMessageConverter);
			}

//			@Override
//			public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//				MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
//				converters.add(jackson2HttpMessageConverter);
//			}
		};
	}

}

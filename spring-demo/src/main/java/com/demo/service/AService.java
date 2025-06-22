package com.demo.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


@Component
public class AService {

	private BService bService;

	@Lazy
	public AService(BService bService) {
		this.bService = bService;
	}

	public void test() {
		System.out.println(bService);
	}
}

package com.demo.controller;

import com.demo.User;
import com.demo.service.ZhouyuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Controller
public class ZhouyuController {

	@Autowired
	private ZhouyuService zhouyuService;

	@RequestMapping(method = RequestMethod.GET, path = "/test")
	@ResponseBody
	public String test() {
		throw new RuntimeException();
//		return "test";
	}

	@RequestMapping(method = RequestMethod.GET, path = "/index")
	@ResponseBody
	public String test1() {
		return "index";
	}


//	@RequestMapping(method = RequestMethod.POST, path = "/upload")
//	public long test1(@RequestPart("file") MultipartFile file, @RequestPart("name") String name) {
//		System.out.println(name);
//		return file.getSize();
//	}
//
//	@GetMapping("/index")
//	public String index() {
//		return "/jsp/index.jsp";
//	}
}

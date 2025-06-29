package com.study.xmlapp.service;

import org.springframework.stereotype.Service;


@Service
public class EchoService {

	public String echo(String msg){
		return "ECHO["+msg+"]";
	}
}

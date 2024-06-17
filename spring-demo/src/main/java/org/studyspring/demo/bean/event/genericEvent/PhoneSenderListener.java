package org.studyspring.demo.bean.event.genericEvent;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.studyspring.demo.bean.event.normalEvent.UserData;

@Component
public class PhoneSenderListener {


	//	@Async
	@EventListener
	@Order(10)
	public void onRegisterSuccess(BaseEvent<UserData> event) {
		System.out.println("============== event listen: " + event.getData().getName());
		System.out.println("监听到用户注册成功！发送号码中。。。");
	}
}

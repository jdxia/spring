package org.studyspring.demo.bean.event.genericEvent;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.studyspring.demo.bean.event.normalEvent.UserData;

@Component
public class EmailSenderListener {

//	@Async
	@EventListener
	@Order(15)
	public void onRegisterSuccess(BaseEvent<UserData> event) {
		System.out.println("============== event listen: " + event.getData().getName());
		System.out.println("监听到用户注册成功！发送邮件中。。。");
	}
}

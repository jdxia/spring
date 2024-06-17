package org.studyspring.demo.bean.event.genericEvent;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import org.studyspring.demo.bean.event.normalEvent.TestEvent;
import org.studyspring.demo.bean.event.normalEvent.UserData;

@Component
public class RegisterService implements ApplicationEventPublisherAware {

	ApplicationEventPublisher publisher;

	public void register(String username) {
		// 用户注册的动作。。。
		System.out.println(username + "注册成功。。。");
		// 发布事件
		UserData userData = new UserData();
		userData.setName(username);

		publisher.publishEvent(new BaseEvent<UserData>(userData));

		System.out.println("======");
	}

	public void testEvent(String username) {
		// 用户注册的动作。。。
		System.out.println(username + "注册成功。。。");
		// 发布事件
		TestEvent testEvent = new TestEvent(this, "111");
		publisher.publishEvent(testEvent);


		System.out.println("======");
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}
}
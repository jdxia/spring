package org.studyspring.demo.bean.event.normalEvent;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component("testEventListener")
public class TestEventListener implements ApplicationListener<TestEvent> {


	@Override
	public void onApplicationEvent(TestEvent event) {
		System.out.println("============== event listen: " + event.getMsg());
		System.out.println("==========> TestEvent成功!!!");
	}
}

package org.studyspring.demo.bean.event.normalEvent;


import org.springframework.context.ApplicationEvent;

import java.util.Objects;

public class TestEvent extends ApplicationEvent {

	private String msg;


	public TestEvent(Object source, String message) {
		super(source);
		this.msg = message;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		TestEvent testEvent = (TestEvent) o;
		return Objects.equals(msg, testEvent.msg);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(msg);
	}

	@Override
	public String toString() {
		return "TestEvent{" +
				"msg='" + msg + '\'' +
				'}';
	}
}

package org.studyspring.demo.bean.transaction;


import org.springframework.context.ApplicationEvent;

import java.util.Objects;

public class UserInsertEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private final Integer userId;

	public UserInsertEvent(Integer userId) {
		super(userId);
		this.userId = userId;
	}

	public Integer getUserId() {
		return userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserInsertEvent that = (UserInsertEvent) o;
		return Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(userId);
	}

	@Override
	public String toString() {
		return "UserInsertEvent{" +
				"userId=" + userId +
				'}';
	}
}

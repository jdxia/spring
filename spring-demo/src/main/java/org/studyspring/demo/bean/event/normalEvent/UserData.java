package org.studyspring.demo.bean.event.normalEvent;


import java.util.Objects;

public class UserData {
	private Long id;
	private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserData userData = (UserData) o;
		return Objects.equals(id, userData.id) && Objects.equals(name, userData.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}

	@Override
	public String toString() {
		return "UserData{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}
}

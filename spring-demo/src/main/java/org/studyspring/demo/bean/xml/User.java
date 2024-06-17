package org.studyspring.demo.bean.xml;


import org.springframework.beans.factory.InitializingBean;

public class User implements InitializingBean {
	private Integer id;
	private String name;

	public User() {
		System.out.println("User create");
	}

	public User(Integer id, String name) {
		this.id = id;
		this.name = name;
		System.out.println("User create all args");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("bean User afterPropertiesSet");
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
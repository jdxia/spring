package org.studyspring.demo.bean.factory;

import java.util.Objects;

public class Order {
	private Long id;
	private String name;
	private String desc;

	public Order(Long id, String name, String desc) {
		this.id = id;
		this.name = name;
		this.desc = desc;
	}

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

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Order order = (Order) o;
		return Objects.equals(id, order.id) && Objects.equals(name, order.name) && Objects.equals(desc, order.desc);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, desc);
	}

	@Override
	public String toString() {
		return "Order{" +
				"id=" + id +
				", name='" + name + '\'' +
				", desc='" + desc + '\'' +
				'}';
	}
}

package org.studyspring.demo.bean.factory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component
public class OrderFactoryBean implements FactoryBean<Order> {
	@Override
	public Order getObject() throws Exception {
		return new Order(1L, "order1", "order1 desc");
	}

	@Override
	public Class<Order> getObjectType() {
		return Order.class;
	}
}

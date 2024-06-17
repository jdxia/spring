package org.studyspring.demo.bean.event.genericEvent;

import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import java.util.Objects;


/**
 *  ResolvableTypeProvider 解决泛型擦除
 */
class BaseEvent<T> implements ResolvableTypeProvider {
	private T data;

	public BaseEvent(T data) {
		this.data = data;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BaseEvent<?> baseEvent = (BaseEvent<?>) o;
		return Objects.equals(data, baseEvent.data);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(data);
	}

	@Override
	public String toString() {
		return "BaseEvent{" +
				"data=" + data +
				'}';
	}

	/**
	 *  java泛型是 声明侧泛型保留, 使用侧泛型擦除
	 *  看字节码: https://www.yuque.com/jdxia/jvm/naocetl0k4gp9nl0
	 *  Code 里面的 LocalVariableTypeTable 里面的 Signature 会保留
	 */
	@Override
	public ResolvableType getResolvableType() {
		return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(getData()));
	}

}

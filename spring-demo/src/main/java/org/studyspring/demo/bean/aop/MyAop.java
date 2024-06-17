package org.studyspring.demo.bean.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)  // 表明该注解用于方法
@Retention(RetentionPolicy.RUNTIME)  // 表明该注解在运行时可用
public @interface MyAop {
}

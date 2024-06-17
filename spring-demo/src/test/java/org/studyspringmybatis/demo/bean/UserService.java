package org.studyspringmybatis.demo.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.studyspringmybatis.demo.mybatis.entity.User;
import org.studyspringmybatis.demo.mybatis.mapper.UserMapper;

import java.util.List;

@Component
public class UserService {

	/**
	 * 想办法 让 userMapper代理对象 成为一个 Bean
	 * 用 FactoryBean生成
	 */
	@Autowired
	private UserMapper userMapper;

	public void testMybatisMapper() {
		List<User> res = userMapper.findAll();

		if (CollectionUtils.isEmpty(res)) {
			System.out.println("查询结果为空");
		} else {
			res.forEach(System.out::println);
		}
	}
}

package org.studyspringmybatis.demo.mybatis.mapper;


import org.apache.ibatis.annotations.Select;
import org.studyspringmybatis.demo.mybatis.entity.User;

import java.util.List;

public interface UserMapper {

	@Select("select * from user")
	List<User> findAll();

}


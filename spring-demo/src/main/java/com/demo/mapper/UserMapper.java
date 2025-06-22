package com.demo.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface UserMapper {

	@Select("select 'zhouyu' from t1")
	String selectById();

	@Insert("insert into t1(id, a, b) values (#{id}, #{a}, #{b})")
	void insertOne(int id, int a, int b);
}

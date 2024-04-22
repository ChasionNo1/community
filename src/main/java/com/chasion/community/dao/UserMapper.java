package com.chasion.community.dao;

import com.chasion.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

//@Repository
@Mapper
public interface UserMapper {

    User selectById(int id);

    User selectByName(String name);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(@Param("id") int id, @Param("status")int status);

    int updateHeader(@Param("id")int id, @Param("headerUrl")String headerUrl);

    //Parameter 'password' not found. Available parameters are [arg1, arg0, param1
    int updatePassword(@Param("id")int id, @Param("password")String password);


}

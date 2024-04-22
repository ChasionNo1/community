package com.chasion.community.service;

import com.chasion.community.dao.UserMapper;
import com.chasion.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService  {

    @Autowired
    private UserMapper userMapper;

    // 根据用户id查询用户名
    public User findUserById(int id){
        return userMapper.selectById(id);
    }
}

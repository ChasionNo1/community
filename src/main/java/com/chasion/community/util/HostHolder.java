package com.chasion.community.util;

import com.chasion.community.entity.User;
import org.springframework.stereotype.Component;

@Component
public class HostHolder {

    // 持有用户信息，用于代替session对象
    private ThreadLocal<User> users = new ThreadLocal<>();
    public void setUser(User user) {
        users.set(user);
    }
    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }

}

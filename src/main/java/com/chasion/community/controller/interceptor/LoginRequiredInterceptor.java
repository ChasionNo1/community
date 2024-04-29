package com.chasion.community.controller.interceptor;

import com.chasion.community.annotation.LoginRequired;
import com.chasion.community.entity.User;
import com.chasion.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        User user = hostHolder.getUser();
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            /// A.isAnnotationPresent(B.class);
            // 大白话：B类型的注解是否在A类上。
            if (method.isAnnotationPresent(LoginRequired.class ) && user == null) {
                // 是这种类型的注解，且用户值为空，即：需要登录但没登录，进行拦截
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}

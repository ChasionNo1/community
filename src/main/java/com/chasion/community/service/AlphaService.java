package com.chasion.community.service;

import com.chasion.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//@Service
//// 单例single，多例
////@Scope("prototype")
//public class AlphaService {
//    @Autowired
//    private AlphaDao alphaDao;
//
//    public AlphaService(){
//        System.out.println("实例化alphaService");
//    }
//
//    // 在构造器之后调用
//    @PostConstruct
//    public void init() {
//        System.out.println("initial alphaService");
//    }
//
//    // 在销毁之前调用
//    @PreDestroy
//    public void destroy(){
//        System.out.println("destroy alphaService");
//    }
//
//    public String find(){
//        return alphaDao.select();
//    }
//}

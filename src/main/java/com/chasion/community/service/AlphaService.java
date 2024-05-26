package com.chasion.community.service;

import com.chasion.community.dao.AlphaDao;
import com.chasion.community.dao.DiscussPostMapper;
import com.chasion.community.dao.UserMapper;
import com.chasion.community.entity.DiscussPost;
import com.chasion.community.entity.User;
import com.chasion.community.util.CommunityUtil;
import org.apache.ibatis.annotations.Insert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

//@Service
//// 单例single，多例
////@Scope("prototype")
//public class AlphaService {
//    @Autowired
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

@Component
public class AlphaService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private static final Logger logger = LoggerFactory.getLogger(AlphaService.class);

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1(){
        // 新增用户
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(5));
        user.setPassword(CommunityUtil.Md5("123456789" + user.getSalt()));
        user.setHeaderUrl("http://www.alpha.com");
        user.setEmail("alpha@alpha.com");
        userMapper.insertUser(user);
        // 新增帖子
        DiscussPost post = new DiscussPost();
        post.setTitle("hello");
        post.setUserId(user.getId());
        post.setContent("this is a test");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        Integer.valueOf("abc");

        return "ok";

    }
    // 编程式注解
    public Object save2(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {

            @Override
            public Object doInTransaction(TransactionStatus transactionStatus) {
                // 新增用户
                User user = new User();
                user.setUsername("alpha1");
                user.setSalt(CommunityUtil.generateUUID().substring(5));
                user.setPassword(CommunityUtil.Md5("123456789" + user.getSalt()));
                user.setHeaderUrl("http://www.alpha.com");
                user.setEmail("alpha@alpha.com");
                userMapper.insertUser(user);
                // 新增帖子
                DiscussPost post = new DiscussPost();
                post.setTitle("hello");
                post.setUserId(user.getId());
                post.setContent("this is a test");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                Integer.valueOf("abc");
                return "OK";
            }
        });
    }

//    // 该方法在多线程环境下，被异步的调用
//    @Async
//    public void execute1(){
//        logger.debug("execute1");
//    }
//
//    // 定时执行
//    @Scheduled(initialDelay = 1000, fixedDelay = 1000)
//    public void execute2(){
//        logger.debug("execute2");
//    }
}

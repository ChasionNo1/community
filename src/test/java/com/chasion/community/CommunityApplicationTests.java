package com.chasion.community;

import com.chasion.community.dao.AlphaDao;
import com.chasion.community.dao.UserMapper;
import com.chasion.community.entity.User;
//import com.chasion.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelectById(){
        User user = userMapper.selectById(101);
        System.out.println(user);
    }

    @Test
    void contextLoads() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void testApplicationContext() {
        System.out.println(applicationContext);
        // 从容器中获取bean的方式
        // 当有多个接口实现类时，可以设置优先级
        AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);
        System.out.println(alphaDao.select());
        // 用名称来获取
        alphaDao = applicationContext.getBean("alphaDaoHibernateImp", AlphaDao.class);
        System.out.println(alphaDao.select());
    }

//    // 测试bean的管理方法
//    @Test
//    public void testBeanManagement(){
//        // 获取bean
//        AlphaService alphaService = applicationContext.getBean(AlphaService.class);
//        System.out.println(alphaService);
//
//    }

    // 获取第三方的bean
    @Test
    public void testBeanConfig(){
        SimpleDateFormat simpleDateFormat = applicationContext.getBean(SimpleDateFormat.class);
        System.out.println(simpleDateFormat.format(new Date()));
    }

    // 自动注入，指定名字注入
    @Autowired
    @Qualifier("alphaDaoHibernateImp")
    private AlphaDao alphaDao;

    @Test
    public void testDI(){
        System.out.println(alphaDao.select());
    }
}

package com.chasion.community;

import com.chasion.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTests {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTests.class);

    // jdk普通线程池
    private ExecutorService threadPool = Executors.newFixedThreadPool(5);
    // jdk可执行定时任务的线程池
    private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
    // spring 普通线程池
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    // spring 定时任务线程池
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Autowired
    private AlphaService alphaService;

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // 1、jdk普通线程池
    @Test
    public void testExecutorService(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello executor service");
            }
        };
        for (int i = 0; i < 10; i++) {
            threadPool.submit(task);
        }

        sleep(10000);
    }

    // 2、jdk定时任务线程池
    @Test
    public void testScheduledExecutorService(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello schedule executor service");
            }
        };
        // 反复执行
        scheduledThreadPool.scheduleAtFixedRate(task, 10000, 1000, TimeUnit.MICROSECONDS);
        sleep(30000);
    }

    // 3、spring普通线程池
    @Test
    public void testThreadPoolTaskExecutor(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello task executor service");
            }
        };
        for (int i = 0; i < 10; i++) {
            threadPoolTaskExecutor.submit(task);
        }
        sleep(10000);

    }

    // 4、spring定时任务线程池
    @Test
    public void testThreadPoolTaskScheduler(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello task schedule service");
            }
        };
        Date start = new Date(System.currentTimeMillis() + 10000);
        threadPoolTaskScheduler.scheduleAtFixedRate(task, start, 1000);
        sleep(30000);
    }

    // 5、spring普通线程池，使用注解的方式
    @Test
    public void testThreadPoolTaskExecutorSimple(){
        for (int i = 0; i < 10; i++) {
            alphaService.execute1();
        }
        sleep(10000);
    }

    // 6、spring定时任务简化
    @Test
    public void testThreadPoolTaskSchedulerSimple(){
        sleep(30000);
    }


}

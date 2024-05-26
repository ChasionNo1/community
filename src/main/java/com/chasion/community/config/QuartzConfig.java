package com.chasion.community.config;

import com.chasion.community.quartz.AlphaJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

// 配置 --> 数据库 --》调用
@Configuration
public class QuartzConfig {

    // FactoryBean 可简化bean的实例化过程：
    // 1、通过FactoryBean封装了bean的实例化过程
    // 2、将FactoryBean装配到spring容器中
    // 3、将FactoryBean注入给其他bean
    // 4、该bean得到的是FactoryBean所管理的对象

    // 配置job detail
    @Bean
    public JobDetailFactoryBean alphaJobDetail(){
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        // 设置参数
        jobDetailFactoryBean.setJobClass(AlphaJob.class);
        jobDetailFactoryBean.setName("alphaJob");
        jobDetailFactoryBean.setGroup("alphaJobGroup");
        // 任务持久保存
        jobDetailFactoryBean.setDurability(true);
        // 任务可恢复的
        jobDetailFactoryBean.setRequestsRecovery(true);
        return jobDetailFactoryBean;

    }
    // 配置 trigger （SimpleTriggerFactoryBean，CronTriggerFactoryBean）
    @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail jobDetail){
        SimpleTriggerFactoryBean simpleTriggerFactoryBean = new SimpleTriggerFactoryBean();
        simpleTriggerFactoryBean.setJobDetail(jobDetail);
        simpleTriggerFactoryBean.setName("alphaTrigger");
        simpleTriggerFactoryBean.setGroup("alphaTriggerGroup");
        simpleTriggerFactoryBean.setRepeatInterval(3000);
        simpleTriggerFactoryBean.setJobDataMap(new JobDataMap());

        return simpleTriggerFactoryBean;
    }
}

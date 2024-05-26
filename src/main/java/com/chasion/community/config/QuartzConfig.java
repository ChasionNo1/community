package com.chasion.community.config;

import com.chasion.community.quartz.AlphaJob;
import com.chasion.community.quartz.PostScoreRefreshJob;
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

    /**
     * 配置文件，两个方法返回bean，自动添加到数据库中，因此，如果不想这个alpha也输出，就得把这个bean注解去掉，且在数据库中删除
     * 运行一次后，将bean注入到容器中后，数据库中也添加了，后续使用就从数据库中取，因此，如果这个bean注入有误，就需要把数据库中的数据删除，再添加
     *
     * */

    // 配置job detail
//    @Bean
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
//    @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail){
        SimpleTriggerFactoryBean simpleTriggerFactoryBean = new SimpleTriggerFactoryBean();
        simpleTriggerFactoryBean.setJobDetail(alphaJobDetail);
        simpleTriggerFactoryBean.setName("alphaTrigger");
        simpleTriggerFactoryBean.setGroup("alphaTriggerGroup");
        simpleTriggerFactoryBean.setRepeatInterval(3000);
        simpleTriggerFactoryBean.setJobDataMap(new JobDataMap());

        return simpleTriggerFactoryBean;
    }

    // 刷新帖子分数任务
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail(){
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        // 设置参数
        jobDetailFactoryBean.setJobClass(PostScoreRefreshJob.class);
        jobDetailFactoryBean.setName("postScoreRefreshJob");
        jobDetailFactoryBean.setGroup("communityJobGroup");
        // 任务持久保存
        jobDetailFactoryBean.setDurability(true);
        // 任务可恢复的
        jobDetailFactoryBean.setRequestsRecovery(true);
        return jobDetailFactoryBean;
    }

    // 配置 trigger （SimpleTriggerFactoryBean，CronTriggerFactoryBean）
    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail){
        SimpleTriggerFactoryBean simpleTriggerFactoryBean = new SimpleTriggerFactoryBean();
        simpleTriggerFactoryBean.setJobDetail(postScoreRefreshJobDetail);
        simpleTriggerFactoryBean.setName("postScoreRefreshTrigger");
        simpleTriggerFactoryBean.setGroup("communityTriggerGroup");
        // 5分钟刷新一下
        simpleTriggerFactoryBean.setRepeatInterval(1000 * 60 * 2);
        simpleTriggerFactoryBean.setJobDataMap(new JobDataMap());

        return simpleTriggerFactoryBean;
    }
}

package com.chasion.community;

import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class QuartzTests {

    @Autowired
    private Scheduler scheduler;

    @Test
    public void testDeleteJob() throws SchedulerException {
        System.out.println(scheduler.deleteJob(new JobKey("postScoreRefreshJob", "communityJobGroup")));
    }

    @Test
    public void testDeleteAlphaJob() throws SchedulerException {
        System.out.println(scheduler.deleteJob(new JobKey("alphaJob", "alphaJobGroup")));
    }
}

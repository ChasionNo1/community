package com.chasion.community;

import com.chasion.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class TransactionTests {

    @Autowired
    private AlphaService alphaService;

    @Test
    public void testAlphaService() {
        Object o = alphaService.save1();
        System.out.println(o);
    }
    @Test
    public void testAlphaService2() {
        Object o = alphaService.save2();
        System.out.println(o);
    }
}

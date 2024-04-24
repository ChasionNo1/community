package com.chasion.community;

import com.chasion.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSender;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testSendMail() {
        mailClient.sendMail("185897780@qq.com", "注册邮件", "欢迎您注册本网站");
    }

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username", "chasion");
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);
        mailClient.sendMail("185897780@qq.com", "注册邮件", content);
    }
}

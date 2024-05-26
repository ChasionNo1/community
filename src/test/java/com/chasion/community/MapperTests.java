package com.chasion.community;

import com.chasion.community.dao.DiscussPostMapper;
import com.chasion.community.dao.LoginTicketMapper;
import com.chasion.community.dao.MessageMapper;
import com.chasion.community.dao.UserMapper;
import com.chasion.community.entity.DiscussPost;
import com.chasion.community.entity.LoginTicket;
import com.chasion.community.entity.Message;
import com.chasion.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectById() {
        User user = userMapper.selectById(101);
        System.out.println(user);
    }

    @Test
    public void testSelectByName(){
        User user = userMapper.selectByName("liubei");
        System.out.println(user);
    }

    @Test
    public void testSelectByEmail(){
        User user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setEmail("test@sina.com");
        user.setSalt("abc");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdateUser(){
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "11111");
        System.out.println(rows);
    }

    @Test
    public void testDiscussPostMapper(){
        System.out.println(discussPostMapper.selectDiscussPostRows(0));

        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPost(0, 0, 10, 0);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }
    }


    @Test
    public void testLoginTicketMapper(){

        // 测试添加
//        LoginTicket loginTicket = new LoginTicket();
//        loginTicket.setTicket("asadasgc");
//        loginTicket.setStatus(1);
//        loginTicket.setUserId(150);
//        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24));
//        loginTicketMapper.insertLoginTicket(loginTicket);

        // 测试查询
        LoginTicket loginTicket1 = loginTicketMapper.selectLoginTicket("asadasgc");
        System.out.println(loginTicket1);

        // 测试修改
        int i = loginTicketMapper.updateLoginTicket("asadasgc", 0);
        System.out.println(i);
    }

    @Test
    public void testMessageMapper(){
        List<Message> conversations = messageMapper.getConversations(111, 0, 10);
        for (Message message :
                conversations) {
            System.out.println(message);
        }

        int conversationCount = messageMapper.getConversationCount(111);
        System.out.println(conversationCount);

        int letterCount = messageMapper.getLetterCount("111_112");
        System.out.println(letterCount);

        List<Message> letters = messageMapper.getLetters("111_112", 0, 10);
        for (Message letter :
                letters) {
            System.out.println(letter);
        }

        System.out.println(messageMapper.getUnreadLetterCount(111, null));
    }




}

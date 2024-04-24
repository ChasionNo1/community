package com.chasion.community.service;

import com.chasion.community.dao.LoginTicketMapper;
import com.chasion.community.dao.UserMapper;
import com.chasion.community.entity.LoginTicket;
import com.chasion.community.entity.User;
import com.chasion.community.util.CommunityConstant;
import com.chasion.community.util.CommunityUtil;
import com.chasion.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sun.security.krb5.internal.Ticket;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService  implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

//    @Value("${server.servlet.context-path}")
//    private String contextPath;

    // 根据用户id查询用户名
    public User findUserById(int id){
        return userMapper.selectById(id);
    }


    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<String,Object>();
        // 参数校验
        if (user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMessage", "用户名不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMessage", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMessage", "邮箱不能为空!");
            return map;
        }
        // 用户是否已注册
        User u = userMapper.selectByName(user.getUsername());
        if (u != null){
            map.put("usernameMessage", "该账号已存在!");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null){
            map.put("emailMessage","该邮箱已被存在!");
            return map;
        }
        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.Md5(user.getPassword() + user.getSalt()));
        user.setCreateTime(new Date());
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        userMapper.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        context.setVariable("url",domain + "/activation/" + user.getId() + "/" + user.getActivationCode());
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;
    }

    public int activation(int userId, String activationCode){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return REGISTER_REPEAT;
        }else if(user.getActivationCode().equals(activationCode)){
            userMapper.updateStatus(userId, 1);
            return REGISTER_SUCCESS;
        }else {
            return REGISTER_FAILURE;
        }
    }

    // 登录行为
    public Map<String, Object> login(String username, String password, long expired){
        HashMap<String, Object> map = new HashMap<>();
        // 校验
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg", "账号不能为空");
            return map;
        }else if (StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        // 登录失败
        // 登录成功
        User user = userMapper.selectByName(username);
        // 用户是否存在？
        if (user == null){
            map.put("usernameMsg", "用户不存在");
            return map;
        }else {
            // 用户存在
            // 是否激活
            if (user.getStatus() == 0){
                map.put("usernameMsg", "账号未激活");
                return map;
            }
            // 对比密码，密码是如何设置的？
            //  user.setPassword(CommunityUtil.Md5(user.getPassword() + user.getSalt()));
            // 用户输入的是明文密码
            String inputPassword = CommunityUtil.Md5(password + user.getSalt());
            String savePassword = user.getPassword();
            System.out.println("inputPassword:"+inputPassword);
            System.out.println("savePassword:"+savePassword);
            if (!savePassword.equals(inputPassword)){
                // 密码不正确
                map.put("passwordMsg", "密码不正确");
                return map;
            }else {
                map.put("passwordMsg", "密码正确");
                // 登录成功，设置登录凭证
                LoginTicket loginTicket = new LoginTicket();
                loginTicket.setUserId(user.getId());
                loginTicket.setStatus(1);
                loginTicket.setTicket(CommunityUtil.generateUUID());
                loginTicket.setExpired(new Date(System.currentTimeMillis() + expired));
                loginTicketMapper.insertLoginTicket(loginTicket);
                // 给客户发送ticket
                map.put("ticket", loginTicket.getTicket());
            }
        }
        return map;
    }

    // 退出登录
    public void logout(String ticket){
        loginTicketMapper.updateLoginTicket(ticket, 1);
    }
}

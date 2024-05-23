package com.chasion.community.controller;

import com.chasion.community.entity.User;
import com.chasion.community.service.UserService;
import com.chasion.community.util.CommunityConstant;
import com.chasion.community.util.CommunityUtil;
import com.chasion.community.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @Autowired
    private Producer captchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);


    // 获取注册页面
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    // 获取登录页面
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    // 响应注册请求
    @RequestMapping(path = "register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活!");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        }else {
            model.addAttribute("usernameMsg", map.get("usernameMessage"));
            model.addAttribute("passwordMsg", map.get("passwordMessage"));
            model.addAttribute("emailMsg", map.get("emailMessage"));
            return "/site/register";
        }
    }
    // 激活链接请求
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String actvation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int activationState = userService.activation(userId, code);
        if (activationState == REGISTER_SUCCESS){
            model.addAttribute("msg", "激活成功，您的账号可以正常使用了!");
            model.addAttribute("target", "/login");

        }else if (activationState == REGISTER_REPEAT){
            model.addAttribute("msg", "无效操作，该账号已经激活!");
            model.addAttribute("target", "/index");
        }else {
            model.addAttribute("msg", "激活失败，激活码有误!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    // 获取验证码
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        // 生成验证码
        String text = captchaProducer.createText();
        BufferedImage image = captchaProducer.createImage(text);

        // 将验证码存入session
//        session.setAttribute("captcha", text);
        // 重构验证码存放的位置，放到redis里
        // 需要临时凭证
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath("/");
        response.addCookie(cookie);
        // 将验证码存入redis中
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        }catch (IOException e){
            logger.error("响应验证码失败:" + e.getMessage());
        }

    }

    // 处理登录请求
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(Model model, String username, String password, String code, boolean rememberMe,
                        HttpSession session, HttpServletResponse response, @CookieValue("kaptchaOwner") String kaptchaOwner) {
        // 验证码在客户获取登录页面的时候加载，信息已经存入到session中，
        // 此时需要将客户从前端页面中输入的验证码和session中取到的验证码进行对比
//        String serverKaptcha = (String)session.getAttribute("captcha");
        // 重构：从redis里取验证码
        String serverKaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)){
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            serverKaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }


        // 验证码不正确，页面需要回填数据，重新填写验证码即可
        if (StringUtils.isBlank(serverKaptcha) || StringUtils.isBlank(code) || !serverKaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg", "验证码不正确");
            return "/site/login";
        }

        // 账号，密码验证
        int expired = rememberMe ? CommunityConstant.REMEMBER_EXPIRATION_TIME : CommunityConstant.DEFAULT_EXPIRATION_TIME;
        Map<String, Object> map = userService.login(username, password, expired);
        if (map.containsKey("ticket")){
            // 登录 成功
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath("/");
            cookie.setMaxAge(expired);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }

    }

    // 退出登录
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

}

package com.chasion.community.controller;

import com.chasion.community.annotation.LoginRequired;
import com.chasion.community.entity.User;
import com.chasion.community.service.UserService;
import com.chasion.community.util.CommunityUtil;
import com.chasion.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    // 响应设置页面
    @LoginRequired
    @RequestMapping(path = "/user/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        return "/site/setting";
    }


    // 上传文件
    @LoginRequired
    @RequestMapping(value = "/user/upload", method = RequestMethod.POST)
    public String upload(@RequestParam("headerImage") MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "Please select a file.");
            return "/site/setting";
        }
        // 获取用户上传的文件名，只是取后缀用
        String filename = headerImage.getOriginalFilename();
        assert filename != null;
        String suffix = filename.substring(filename.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error", "文件格式不正确.");
            return "/site/setting";
        }

        // 生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放路径
        try {
            // 文件不存在加一个判断，再创建
            File file = new File(uploadPath + '/' + filename);
            // 写入数据
            headerImage.transferTo(file);
        }catch (IOException e){
            logger.error("上传文件失败:{}", e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常", e);
        }

        // 更新当前用户头像路径
        // 相对路径
        // domain/user/header/xxx.
        User user = hostHolder.getUser();
        String headerUrl = domain + "/" + "user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";

    }

    // 获取头像
    @RequestMapping(path = "/user/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable String filename, HttpServletResponse response) {
        // 找到服务器存放的路径
        filename = uploadPath + "/" + filename;
        // 文件的后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);

        try (OutputStream os = response.getOutputStream();
             FileInputStream fis = new FileInputStream(filename);)
        {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1){
                os.write(buffer, 0, b);
            }
        }catch (IOException e){
            logger.error("读取头像失败:{}", e.getMessage());
        }
    }

    // 更改密码
    @LoginRequired
    @RequestMapping(path = "/user/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, String confirmPassword, Model model, @CookieValue("ticket") String ticket){
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword, confirmPassword);
        if (!map.isEmpty()){
            Set<String> keys = map.keySet();
            Iterator<String> iterator = keys.iterator();
            while (iterator.hasNext()){
                String key = iterator.next();
                System.out.println(key + ":" + map.get(key));
            }
        }
        if (map.isEmpty()){
            // 没有错误，修改成功
            // 退出登录
            userService.logout(ticket);
            return "redirect:/login";
        }else {
            model.addAttribute("oldMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newMsg", map.get("newPasswordMsg"));
            model.addAttribute("confirmMsg", map.get("confirmPasswordMsg"));
            // 重定向是两次请求，不携带数据
//            return "forward:/site/setting";
            return "/site/setting";
        }
    }

}

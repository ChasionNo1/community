package com.chasion.community.controller;

import com.chasion.community.annotation.LoginRequired;
import com.chasion.community.entity.Comment;
import com.chasion.community.entity.DiscussPost;
import com.chasion.community.entity.Page;
import com.chasion.community.entity.User;
import com.chasion.community.service.*;
import com.chasion.community.util.CommunityConstant;
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
import java.util.*;

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

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

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

    // 个人主页
    @RequestMapping(path = "/user/profile/{userId}", method = RequestMethod.GET)
    public String getProfile(Model model, @PathVariable int userId) {
        // 需要传入，用户信息：头像、用户名、注册时间、关注了几个人、关注者、获得了多少赞
        User user = userService.findUserById(userId);
        model.addAttribute("user", user);
        if (user == null){
            throw new RuntimeException("该用户不存在");
        }
        int userLikeCount = likeService.getUserLikeCount(userId);
        model.addAttribute("userLikeCount", userLikeCount);
        // 关注数量
        long followeeCount = followService.getFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        // 粉丝数量
        long followerCount = followService.getFollowerCount(CommunityConstant.ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        // 是否关注
        boolean followed = false;
        if (hostHolder.getUser() != null){
            followed = followService.isFollowed(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER, userId);

        }
        model.addAttribute("followed", followed);


        return "/site/profile";
    }

    /**
     * 获取个人发布的帖子列表，支持分页
     * 需要的信息有：
     * 1、发布的帖子总数
     * 2、每条帖子的标题和内容，获得的赞和发布时间
     *
     * */
    @RequestMapping(path = "/user/discussPost/{userId}", method = RequestMethod.GET)
    public String getMyPostList(Model model, @PathVariable("userId") int userId, Page page) {
        User user = userService.findUserById(userId);
        model.addAttribute("user", user);
        // 设置分页信息
        int discussPostRows = discussPostService.findDiscussPostRows(userId);
        model.addAttribute("discussPostRows", discussPostRows);
        page.setRows(discussPostRows);
        page.setLimit(10);
        page.setPath("/user/discussPost" + userId);
        // 查找某人发布过的帖子
        List<DiscussPost> postList = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit());
        // 封装volist
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        if (!postList.isEmpty()){
            for (DiscussPost post : postList){
                HashMap<String, Object> map = new HashMap<>();
                map.put("id", post.getId());
                map.put("title", post.getTitle());
                map.put("content", post.getContent());
                map.put("createTime", post.getCreateTime());
                long likeCount = likeService.getEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                list.add(map);
            }
        }
        model.addAttribute("postList", list);

        return "/site/my-post";
    }

    /**
     * 回复列表：支持分页
     * 需要的信息有：
     * 回复的帖子总数
     * 回复帖子的id，回复帖子的标题，回复的内容，回复的时间
     *
     *
     * */
    @RequestMapping(path = "/user/reply/{userId}", method = RequestMethod.GET)
    public String getMyReplyList(Model model, @PathVariable("userId") int userId, Page page) {
        User user = userService.findUserById(userId);
        model.addAttribute("user", user);
        page.setPath("/user/reply" + userId);
        page.setLimit(10);
        // 对帖子的评论
        int commentCount = commentService.getCommentCountByUserId(CommunityConstant.ENTITY_TYPE_POST, userId);
        model.addAttribute("commentCount", commentCount);
        page.setRows(commentCount);
        List<Comment> comments = commentService.getCommentsByUserId(CommunityConstant.ENTITY_TYPE_POST, userId, page.getOffset(), page.getLimit());
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        if (!comments.isEmpty()){
            for (Comment comment : comments){
                HashMap<String, Object> map = new HashMap<>();
                map.put("id", comment.getEntityId());
                map.put("content", comment.getContent());
                map.put("createTime", comment.getCreateTime());
                DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
                map.put("title", post.getTitle());
                list.add(map);
            }
        }
        model.addAttribute("commentList", list);
        return "/site/my-reply";
    }


}

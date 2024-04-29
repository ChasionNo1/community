package com.chasion.community.controller;

import com.chasion.community.entity.Comment;
import com.chasion.community.entity.DiscussPost;
import com.chasion.community.entity.Page;
import com.chasion.community.entity.User;
import com.chasion.community.service.CommentService;
import com.chasion.community.service.DiscussPostService;
import com.chasion.community.service.UserService;
import com.chasion.community.util.CommunityConstant;
import com.chasion.community.util.CommunityUtil;
import com.chasion.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    // 增加帖子，返回json字符串
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        // 封装discussPost
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "您还没有登录");
        }
        DiscussPost discussPost = new DiscussPost();
        // 获取作者的id
        int userId = user.getId();
        discussPost.setUserId(userId);
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);
        return CommunityUtil.getJSONString(0, "发布成功");

    }
    // 获取帖子详情页面
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String getDiscussPostDetail(@PathVariable int id, Model model, Page page) {
        // 显示帖子的内容: 标题
        DiscussPost post = discussPostService.findDiscussPostById(id);
        if (post == null) {
            return CommunityUtil.getJSONString(404, "post is not found");
        }
        int userId = post.getUserId();
        // 一般说来这儿是不为空的
        User user = userService.findUserById(userId);
        model.addAttribute("post", post);
        model.addAttribute("user", user);

        // 设置评论分页
        page.setLimit(5);
        page.setPath("/discuss/" + id);
        page.setRows(post.getCommentCount());

        // 获取帖子的评论列表
        List<Comment> commentList = commentService.getComments(CommunityConstant.ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 封装信息，评论者信息，评论内容信息，以及评论的回复信息
        List<Map<String, Object>> commentVolist = new ArrayList<Map<String, Object>>();
        if (commentList != null && !commentList.isEmpty()) {
            for (Comment comment : commentList) {
                HashMap<String, Object> commentVo = new HashMap<>();
                // 评论内容
                commentVo.put("comment", comment);
                // 评论者
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                // 评论的回复信息
                // 查询所有回复信息
                List<Comment> replyList = commentService.getComments(CommunityConstant.ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 有对帖子的comment，有对comment的comment
                // 回复的Vo列表
                List<Map<String, Object>> replyVolist = new ArrayList<>();
                if (replyList != null && !replyList.isEmpty()) {
                    for (Comment reply : replyList) {
                        HashMap<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply);
//                        System.out.println("reply = " + reply.getContent());
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 回复目标, targetId ---> target user id
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
//                        System.out.println("target id: " + reply.getTargetId());
                        replyVo.put("target", target);
                        replyVolist.add(replyVo);
                    }
                }
                commentVo.put("replies", replyVolist);
                // 回复数量
                int replyCount = commentService.getCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);
                commentVolist.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVolist);

        return "/site/discuss-detail";
    }
}

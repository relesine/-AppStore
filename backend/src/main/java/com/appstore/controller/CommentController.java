package com.appstore.controller;

import com.appstore.common.Result;
import com.appstore.entity.Comment;
import com.appstore.service.CommentService;
import com.appstore.vo.CommentVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @GetMapping("/list/{appId}")
    public Result<List<CommentVO>> getComments(@PathVariable Long appId) {
        return Result.success(commentService.getCommentsByAppId(appId));
    }

    @PostMapping("/add")
    public Result<Void> addComment(@RequestBody Comment comment) {
        commentService.addComment(comment);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success();
    }
}

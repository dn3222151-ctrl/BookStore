package com.example.bookstore.Controller;

import com.example.bookstore.Entity.ForumComment;
import com.example.bookstore.Entity.ForumPost;
import com.example.bookstore.Entity.User;
import com.example.bookstore.Repository.ForumCommentRepository;
import com.example.bookstore.Repository.ForumPostRepository;
import com.example.bookstore.Service.CultivationHelper;
import com.example.bookstore.Service.OrdersService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/forum")
public class ForumController {

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private ForumCommentRepository forumCommentRepository;

    @Autowired
    private OrdersService ordersService;

    @GetMapping
    @Transactional(readOnly = true)
    public String viewForum(Model model, HttpSession session) {
        List<ForumPost> posts = forumPostRepository.findAllByOrderByCreatedAtDesc();
        Map<Integer, CultivationHelper.RealmInfo> userRealmMap = new HashMap<>();

        for (ForumPost p : posts) {
            userRealmMap.computeIfAbsent(p.getUser().getId(),
                    id -> CultivationHelper.calculateForUser(p.getUser(), ordersService.getOrdersByUserId(id)));
            for (ForumComment c : p.getComments()) {
                userRealmMap.computeIfAbsent(c.getUser().getId(),
                        id -> CultivationHelper.calculateForUser(c.getUser(), ordersService.getOrdersByUserId(id)));
            }
        }

        model.addAttribute("posts", posts);
        model.addAttribute("userRealmMap", userRealmMap);
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "Forum";
    }

    @PostMapping("/post")
    public String createPost(@RequestParam("title") String title,
                             @RequestParam("content") String content,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login?required=true";
        }

        if (title.trim().isEmpty() || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tiêu đề và nội dung không được để trống!");
            return "redirect:/forum";
        }

        ForumPost post = new ForumPost();
        post.setUser(currentUser);
        post.setTitle(title.trim());
        post.setContent(content.trim());
        post.setCreatedAt(LocalDateTime.now());
        forumPostRepository.save(post);

        redirectAttributes.addFlashAttribute("successMessage", "Đăng bài thảo luận thành công!");
        return "redirect:/forum";
    }

    @PostMapping("/comment")
    public String addComment(@RequestParam("postId") int postId,
                             @RequestParam("content") String content,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login?required=true";
        }

        Optional<ForumPost> postOpt = forumPostRepository.findById(postId);
        if (postOpt.isPresent() && !content.trim().isEmpty()) {
            ForumComment comment = new ForumComment();
            comment.setPost(postOpt.get());
            comment.setUser(currentUser);
            comment.setContent(content.trim());
            comment.setCreatedAt(LocalDateTime.now());
            forumCommentRepository.save(comment);
        }
        return "redirect:/forum";
    }

    @PostMapping("/post/delete/{id}")
    public String deletePost(@PathVariable("id") int id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<ForumPost> postOpt = forumPostRepository.findById(id);
        if (postOpt.isPresent()) {
            ForumPost post = postOpt.get();
            boolean isOwner = post.getUser().getId() == currentUser.getId();
            boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
            if (isOwner || isAdmin) {
                forumPostRepository.delete(post);
                redirectAttributes.addFlashAttribute("successMessage", "Đã xóa bài viết!");
            }
        }
        return "redirect:/forum";
    }
}
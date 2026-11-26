package com.example.bookstore.Controller;

import com.example.bookstore.Entity.Orders;
import com.example.bookstore.Entity.User;
import com.example.bookstore.Repository.UserRepository;
import com.example.bookstore.Service.CultivationHelper;
import com.example.bookstore.Service.OrdersService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/users")
public class AdminUsersController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrdersService ordersService;

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String manageUsers(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<User> users = userRepository.findAll();
        Map<Integer, CultivationHelper.RealmInfo> userRealmMap = new HashMap<>();

        for (User u : users) {
            List<Orders> orders = ordersService.getOrdersByUserId(u.getId());
            userRealmMap.put(u.getId(), CultivationHelper.calculateForUser(u, orders));
        }

        model.addAttribute("users", users);
        model.addAttribute("userRealmMap", userRealmMap);
        model.addAttribute("allRealms", CultivationHelper.getAllRealms());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));

        return "AdminUsers";
    }

    @PostMapping("/update-realm")
    @Transactional
    public String updateRealm(@RequestParam("userId") int userId,
                              @RequestParam("customRealm") String customRealm,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Optional<User> opt = userRepository.findById(userId);
        if (opt.isPresent()) {
            User target = opt.get();
            if (customRealm == null || customRealm.trim().isEmpty() || "AUTO".equalsIgnoreCase(customRealm)) {
                target.setCustomRealm(null);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã đưa tài khoản \"" + target.getFullName() + "\" về chế độ tính cảnh giới tự động.");
            } else {
                target.setCustomRealm(customRealm.trim());
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã cập nhật cảnh giới của \"" + target.getFullName() + "\" thành " + customRealm + "!");
            }
            userRepository.save(target);


            CultivationHelper.RealmInfo newRealm = CultivationHelper.calculateForUser(
                    target, ordersService.getOrdersByUserId(target.getId()));
            redirectAttributes.addFlashAttribute("levelUpTargetUser", target.getFullName());
            redirectAttributes.addFlashAttribute("levelUpRealm", newRealm);


            User sessionUser = (User) session.getAttribute("currentUser");
            if (sessionUser != null && sessionUser.getId() == target.getId()) {
                session.setAttribute("currentUser", target);
            }
        }
        return "redirect:/admin/users";
    }
}
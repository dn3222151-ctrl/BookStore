package com.example.bookstore.Controller;

import com.example.bookstore.Entity.Books;
import com.example.bookstore.Entity.OrderDetails;
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

import java.util.*;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public String accountPage(Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("currentUser");
        if (sessionUser == null) {
            return "redirect:/login?required=true";
        }

        User currentUser = userRepository.findById(sessionUser.getId()).orElse(sessionUser);
        List<Orders> orders = ordersService.getOrdersByUserId(currentUser.getId());

        double totalSpent = 0.0;
        int totalBooksBought = 0;

        Map<Integer, Books> boughtBookMap = new LinkedHashMap<>();
        Map<Integer, Integer> boughtQtyMap = new HashMap<>();

        if (orders != null) {
            for (Orders order : orders) {
                if ("CANCELLED".equalsIgnoreCase(order.getStatus()) || "Đã hủy".equalsIgnoreCase(order.getStatus())) {
                    continue;
                }
                totalSpent += order.getTotalAmount();
                if (order.getOrderDetails() != null) {
                    for (OrderDetails detail : order.getOrderDetails()) {
                        totalBooksBought += detail.getQuantity();
                        Books b = detail.getBook();
                        if (b != null) {
                            boughtBookMap.putIfAbsent(b.getId(), b);
                            boughtQtyMap.put(b.getId(), boughtQtyMap.getOrDefault(b.getId(), 0) + detail.getQuantity());
                        }
                    }
                }
            }
        }

        CultivationHelper.RealmInfo realm = CultivationHelper.calculateRealm(
                totalSpent, totalBooksBought, currentUser.getCustomRealm()
        );

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("realm", realm);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("totalBooksBought", totalBooksBought);
        model.addAttribute("totalOrders", orders != null ? orders.size() : 0);
        model.addAttribute("boughtBooks", new ArrayList<>(boughtBookMap.values()));
        model.addAttribute("boughtQtyMap", boughtQtyMap);

        return "Account";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam("email") String email,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("currentUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        Optional<User> opt = userRepository.findById(sessionUser.getId());
        if (opt.isPresent()) {
            User user = opt.get();
            user.setFullName(fullName.trim());
            user.setEmail(email.trim());
            userRepository.save(user);
            session.setAttribute("currentUser", user);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin cá nhân thành công!");
        }
        return "redirect:/account";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("currentUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        if (newPassword == null || newPassword.length() < 4) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải có ít nhất 4 ký tự!");
            return "redirect:/account";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "redirect:/account";
        }

        Optional<User> opt = userRepository.findById(sessionUser.getId());
        if (opt.isPresent()) {
            User user = opt.get();
            if (!user.getPassword().equals(oldPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không đúng!");
                return "redirect:/account";
            }
            user.setPassword(newPassword);
            userRepository.save(user);
            session.setAttribute("currentUser", user);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        }
        return "redirect:/account";
    }
}
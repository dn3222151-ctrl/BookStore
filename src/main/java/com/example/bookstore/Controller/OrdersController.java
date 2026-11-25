package com.example.bookstore.Controller;

import com.example.bookstore.Entity.Orders;
import com.example.bookstore.Entity.User;
import com.example.bookstore.Service.OrdersService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    private boolean checkAdmin(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    @GetMapping
    public String listOrders(@RequestParam(value = "status", required = false) String status,
                             Model model,
                             HttpSession session) {
        if (!checkAdmin(session)) {
            return "redirect:/login";
        }

        List<Orders> orders;
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            orders = ordersService.getOrdersByStatus(status.trim());
        } else {
            orders = ordersService.getAllOrders();
        }

        double totalRevenue = orders.stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()))
                .mapToDouble(Orders::getTotalAmount)
                .sum();

        long pendingCount = ordersService.getAllOrders().stream()
                .filter(o -> "PENDING".equalsIgnoreCase(o.getStatus()))
                .count();

        model.addAttribute("orders", orders);
        model.addAttribute("selectedStatus", status != null ? status : "ALL");
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("currentUser", session.getAttribute("currentUser"));

        return "AdminOrders";
    }

    @GetMapping("/{id}")
    public String viewOrderDetail(@PathVariable("id") Integer id,
                                  Model model,
                                  HttpSession session) {
        if (!checkAdmin(session)) {
            return "redirect:/login";
        }

        Orders order = ordersService.getOrderById(id);
        if (order == null) {
            return "redirect:/admin/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
        return "OrderDetail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable("id") Integer id,
                               @RequestParam("status") String status,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        if (!checkAdmin(session)) {
            return "redirect:/login";
        }

        try {
            ordersService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng #" + id + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable("id") Integer id,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        if (!checkAdmin(session)) {
            return "redirect:/login";
        }

        try {
            ordersService.cancelOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng #" + id + " và hoàn lại số lượng sách vào kho!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi hủy đơn hàng: " + e.getMessage());
        }

        return "redirect:/admin/orders";
    }
}


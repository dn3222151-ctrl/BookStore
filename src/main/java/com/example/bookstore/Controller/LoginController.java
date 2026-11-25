package com.example.bookstore.Controller;

import com.example.bookstore.Entity.User;
import com.example.bookstore.Request.LoginRequest;
import com.example.bookstore.Request.RegisterRequest;
import com.example.bookstore.Response.LoginReponse;
import com.example.bookstore.Service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private LoginService loginService;

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "logout", required = false) String logout,
                                @RequestParam(value = "registered", required = false) String registered,
                                @RequestParam(value = "required", required = false) String required,
                                Model model,
                                HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
                return "redirect:/books/hienthi";
            }
            return "redirect:/shop";
        }

        if (logout != null) {
            model.addAttribute("message", "Bạn đã đăng xuất thành công!");
        }
        if (registered != null) {
            model.addAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
        }
        if (required != null) {
            model.addAttribute("error", "Vui lòng đăng nhập để tiếp tục thanh toán!");
        }

        model.addAttribute("loginRequest", new LoginRequest());
        return "Login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute("loginRequest") LoginRequest loginRequest,
                               HttpServletRequest request,
                               HttpSession session,
                               Model model) {
        String clientIp = request.getRemoteAddr();
        LoginReponse response = loginService.authenticate(loginRequest, clientIp);

        if (!response.isSuccess()) {
            model.addAttribute("error", response.getMessage());
            return "Login";
        }

        User user = response.getUser();
        session.setAttribute("currentUser", user);

        // Chuyển hướng theo vai trò (Role)
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/books/hienthi";
        } else {
            return "redirect:/shop";
        }
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "Register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("registerRequest") RegisterRequest registerRequest,
                                  Model model) {
        LoginReponse response = loginService.register(registerRequest);

        if (!response.isSuccess()) {
            model.addAttribute("error", response.getMessage());
            return "Register";
        }

        return "redirect:/login?registered=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }
}

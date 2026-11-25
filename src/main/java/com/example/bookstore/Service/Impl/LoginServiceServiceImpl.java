package com.example.bookstore.Service.Impl;

import com.example.bookstore.Entity.Login;
import com.example.bookstore.Entity.User;
import com.example.bookstore.Repository.LoginRepo;
import com.example.bookstore.Repository.UserRepository;
import com.example.bookstore.Request.LoginRequest;
import com.example.bookstore.Request.RegisterRequest;
import com.example.bookstore.Response.LoginReponse;
import com.example.bookstore.Service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoginServiceServiceImpl implements LoginService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginRepo loginRepo;

    @Override
    public String login(String username, String password) {
        if (username == null || password == null
                || username.trim().isEmpty() || password.trim().isEmpty()) {
            return "Không được để trống đăng nhập";
        }
        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password.trim())) {
            return "Đăng nhập thành công";
        }
        return "Tài khoản hoặc mật khẩu không đúng";
    }

    @Override
    @Transactional
    public LoginReponse authenticate(LoginRequest loginRequest, String ipAddress) {
        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null
                || loginRequest.getUsername().trim().isEmpty() || loginRequest.getPassword().trim().isEmpty()) {
            return new LoginReponse(false, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!", null);
        }

        String username = loginRequest.getUsername().trim();
        String password = loginRequest.getPassword().trim();

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return new LoginReponse(false, "Tài khoản không tồn tại trên hệ thống!", null);
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(password)) {
            return new LoginReponse(false, "Mật khẩu không chính xác!", null);
        }

        // Ghi lại lịch sử đăng nhập vào bảng login_history
        try {
            Login history = new Login();
            history.setUser(user);
            history.setLoginTime(LocalDateTime.now());
            history.setIpAddress(ipAddress != null ? ipAddress : "127.0.0.1");
            loginRepo.save(history);
        } catch (Exception ignored) {
        }

        LoginReponse response = new LoginReponse(true, "Đăng nhập thành công!", user);
        return response;
    }

    @Override
    @Transactional
    public LoginReponse register(RegisterRequest registerRequest) {
        if (registerRequest == null || registerRequest.getUsername() == null || registerRequest.getPassword() == null) {
            return new LoginReponse(false, "Vui lòng điền đầy đủ thông tin!", null);
        }

        String username = registerRequest.getUsername().trim();
        String password = registerRequest.getPassword().trim();
        String confirm = registerRequest.getConfirmPassword() != null ? registerRequest.getConfirmPassword().trim() : "";

        if (username.isEmpty() || password.isEmpty()) {
            return new LoginReponse(false, "Tên đăng nhập và mật khẩu không được để trống!", null);
        }

        if (!password.equals(confirm)) {
            return new LoginReponse(false, "Mật khẩu xác nhận không khớp!", null);
        }

        if (userRepository.existsByUsername(username)) {
            return new LoginReponse(false, "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác!", null);
        }

        if (registerRequest.getEmail() != null && !registerRequest.getEmail().trim().isEmpty()) {
            if (userRepository.existsByEmail(registerRequest.getEmail().trim())) {
                return new LoginReponse(false, "Email đã được sử dụng bởi tài khoản khác!", null);
            }
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(registerRequest.getFullName() != null ? registerRequest.getFullName().trim() : username);
        user.setEmail(registerRequest.getEmail() != null ? registerRequest.getEmail().trim() : "");
        user.setRole("USER"); // Mặc định tài khoản đăng ký là khách hàng USER

        User savedUser = userRepository.save(user);
        return new LoginReponse(true, "Đăng ký thành công!", savedUser);
    }

    @Override
    public User findByUsername(String username) {
        if (username == null) return null;
        return userRepository.findByUsername(username.trim()).orElse(null);
    }
}

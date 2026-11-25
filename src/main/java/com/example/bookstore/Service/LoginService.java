package com.example.bookstore.Service;

import com.example.bookstore.Entity.User;
import com.example.bookstore.Request.LoginRequest;
import com.example.bookstore.Request.RegisterRequest;
import com.example.bookstore.Response.LoginReponse;

public interface LoginService {

    String login(String username, String password);

    LoginReponse authenticate(LoginRequest loginRequest, String ipAddress);

    LoginReponse register(RegisterRequest registerRequest);

    User findByUsername(String username);
}

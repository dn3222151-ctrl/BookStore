package com.example.bookstore.Response;

import com.example.bookstore.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginReponse {
    private boolean success;
    private String message;
    private User user;

    public LoginReponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}

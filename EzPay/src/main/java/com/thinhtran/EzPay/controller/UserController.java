package com.thinhtran.EzPay.controller;

import com.thinhtran.EzPay.dto.response.UserResponse;
import com.thinhtran.EzPay.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/users")
public class UserController {
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal User user) {
        var res = new UserResponse();
        res.setUserName(user.getUserName());
        res.setFullName(user.getFullName());
        res.setEmail(user.getEmail());
        res.setBalance(user.getBalance());
        return ResponseEntity.ok(res);
    }
}

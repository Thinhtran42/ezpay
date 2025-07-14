package com.thinhtran.EzPay.controller;

import com.thinhtran.EzPay.dto.request.UpdateProfileRequest;
import com.thinhtran.EzPay.dto.response.ApiResponse;
import com.thinhtran.EzPay.dto.response.UserResponse;
import com.thinhtran.EzPay.entity.User;
import com.thinhtran.EzPay.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal User user) {
        var res = new UserResponse();
        res.setUserName(user.getUserName());
        res.setFullName(user.getFullName());
        res.setEmail(user.getEmail());
        res.setPhone(user.getPhone());
        res.setBalance(user.getBalance());
        res.setRole(user.getRole().name());
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công", res));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@AuthenticationPrincipal User user,
                                                                  @Valid @RequestBody UpdateProfileRequest request) {
        User updatedUser = userService.updateProfile(user.getUserName(), request);
        
        var res = new UserResponse();
        res.setUserName(updatedUser.getUserName());
        res.setFullName(updatedUser.getFullName());
        res.setEmail(updatedUser.getEmail());
        res.setPhone(updatedUser.getPhone());
        res.setBalance(updatedUser.getBalance());
        res.setRole(updatedUser.getRole().name());
        
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin thành công", res));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponse> userResponses = users.stream()
                .map(user -> {
                    var res = new UserResponse();
                    res.setUserName(user.getUserName());
                    res.setFullName(user.getFullName());
                    res.setEmail(user.getEmail());
                    res.setPhone(user.getPhone());
                    res.setBalance(user.getBalance());
                    res.setRole(user.getRole().name());
                    return res;
                })
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách người dùng thành công", userResponses));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(@RequestParam String q) {
        List<User> users = userService.searchUsers(q);
        List<UserResponse> userResponses = users.stream()
                .map(user -> {
                    var res = new UserResponse();
                    res.setUserName(user.getUserName());
                    res.setFullName(user.getFullName());
                    res.setEmail(user.getEmail());
                    res.setPhone(user.getPhone());
                    res.setBalance(user.getBalance());
                    res.setRole(user.getRole().name());
                    return res;
                })
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm người dùng thành công", userResponses));
    }
}

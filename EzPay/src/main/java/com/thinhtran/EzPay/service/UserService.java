package com.thinhtran.EzPay.service;

import com.thinhtran.EzPay.dto.request.UpdateProfileRequest;
import com.thinhtran.EzPay.entity.User;

import java.util.List;

public interface UserService {
    User updateProfile(String userName, UpdateProfileRequest request);
    List<User> getAllUsers();
    User getUserByUserName(String userName);
    List<User> searchUsers(String searchTerm);
} 
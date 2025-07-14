package com.thinhtran.EzPay.service.impl;

import com.thinhtran.EzPay.dto.request.UpdateProfileRequest;
import com.thinhtran.EzPay.entity.User;
import com.thinhtran.EzPay.exception.UserNotFoundException;
import com.thinhtran.EzPay.repository.UserRepository;
import com.thinhtran.EzPay.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User updateProfile(String userName, UpdateProfileRequest request) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng: " + userName));

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByUserName(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng: " + userName));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }
        
        String trimmedTerm = searchTerm.trim().toLowerCase();
        
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() != com.thinhtran.EzPay.entity.Role.ADMIN) // Exclude admin users
                .filter(user -> 
                    user.getUserName().toLowerCase().contains(trimmedTerm) ||
                    user.getFullName().toLowerCase().contains(trimmedTerm) ||
                    user.getEmail().toLowerCase().contains(trimmedTerm) ||
                    (user.getPhone() != null && user.getPhone().contains(trimmedTerm))
                )
                .limit(10) // Limit results to 10 for performance
                .toList();
    }
} 
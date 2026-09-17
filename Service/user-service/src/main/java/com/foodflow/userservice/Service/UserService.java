package com.foodflow.userservice.Service;

import com.foodflow.userservice.Dto.RegisterRequest;
import com.foodflow.userservice.Dto.UpdateUserRequest;
import com.foodflow.userservice.Dto.UserResponse;
import com.foodflow.userservice.Entity.User;
import com.foodflow.userservice.Entity.UserRole;
import com.foodflow.userservice.Exception.AdminRegistrationBlockedException;
import com.foodflow.userservice.Exception.EmailAlreadyExistsException;
import com.foodflow.userservice.Exception.UserNotFoundException;
import com.foodflow.userservice.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    private UserResponse convertToUserResponse(User savedUser) {
        UserResponse response = new UserResponse();

        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        response.setPhone(savedUser.getPhone());
        response.setAddress(savedUser.getAddress());
        response.setUserRole(savedUser.getRole());
        response.setCreatedAt(savedUser.getCreatedAt());
        response.setUpdatedAt(savedUser.getUpdatedAt());

        return response;
    }

    public UserResponse createUser(RegisterRequest request) {
        if(userRepo.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists!");
        }

        if (request.getUserRole() == UserRole.ADMIN) {
            throw new AdminRegistrationBlockedException("Admin registration is not allowed");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        user.setAddress(request.getAddress());
        user.setRole(request.getUserRole());

        User savedUser = userRepo.save(user);

        return convertToUserResponse(savedUser);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

        return convertToUserResponse(user);
    }

    public List<UserResponse> getUsers() {
        List<User> userList = userRepo.findAll();

        List<UserResponse> userResponseList = new ArrayList<>();

        for(User user : userList) {
            UserResponse userResponse = convertToUserResponse(user);
            userResponseList.add(userResponse);
        }

        return userResponseList;
    }

    public UserResponse updateUserById(Long id, UpdateUserRequest request) {
        User user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());

        User updatedUser = userRepo.save(user);

        return convertToUserResponse(updatedUser);
    }

    public void deleteUserById(Long id) {
        User user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

        userRepo.delete(user);
    }
}

package com.foodflow.userservice.Controller;

import com.foodflow.userservice.Dto.RegisterRequest;
import com.foodflow.userservice.Dto.UpdateUserRequest;
import com.foodflow.userservice.Dto.UserResponse;
import com.foodflow.userservice.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping()
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.createUser(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SERVICE') or @userAuthorization.isOwner(#id)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }
    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> responses = userService.getUsers();

        return new ResponseEntity<>(responses,HttpStatus.OK);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userAuthorization.isOwner(#id)")
    public ResponseEntity<UserResponse> updateUserById(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUserById(id,request);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userAuthorization.isOwner(#id)")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}

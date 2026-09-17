package com.foodflow.userservice.Service;

import com.foodflow.userservice.Dto.LoginRequest;
import com.foodflow.userservice.Dto.LoginResponse;
import com.foodflow.userservice.Dto.RegisterRequest;
import com.foodflow.userservice.Entity.User;
import com.foodflow.userservice.Entity.UserRole;
import com.foodflow.userservice.Exception.AdminRegistrationBlockedException;
import com.foodflow.userservice.Exception.EmailAlreadyExistsException;
import com.foodflow.userservice.Repository.UserRepo;
import com.foodflow.userservice.Security.CustomUserDetails;
import com.foodflow.userservice.Security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public void register(RegisterRequest request) {

        if (userRepo.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists!");
        }

        if (request.getUserRole() == UserRole.ADMIN) {
            throw new AdminRegistrationBlockedException(
                    "Admin registration is not allowed"
            );
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRole(request.getUserRole());

        userRepo.save(user);
    }

    public LoginResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(userDetails);

        return new LoginResponse(
                token,
                userDetails.getUserId(),
                userDetails.getRole()
        );
    }
}

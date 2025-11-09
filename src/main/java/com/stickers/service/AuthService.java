package com.stickers.service;

import com.stickers.dto.AuthRequest;
import com.stickers.dto.AuthResponse;
import com.stickers.entity.User;
import com.stickers.repository.UserRepository;
import com.stickers.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Random;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Transactional
    public AuthResponse signup(AuthRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        
        // Generate username: firstname_randomnumber
        String firstName = request.getName().split(" ")[0].toLowerCase();
        String username;
        Random random = new Random();
        do {
            int randomNumber = 1000 + random.nextInt(9000); // 4-digit random number (1000-9999)
            username = firstName + "_" + randomNumber;
        } while (userRepository.existsByUsername(username));
        
        User user = new User();
        user.setName(request.getName());
        user.setUsername(username);
        user.setEmail(request.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        user = userRepository.save(user);
        
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(
            user.getId(),
            user.getName(),
            user.getUsername(),
            user.getEmail(),
            user.getProfileImageUrl()
        );
        
        return new AuthResponse("User created successfully", token, userDto);
    }
    
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(
            user.getId(),
            user.getName(),
            user.getUsername(),
            user.getEmail(),
            user.getProfileImageUrl()
        );
        
        return new AuthResponse("Login successful", token, userDto);
    }
    
    public AuthResponse.UserDto verifyToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        
        Integer userId = jwtUtil.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        return new AuthResponse.UserDto(
            user.getId(),
            user.getName(),
            user.getUsername(),
            user.getEmail(),
            user.getProfileImageUrl()
        );
    }
}


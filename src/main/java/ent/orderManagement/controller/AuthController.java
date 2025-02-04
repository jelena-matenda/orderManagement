package ent.orderManagement.controller;

import ent.orderManagement.model.Role;
import ent.orderManagement.model.User;
import ent.orderManagement.payload.AuthRequest;
import ent.orderManagement.payload.AuthResponse;
import ent.orderManagement.repository.UserRepository;
import ent.orderManagement.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1) Register (sign up)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AuthRequest request) {
        // Check if user already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already taken.");
        }

        // Create new user with default ROLE_USER
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(Role.ROLE_USER));

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully.");
    }

    // 2) Login (sign in)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        // Attempt authentication
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // If successful, generate JWT
        var user = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        Set<String> roles = user.getAuthorities().stream()
        .map(authority -> authority.getAuthority())
        .collect(Collectors.toSet());

        String token = jwtUtils.generateToken(user.getUsername(), roles);
        return ResponseEntity.ok(new AuthResponse(token));
    }
}

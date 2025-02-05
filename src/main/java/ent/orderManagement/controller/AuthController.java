package ent.orderManagement.controller;

import ent.orderManagement.model.User;
import ent.orderManagement.service.UserService;
import ent.orderManagement.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /**
     * 📝 Register a new user (Admin or User)
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User request) {
        userService.registerUser(request.getUsername(), request.getPassword(), request.getRole());
        return ResponseEntity.ok("User registered successfully!");
    }

    /**
     * 🔑 Authenticate user & return JWT Token
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserDetails userDetails = userService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails.getUsername(), ((User) userDetails).getRole().name());

        return ResponseEntity.ok(token);
    }
}

package com.nguyenhuutai.example304.controllers;

import com.nguyenhuutai.example304.repository.UserRepository;
import com.nguyenhuutai.example304.repository.RoleRepository;
import com.nguyenhuutai.example304.dto.LoginDto;
import com.nguyenhuutai.example304.dto.RegisterDto;
import com.nguyenhuutai.example304.model.User;
import com.nguyenhuutai.example304.model.Role;
import com.nguyenhuutai.example304.model.ERole;
import com.nguyenhuutai.example304.security.jwt.JwtUtils;
import com.nguyenhuutai.example304.security.services.UserDetailsImpl;
import com.nguyenhuutai.example304.payload.response.JwtResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager; // THÊM

    public AuthController(UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            AuthenticationManager authenticationManager) { // THÊM
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager; // THÊM
    }

    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto) {
        System.out.println("=== REGISTER ===");
        System.out.println("Username: " + registerDto.getUsername());
        System.out.println("Email: " + registerDto.getEmail());
        System.out.println("Password: " + registerDto.getPassword());
        System.out.println("Requested Roles: " + registerDto.getRoles());

        if (userRepository.existsByUsername(registerDto.getUsername())) {
            return new ResponseEntity<>("Username is taken!", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(registerDto.getEmail())) {
            return new ResponseEntity<>("Email is already in use!", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Set<Role> roles = new HashSet<>();
        
        // XỬ LÝ ROLE THEO REQUEST
        if (registerDto.getRoles() == null || registerDto.getRoles().isEmpty()) {
            // Nếu không có role nào được chỉ định, mặc định là USER
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: USER Role is not found."));
            roles.add(userRole);
            System.out.println("Assigning default USER role");
        } else {
            // Xử lý các role được chỉ định
            for (String roleName : registerDto.getRoles()) {
                try {
                    // Chuyển đổi role name sang ERole enum
                    String roleEnumName = "ROLE_" + roleName.toUpperCase();
                    ERole roleEnum = ERole.valueOf(roleEnumName);
                    
                    Role role = roleRepository.findByName(roleEnum)
                            .orElseThrow(() -> new RuntimeException("Error: Role " + roleName + " is not found."));
                    roles.add(role);
                    System.out.println("Assigning role: " + roleName);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid role requested: " + roleName);
                    return new ResponseEntity<>("Invalid role: " + roleName, HttpStatus.BAD_REQUEST);
                }
            }
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        System.out.println("User registered successfully with ID: " + savedUser.getId());
        System.out.println("Assigned roles: " + roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));
        return new ResponseEntity<>("User registered success!", HttpStatus.OK);
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        try {
            // DÙNG AUTHENTICATION MANAGER
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication); // Dùng method có Authentication
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), 
                    userDetails.getUsername(), userDetails.getEmail(), roles));

        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            return new ResponseEntity<>("Invalid username or password! Error: " + e.getMessage(),
                    HttpStatus.UNAUTHORIZED);
        }
    }
}
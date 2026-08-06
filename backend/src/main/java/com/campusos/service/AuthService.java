package com.campusos.service;

import com.campusos.dto.AuthRequest;
import com.campusos.model.Role;
import com.campusos.model.UserEntity;
import com.campusos.repository.RoleRepository;
import com.campusos.repository.UserRepository;
import com.campusos.security.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RoleRepository roleRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.roleRepository = roleRepository;
    }

    public Optional<String> authenticate(AuthRequest req) {
        Optional<UserEntity> u = userRepository.findByEmail(req.getEmail());
        if (u.isEmpty()) return Optional.empty();
        UserEntity user = u.get();
        if (passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            String token = jwtUtils.generateToken(user.getId());
            return Optional.of(token);
        }
        return Optional.empty();
    }

    public UserEntity registerUser(String email, String password, String fullName) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(fullName);
        // assign STUDENT role by default if present
        roleRepository.findByName("STUDENT").ifPresent(r -> user.getRoles().add(r));
        return userRepository.save(user);
    }
}

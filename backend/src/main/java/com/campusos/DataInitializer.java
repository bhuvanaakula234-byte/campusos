package com.campusos;

import com.campusos.model.Role;
import com.campusos.model.UserEntity;
import com.campusos.repository.RoleRepository;
import com.campusos.repository.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        // seed roles
        seedRole("STUDENT", "Student role");
        seedRole("FACULTY", "Faculty role");
        seedRole("HOD", "Head of Department");
        seedRole("PRINCIPAL", "Principal");
        seedRole("PLACEMENT", "Placement cell");
        seedRole("ADMIN", "Administrator");

        // create admin user if not exists
        String adminEmail = "admin@campusos.local";
        Optional<UserEntity> adminOpt = userRepository.findByEmail(adminEmail);
        if (adminOpt.isEmpty()) {
            UserEntity admin = new UserEntity();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode("adminpass"));
            admin.setFullName("CampusOS Admin");
            roleRepository.findByName("ADMIN").ifPresent(r -> admin.setRoles(Set.of(r)));
            userRepository.save(admin);
            System.out.println("Created admin user: " + adminEmail + " with password 'adminpass' - change immediately");
        }
    }

    private void seedRole(String name, String desc) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role r = new Role();
            r.setName(name);
            r.setDescription(desc);
            roleRepository.save(r);
        }
    }
}

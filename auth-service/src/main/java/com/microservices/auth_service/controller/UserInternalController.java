package com.microservices.auth_service.controller;

import com.microservices.auth_service.dto.UserInternalResponse;
import com.microservices.auth_service.entity.Role;
import com.microservices.auth_service.entity.User;
import com.microservices.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/internal")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserRepository userRepository;

    @GetMapping("/etudiants")
    public ResponseEntity<List<UserInternalResponse>> getAllStudents() {
        List<UserInternalResponse> students = userRepository.findByRoleAndEnabledTrue(Role.ROLE_ETUDIANT)
                .stream()
                .map(UserInternalResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(students);
    }

    @GetMapping("/{externalId}")
    public ResponseEntity<UserInternalResponse> getByExternalId(@PathVariable Long externalId) {
        return userRepository.findByExternalId(externalId)
                .map(UserInternalResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{externalId}/exists")
    public ResponseEntity<Boolean> existsByExternalIdAndRole(
            @PathVariable Long externalId,
            @RequestParam Role role
    ) {
        return ResponseEntity.ok(userRepository.findByExternalId(externalId)
                .filter(User::isEnabled)
                .map(user -> user.getRole() == role)
                .orElse(false));
    }
}

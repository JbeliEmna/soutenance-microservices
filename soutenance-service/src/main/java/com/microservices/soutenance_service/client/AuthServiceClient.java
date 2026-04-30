package com.microservices.soutenance_service.client;

import com.microservices.soutenance_service.dto.AuthUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {
    @GetMapping("/api/users/internal/{externalId}")
    AuthUserResponse getByExternalId(@PathVariable("externalId") Long externalId);

    @GetMapping("/api/users/internal/{externalId}/exists")
    Boolean existsByExternalIdAndRole(
            @PathVariable("externalId") Long externalId,
            @RequestParam("role") String role
    );
}

package com.microservices.soutenance_service.client;

import com.microservices.soutenance_service.dto.AuthUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {
    @GetMapping("/api/users/internal/{externalId}")
    AuthUserResponse getByExternalId(@PathVariable("externalId") Long externalId);
}

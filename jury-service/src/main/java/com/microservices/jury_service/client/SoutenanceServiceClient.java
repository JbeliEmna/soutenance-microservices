package com.microservices.jury_service.client;

import com.microservices.jury_service.dto.SoutenanceSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "soutenance-service")
public interface SoutenanceServiceClient {
    @GetMapping("/api/soutenances/{id}")
    SoutenanceSummaryDTO getSoutenanceById(@PathVariable("id") Long id);
}

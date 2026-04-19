package com.microservices.planning.controller;

import com.microservices.planning.repository.SalleRepository;
import com.microservices.planning.repository.CreneauRepository;
import com.microservices.planning.repository.OccupationSalleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final SalleRepository salleRepository;
    private final CreneauRepository creneauRepository;
    private final OccupationSalleRepository occupationRepository;

    @GetMapping("/ping")
    public String ping() {
        log.info("GET /api/test/ping - Test de connectivité");
        return "✅ Service Planning & Conflits opérationnel - " + LocalDateTime.now();
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        log.info("GET /api/test/stats - Statistiques base de données");
        Map<String, Long> stats = new HashMap<>();
        stats.put("nombre_salles", salleRepository.count());
        stats.put("nombre_creneaux", creneauRepository.count());
        stats.put("nombre_reservations", occupationRepository.count());
        return stats;
    }
}
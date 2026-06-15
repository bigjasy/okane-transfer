package ma.ensam.okanetransfer.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import ma.ensam.okanetransfer.dto.agency.CorridorRequest;
import ma.ensam.okanetransfer.dto.agency.CorridorResponse;
import ma.ensam.okanetransfer.service.CorridorService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/corridors")
public class CorridorController {

    private final CorridorService corridorService;

    public CorridorController(CorridorService corridorService) {
        this.corridorService = corridorService;
    }

    @GetMapping
    public ResponseEntity<List<CorridorResponse>> getAllCorridors() {
        return ResponseEntity.ok(corridorService.getAllCorridors());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CorridorResponse> createCorridor(@Valid @RequestBody CorridorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(corridorService.createCorridor(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CorridorResponse> updateCorridor(
            @PathVariable("id") Long id,
            @Valid @RequestBody CorridorRequest request) {
        return ResponseEntity.ok(corridorService.updateCorridor(id, request));
    }

    @PatchMapping("/{id}/activation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CorridorResponse> toggleActivation(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Boolean> payload) {
        boolean active = payload.getOrDefault("active", false);
        return ResponseEntity.ok(corridorService.toggleActivation(id, active));
    }
}

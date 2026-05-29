package ma.ensam.okanetransfer.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ma.ensam.okanetransfer.dto.agency.CorridorRequest;
import ma.ensam.okanetransfer.dto.agency.CorridorResponse;
import ma.ensam.okanetransfer.service.CorridorService;

import jakarta.validation.Valid;

import java.util.List;

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
    public ResponseEntity<CorridorResponse> createCorridor(@Valid @RequestBody CorridorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(corridorService.createCorridor(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CorridorResponse> updateCorridor(@PathVariable Long id, @RequestBody CorridorRequest request) {
        return ResponseEntity.ok(corridorService.updateCorridor(id, request));
    }
}
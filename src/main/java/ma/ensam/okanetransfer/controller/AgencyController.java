package ma.ensam.okanetransfer.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ma.ensam.okanetransfer.dto.agency.AgencyRequest;
import ma.ensam.okanetransfer.dto.agency.AgencyResponse;
import ma.ensam.okanetransfer.enums.AgencyStatus;
import ma.ensam.okanetransfer.service.AgencyService;

@RestController
@RequestMapping("/api/v1/agencies")
public class AgencyController {

    private final AgencyService agencyService;

    public AgencyController(AgencyService agencyService) {
        this.agencyService = agencyService;
    }

    @GetMapping
    public ResponseEntity<Page<AgencyResponse>> getAllAgencies(
            @RequestParam(required = false) Long countryId,
            @RequestParam(required = false) AgencyStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<AgencyResponse> agencies = agencyService.getAllAgencies(countryId, status, PageRequest.of(page, size));
        return ResponseEntity.ok(agencies);
    }

    @PostMapping
    public ResponseEntity<AgencyResponse> createAgency(@RequestBody AgencyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agencyService.createAgency(request));
    }

    @getMapping("/{id}")
    public ResponseEntity<AgencyResponse> getAgencyById(@PathVariable Long id) {
        return ResponseEntity.ok(agencyService.getAgencyById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgencyResponse> updateAgency(@PathVariable Long id, @RequestBody AgencyRequest request) {
        return ResponseEntity.ok(agencyService.updateAgency(id, request));
    }

    @patchMapping("/{id}/status")
    public ResponseEntity<AgencyResponse> updateStatus(@PathVariable Long id, @RequestParam AgencyStatus status) {
        return ResponseEntity.ok(agencyService.updateStatus(id, status));
    }

    @PostMapping("/{id}/agents/{agentId}")
    public ResponseEntity<AgencyResponse> assignAgent(@PathVariable Long id, @PathVariable Long agentId) {
        return ResponseEntity.ok(agencyService.assignAgent(id, agentId));
    }

    @PostMapping("/{id}/managers/{managerId}")
    public ResponseEntity<AgencyResponse> assignManager(@PathVariable Long id, @PathVariable Long managerId) {
        return ResponseEntity.ok(agencyService.assignManager(id, managerId));
    }
}
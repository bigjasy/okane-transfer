package ma.ensam.okanetransfer.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import ma.ensam.okanetransfer.dto.agency.AgencyRequest;
import ma.ensam.okanetransfer.dto.agency.AgencyResponse;
import ma.ensam.okanetransfer.dto.agency.AgencyStaffResponse;
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
            @RequestParam(name = "countryId", required = false) Long countryId,
            @RequestParam(name = "status", required = false) AgencyStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        Page<AgencyResponse> agencies = agencyService.getAllAgencies(countryId, status, PageRequest.of(page, size));
        return ResponseEntity.ok(agencies);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgencyResponse> createAgency(@RequestBody AgencyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agencyService.createAgency(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgencyResponse> getAgencyById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(agencyService.getAgencyById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgencyResponse> updateAgency(@PathVariable("id") Long id, @RequestBody AgencyRequest request) {
        return ResponseEntity.ok(agencyService.updateAgency(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<AgencyResponse> updateStatus(@PathVariable("id") Long id, @RequestParam(name = "status") AgencyStatus status) {
        return ResponseEntity.ok(agencyService.updateStatus(id, status));
    }

    @PostMapping("/{id}/agents/{agentId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<AgencyResponse> assignAgent(@PathVariable("id") Long id, @PathVariable("agentId") Long agentId) {
        return ResponseEntity.ok(agencyService.assignAgent(id, agentId));
    }

    @PostMapping("/{id}/managers/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgencyResponse> assignManager(@PathVariable("id") Long id, @PathVariable("managerId") Long managerId) {
        return ResponseEntity.ok(agencyService.assignManager(id, managerId));
    }

    @GetMapping("/{id}/staff")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<AgencyStaffResponse> getAgencyStaff(@PathVariable("id") Long id) {
        return ResponseEntity.ok(agencyService.getAgencyStaff(id));
    }
}

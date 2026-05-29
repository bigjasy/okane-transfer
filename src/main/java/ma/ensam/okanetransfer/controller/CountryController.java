package ma.ensam.okanetransfer.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import ma.ensam.okanetransfer.dto.referential.ActivationRequest;
import ma.ensam.okanetransfer.dto.referential.CountryRequest;
import ma.ensam.okanetransfer.dto.referential.CountryResponse;
import ma.ensam.okanetransfer.service.CountryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/countries")
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping
    public ResponseEntity<List<CountryResponse>> listCountries(@RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(countryService.listCountries(active));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CountryResponse> createCountry(@Valid @RequestBody CountryRequest request) {
        CountryResponse created = countryService.createCountry(request);
        return ResponseEntity.created(URI.create("/api/v1/countries/" + created.getId())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CountryResponse> getCountry(@PathVariable Long id) {
        return ResponseEntity.ok(countryService.getCountry(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CountryResponse> updateCountry(
            @PathVariable Long id,
            @Valid @RequestBody CountryRequest request
    ) {
        return ResponseEntity.ok(countryService.updateCountry(id, request));
    }

    @PatchMapping("/{id}/activation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CountryResponse> updateActivation(
            @PathVariable Long id,
            @RequestBody ActivationRequest request
    ) {
        return ResponseEntity.ok(countryService.updateActivation(id, request.isActive()));
    }
}

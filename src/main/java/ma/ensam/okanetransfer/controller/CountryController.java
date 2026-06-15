package ma.ensam.okanetransfer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Countries", description = "ISO country referential (M3)")
@SecurityRequirement(name = "BearerAuth")
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping
    @Operation(summary = "List countries")
    public ResponseEntity<List<CountryResponse>> listCountries(@RequestParam(name = "active", required = false) Boolean active) {
        return ResponseEntity.ok(countryService.listCountries(active));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create country")
    public ResponseEntity<CountryResponse> createCountry(@Valid @RequestBody CountryRequest request) {
        CountryResponse created = countryService.createCountry(request);
        return ResponseEntity.created(URI.create("/api/v1/countries/" + created.getId())).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get country by id")
    public ResponseEntity<CountryResponse> getCountry(@PathVariable("id") Long id) {
        return ResponseEntity.ok(countryService.getCountry(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update country")
    public ResponseEntity<CountryResponse> updateCountry(
            @PathVariable("id") Long id,
            @Valid @RequestBody CountryRequest request
    ) {
        return ResponseEntity.ok(countryService.updateCountry(id, request));
    }

    @PatchMapping("/{id}/activation")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate or deactivate country")
    public ResponseEntity<CountryResponse> updateActivation(
            @PathVariable("id") Long id,
            @RequestBody ActivationRequest request
    ) {
        return ResponseEntity.ok(countryService.updateActivation(id, request.isActive()));
    }
}

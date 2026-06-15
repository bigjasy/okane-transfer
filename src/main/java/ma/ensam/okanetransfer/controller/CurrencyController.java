package ma.ensam.okanetransfer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import ma.ensam.okanetransfer.dto.referential.ActivationRequest;
import ma.ensam.okanetransfer.dto.referential.CurrencyRequest;
import ma.ensam.okanetransfer.dto.referential.CurrencyResponse;
import ma.ensam.okanetransfer.service.CurrencyService;
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
@RequestMapping("/api/v1/currencies")
@Tag(name = "Currencies", description = "ISO currency referential (M3)")
@SecurityRequirement(name = "BearerAuth")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping
    @Operation(summary = "List currencies")
    public ResponseEntity<List<CurrencyResponse>> listCurrencies(@RequestParam(name = "active", required = false) Boolean active) {
        return ResponseEntity.ok(currencyService.listCurrencies(active));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get currency by id")
    public ResponseEntity<CurrencyResponse> getCurrency(@PathVariable("id") Long id) {
        return ResponseEntity.ok(currencyService.getCurrency(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create currency")
    public ResponseEntity<CurrencyResponse> createCurrency(@Valid @RequestBody CurrencyRequest request) {
        CurrencyResponse created = currencyService.createCurrency(request);
        return ResponseEntity.created(URI.create("/api/v1/currencies/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update currency")
    public ResponseEntity<CurrencyResponse> updateCurrency(
            @PathVariable("id") Long id,
            @Valid @RequestBody CurrencyRequest request
    ) {
        return ResponseEntity.ok(currencyService.updateCurrency(id, request));
    }

    @PatchMapping("/{id}/activation")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate or deactivate currency")
    public ResponseEntity<CurrencyResponse> updateActivation(
            @PathVariable("id") Long id,
            @RequestBody ActivationRequest request
    ) {
        return ResponseEntity.ok(currencyService.updateActivation(id, request.isActive()));
    }
}

package ma.ensam.okanetransfer.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import ma.ensam.okanetransfer.dto.referential.CurrencyRequest;
import ma.ensam.okanetransfer.dto.referential.CurrencyResponse;
import ma.ensam.okanetransfer.service.CurrencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping
    public ResponseEntity<List<CurrencyResponse>> listCurrencies(@RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(currencyService.listCurrencies(active));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CurrencyResponse> createCurrency(@Valid @RequestBody CurrencyRequest request) {
        CurrencyResponse created = currencyService.createCurrency(request);
        return ResponseEntity.created(URI.create("/api/v1/currencies/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CurrencyResponse> updateCurrency(
            @PathVariable Long id,
            @Valid @RequestBody CurrencyRequest request
    ) {
        return ResponseEntity.ok(currencyService.updateCurrency(id, request));
    }
}

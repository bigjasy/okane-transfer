package ma.ensam.okanetransfer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import ma.ensam.okanetransfer.dto.referential.ActivationRequest;
import ma.ensam.okanetransfer.dto.referential.ConversionRequest;
import ma.ensam.okanetransfer.dto.referential.ConversionResponse;
import ma.ensam.okanetransfer.dto.referential.ExchangeRateHistoryResponse;
import ma.ensam.okanetransfer.dto.referential.ExchangeRateRequest;
import ma.ensam.okanetransfer.dto.referential.ExchangeRateResponse;
import ma.ensam.okanetransfer.dto.referential.ExchangeRateSyncResponse;
import ma.ensam.okanetransfer.service.ExchangeRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exchange-rates")
@Tag(name = "Exchange Rates", description = "Multi-currency rates, conversion and external sync (M3)")
@SecurityRequirement(name = "BearerAuth")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    @Operation(summary = "List exchange rates", description = "Returns active or filtered exchange rates.")
    public ResponseEntity<List<ExchangeRateResponse>> listRates(
            @RequestParam(name = "source", required = false) String source,
            @RequestParam(name = "target", required = false) String target,
            @RequestParam(name = "active", required = false) Boolean active
    ) {
        return ResponseEntity.ok(exchangeRateService.listActiveRates(source, target, active));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upsert exchange rate", description = "Creates a new active rate and archives the previous one with history.")
    public ResponseEntity<ExchangeRateResponse> upsertRate(
            Authentication authentication,
            HttpServletRequest request,
            @Valid @RequestBody ExchangeRateRequest body
    ) {
        ExchangeRateResponse created = exchangeRateService.upsertRate(
                body,
                authentication.getName(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );
        return ResponseEntity.created(URI.create("/api/v1/exchange-rates/" + created.getId())).body(created);
    }

    @PostMapping("/sync-external")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sync rates from simulated external provider", description = "Pulls FX quotes from OkaneFX-Simulated and upserts active rates with EXTERNAL_API source.")
    public ResponseEntity<ExchangeRateSyncResponse> syncExternalRates(
            Authentication authentication,
            HttpServletRequest request
    ) {
        ExchangeRateSyncResponse response = exchangeRateService.syncFromExternalProvider(
                authentication.getName(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/convert")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT','CLIENT')")
    @Operation(summary = "Convert amount", description = "Converts an amount using the active rate for the currency pair.")
    public ResponseEntity<ConversionResponse> convert(@Valid @RequestBody ConversionRequest request) {
        return ResponseEntity.ok(exchangeRateService.convert(request));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Exchange rate history", description = "Returns audit trail of rate changes.")
    public ResponseEntity<List<ExchangeRateHistoryResponse>> history(
            @RequestParam(name = "source", required = false) String source,
            @RequestParam(name = "target", required = false) String target
    ) {
        return ResponseEntity.ok(exchangeRateService.getHistory(source, target));
    }
}

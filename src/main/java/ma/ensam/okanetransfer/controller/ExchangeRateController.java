package ma.ensam.okanetransfer.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import ma.ensam.okanetransfer.dto.referential.ConversionRequest;
import ma.ensam.okanetransfer.dto.referential.ConversionResponse;
import ma.ensam.okanetransfer.dto.referential.ExchangeRateHistoryResponse;
import ma.ensam.okanetransfer.dto.referential.ExchangeRateRequest;
import ma.ensam.okanetransfer.dto.referential.ExchangeRateResponse;
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
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT')")
    public ResponseEntity<List<ExchangeRateResponse>> listRates(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String target,
            @RequestParam(required = false) Boolean active
    ) {
        return ResponseEntity.ok(exchangeRateService.listActiveRates(source, target, active));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
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

    @PostMapping("/convert")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AGENT','CLIENT')")
    public ResponseEntity<ConversionResponse> convert(@Valid @RequestBody ConversionRequest request) {
        return ResponseEntity.ok(exchangeRateService.convert(request));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<ExchangeRateHistoryResponse>> history(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String target
    ) {
        return ResponseEntity.ok(exchangeRateService.getHistory(source, target));
    }
}

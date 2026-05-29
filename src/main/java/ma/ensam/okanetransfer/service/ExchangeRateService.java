package ma.ensam.okanetransfer.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import ma.ensam.okanetransfer.domain.referential.Currency;
import ma.ensam.okanetransfer.domain.referential.ExchangeRate;
import ma.ensam.okanetransfer.domain.referential.ExchangeRateHistory;
import ma.ensam.okanetransfer.domain.user.User;
import ma.ensam.okanetransfer.dto.referential.ConversionRequest;
import ma.ensam.okanetransfer.dto.referential.ConversionResponse;
import ma.ensam.okanetransfer.dto.referential.ExchangeRateHistoryResponse;
import ma.ensam.okanetransfer.dto.referential.ExchangeRateRequest;
import ma.ensam.okanetransfer.dto.referential.ExchangeRateResponse;
import ma.ensam.okanetransfer.enums.AuditAction;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.exception.ResourceNotFoundException;
import ma.ensam.okanetransfer.repository.ExchangeRateHistoryRepository;
import ma.ensam.okanetransfer.repository.ExchangeRateRepository;
import ma.ensam.okanetransfer.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateHistoryRepository exchangeRateHistoryRepository;
    private final CurrencyService currencyService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public ExchangeRateService(
            ExchangeRateRepository exchangeRateRepository,
            ExchangeRateHistoryRepository exchangeRateHistoryRepository,
            CurrencyService currencyService,
            UserRepository userRepository,
            AuditService auditService
    ) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.exchangeRateHistoryRepository = exchangeRateHistoryRepository;
        this.currencyService = currencyService;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<ExchangeRateResponse> listActiveRates(String source, String target, Boolean active) {
        List<ExchangeRate> rates = exchangeRateRepository.findAll().stream()
                .filter(rate -> active == null || rate.isActive() == active)
                .filter(rate -> source == null || rate.getSourceCurrency().getCode().equalsIgnoreCase(source))
                .filter(rate -> target == null || rate.getTargetCurrency().getCode().equalsIgnoreCase(target))
                .toList();
        return rates.stream().map(this::toResponse).toList();
    }

    public ExchangeRateResponse upsertRate(ExchangeRateRequest request, String actorEmail, String ipAddress, String userAgent) {
        Currency source = currencyService.findCurrency(request.getSourceCurrencyId());
        Currency target = currencyService.findCurrency(request.getTargetCurrencyId());
        if (source.getId().equals(target.getId())) {
            throw new BusinessException("Source and target currencies must differ");
        }

        var existingRate = exchangeRateRepository.findActiveRate(source.getCode(), target.getCode());
        BigDecimal oldRateValue = existingRate.map(ExchangeRate::getRate).orElse(null);
        existingRate.ifPresent(existing -> {
            existing.setActive(false);
            exchangeRateRepository.save(existing);
        });

        ExchangeRate rate = new ExchangeRate();
        rate.setSourceCurrency(source);
        rate.setTargetCurrency(target);
        rate.setRate(request.getRate());
        rate.setSource(request.getSource());
        rate.setActive(true);
        ExchangeRate saved = exchangeRateRepository.save(rate);

        User actor = userRepository.findByEmailIgnoreCase(actorEmail).orElse(null);
        ExchangeRateHistory history = new ExchangeRateHistory();
        history.setSourceCurrencyCode(source.getCode());
        history.setTargetCurrencyCode(target.getCode());
        history.setOldRate(oldRateValue);
        history.setNewRate(request.getRate());
        history.setSource(request.getSource());
        history.setChangedBy(actor);
        exchangeRateHistoryRepository.save(history);

        if (actor != null) {
            auditService.record(
                    AuditAction.UPDATE_RATE,
                    actor,
                    "ExchangeRate",
                    String.valueOf(saved.getId()),
                    ipAddress,
                    userAgent,
                    "{\"source\":\"" + source.getCode() + "\",\"target\":\"" + target.getCode() + "\",\"rate\":" + request.getRate() + "}"
            );
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ConversionResponse convert(ConversionRequest request) {
        ExchangeRate rate = exchangeRateRepository
                .findActiveRate(request.getSourceCurrency().toUpperCase(), request.getTargetCurrency().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active exchange rate",
                        request.getSourceCurrency() + "->" + request.getTargetCurrency()
                ));

        BigDecimal converted = request.getAmount()
                .multiply(rate.getRate())
                .setScale(2, RoundingMode.HALF_UP);

        ConversionResponse response = new ConversionResponse();
        response.setSourceAmount(request.getAmount());
        response.setConvertedAmount(converted);
        response.setRate(rate.getRate());
        response.setSourceCurrency(rate.getSourceCurrency().getCode());
        response.setTargetCurrency(rate.getTargetCurrency().getCode());
        return response;
    }

    @Transactional(readOnly = true)
    public List<ExchangeRateHistoryResponse> getHistory(String source, String target) {
        if (source == null || target == null) {
            return exchangeRateHistoryRepository.findAll().stream().map(this::toHistoryResponse).toList();
        }
        return exchangeRateHistoryRepository
                .findBySourceCurrencyCodeAndTargetCurrencyCodeOrderByChangedAtDesc(source.toUpperCase(), target.toUpperCase())
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private ExchangeRateResponse toResponse(ExchangeRate rate) {
        ExchangeRateResponse response = new ExchangeRateResponse();
        response.setId(rate.getId());
        response.setSourceCurrency(rate.getSourceCurrency().getCode());
        response.setTargetCurrency(rate.getTargetCurrency().getCode());
        response.setRate(rate.getRate());
        response.setSource(rate.getSource());
        response.setValidFrom(rate.getValidFrom());
        response.setActive(rate.isActive());
        return response;
    }

    private ExchangeRateHistoryResponse toHistoryResponse(ExchangeRateHistory history) {
        ExchangeRateHistoryResponse response = new ExchangeRateHistoryResponse();
        response.setId(history.getId());
        response.setSourceCurrencyCode(history.getSourceCurrencyCode());
        response.setTargetCurrencyCode(history.getTargetCurrencyCode());
        response.setOldRate(history.getOldRate());
        response.setNewRate(history.getNewRate());
        response.setSource(history.getSource());
        response.setChangedAt(history.getChangedAt());
        if (history.getChangedBy() != null) {
            response.setChangedByEmail(history.getChangedBy().getEmail());
        }
        return response;
    }
}

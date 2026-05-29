package ma.ensam.okanetransfer.service;

import java.util.List;
import ma.ensam.okanetransfer.domain.referential.Currency;
import ma.ensam.okanetransfer.dto.referential.CurrencyRequest;
import ma.ensam.okanetransfer.dto.referential.CurrencyResponse;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.exception.ResourceNotFoundException;
import ma.ensam.okanetransfer.repository.CurrencyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    @Transactional(readOnly = true)
    public List<CurrencyResponse> listCurrencies(Boolean active) {
        List<Currency> currencies = active == null
                ? currencyRepository.findAll()
                : active ? currencyRepository.findByActiveTrue() : currencyRepository.findAll().stream().filter(c -> !c.isActive()).toList();
        return currencies.stream().map(this::toResponse).toList();
    }

    public CurrencyResponse createCurrency(CurrencyRequest request) {
        if (currencyRepository.findByCode(request.getCode().toUpperCase()).isPresent()) {
            throw new BusinessException("CONFLICT", "Currency code already exists", HttpStatus.CONFLICT);
        }
        Currency currency = new Currency();
        currency.setCode(request.getCode().toUpperCase());
        currency.setName(request.getName());
        currency.setSymbol(request.getSymbol());
        currency.setScale(request.getScale());
        currency.setActive(request.isActive());
        return toResponse(currencyRepository.save(currency));
    }

    public CurrencyResponse updateCurrency(Long id, CurrencyRequest request) {
        Currency currency = findCurrency(id);
        currency.setName(request.getName());
        currency.setSymbol(request.getSymbol());
        currency.setScale(request.getScale());
        currency.setActive(request.isActive());
        return toResponse(currencyRepository.save(currency));
    }

    @Transactional(readOnly = true)
    public Currency findCurrency(Long id) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency", id));
    }

    @Transactional(readOnly = true)
    public Currency findByCode(String code) {
        return currencyRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", code));
    }

    private CurrencyResponse toResponse(Currency currency) {
        CurrencyResponse response = new CurrencyResponse();
        response.setId(currency.getId());
        response.setCode(currency.getCode());
        response.setName(currency.getName());
        response.setSymbol(currency.getSymbol());
        response.setScale(currency.getScale());
        response.setActive(currency.isActive());
        return response;
    }
}

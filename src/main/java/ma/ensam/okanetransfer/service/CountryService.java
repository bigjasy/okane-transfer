package ma.ensam.okanetransfer.service;

import java.util.List;
import ma.ensam.okanetransfer.domain.referential.Country;
import ma.ensam.okanetransfer.domain.referential.Currency;
import ma.ensam.okanetransfer.dto.referential.CountryRequest;
import ma.ensam.okanetransfer.dto.referential.CountryResponse;
import ma.ensam.okanetransfer.dto.referential.CurrencyResponse;
import ma.ensam.okanetransfer.exception.BusinessException;
import ma.ensam.okanetransfer.exception.ResourceNotFoundException;
import ma.ensam.okanetransfer.repository.CountryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CountryService {

    private final CountryRepository countryRepository;
    private final CurrencyService currencyService;

    public CountryService(CountryRepository countryRepository, CurrencyService currencyService) {
        this.countryRepository = countryRepository;
        this.currencyService = currencyService;
    }

    @Transactional(readOnly = true)
    public List<CountryResponse> listCountries(Boolean active) {
        List<Country> countries = active == null
                ? countryRepository.findAll()
                : active ? countryRepository.findByActiveTrue() : countryRepository.findAll().stream().filter(c -> !c.isActive()).toList();
        return countries.stream().map(this::toResponse).toList();
    }

    public CountryResponse createCountry(CountryRequest request) {
        if (countryRepository.findByIsoCode(request.getIsoCode().toUpperCase()).isPresent()) {
            throw new BusinessException("CONFLICT", "Country ISO code already exists", HttpStatus.CONFLICT);
        }
        Currency currency = currencyService.findCurrency(request.getCurrencyId());
        Country country = new Country();
        country.setIsoCode(request.getIsoCode().toUpperCase());
        country.setName(request.getName());
        country.setPhonePrefix(request.getPhonePrefix());
        country.setCurrency(currency);
        country.setActive(request.isActive());
        return toResponse(countryRepository.save(country));
    }

    @Transactional(readOnly = true)
    public CountryResponse getCountry(Long id) {
        return toResponse(findCountry(id));
    }

    public CountryResponse updateCountry(Long id, CountryRequest request) {
        Country country = findCountry(id);
        Currency currency = currencyService.findCurrency(request.getCurrencyId());
        country.setName(request.getName());
        country.setPhonePrefix(request.getPhonePrefix());
        country.setCurrency(currency);
        country.setActive(request.isActive());
        return toResponse(countryRepository.save(country));
    }

    public CountryResponse updateActivation(Long id, boolean active) {
        Country country = findCountry(id);
        country.setActive(active);
        return toResponse(countryRepository.save(country));
    }

    @Transactional(readOnly = true)
    public Country findCountry(Long id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country", id));
    }

    private CountryResponse toResponse(Country country) {
        CountryResponse response = new CountryResponse();
        response.setId(country.getId());
        response.setIsoCode(country.getIsoCode());
        response.setName(country.getName());
        response.setPhonePrefix(country.getPhonePrefix());
        response.setActive(country.isActive());
        CurrencyResponse currencyResponse = new CurrencyResponse();
        Currency currency = country.getCurrency();
        currencyResponse.setId(currency.getId());
        currencyResponse.setCode(currency.getCode());
        currencyResponse.setName(currency.getName());
        currencyResponse.setSymbol(currency.getSymbol());
        currencyResponse.setScale(currency.getScale());
        currencyResponse.setActive(currency.isActive());
        response.setCurrency(currencyResponse);
        return response;
    }
}

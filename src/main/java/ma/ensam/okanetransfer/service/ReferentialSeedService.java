package ma.ensam.okanetransfer.service;

import java.math.BigDecimal;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import ma.ensam.okanetransfer.domain.compliance.WatchlistEntry;
import ma.ensam.okanetransfer.domain.referential.Country;
import ma.ensam.okanetransfer.domain.referential.Currency;
import ma.ensam.okanetransfer.domain.referential.ExchangeRate;
import ma.ensam.okanetransfer.enums.RateSource;
import ma.ensam.okanetransfer.repository.CountryRepository;
import ma.ensam.okanetransfer.repository.CurrencyRepository;
import ma.ensam.okanetransfer.repository.ExchangeRateRepository;
import ma.ensam.okanetransfer.repository.WatchlistEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReferentialSeedService {

    private final CurrencyRepository currencyRepository;
    private final CountryRepository countryRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final WatchlistEntryRepository watchlistEntryRepository;

    public ReferentialSeedService(
            CurrencyRepository currencyRepository,
            CountryRepository countryRepository,
            ExchangeRateRepository exchangeRateRepository,
            WatchlistEntryRepository watchlistEntryRepository
    ) {
        this.currencyRepository = currencyRepository;
        this.countryRepository = countryRepository;
        this.exchangeRateRepository = exchangeRateRepository;
        this.watchlistEntryRepository = watchlistEntryRepository;
    }

    @EventListener(ContextRefreshedEvent.class)
    @Transactional
    public void seedIfEmpty() {
        if (currencyRepository.count() > 0) {
            return;
        }

        Currency mad = saveCurrency("MAD", "Moroccan Dirham", "DH", 2);
        Currency xof = saveCurrency("XOF", "West African CFA Franc", "CFA", 0);
        Currency eur = saveCurrency("EUR", "Euro", "€", 2);

        Country morocco = saveCountry("MA", "Morocco", "+212", mad);
        Country senegal = saveCountry("SN", "Senegal", "+221", xof);
        saveCountry("FR", "France", "+33", eur);

        saveRate(mad, xof, new BigDecimal("61.00"));
        saveRate(mad, eur, new BigDecimal("0.092"));

        WatchlistEntry watchlistEntry = new WatchlistEntry();
        watchlistEntry.setFirstName("Ali");
        watchlistEntry.setLastName("Diop");
        watchlistEntry.setCountry(senegal);
        watchlistEntry.setSource("OFAC-FICTIVE");
        watchlistEntry.setActive(true);
        watchlistEntryRepository.save(watchlistEntry);

        WatchlistEntry moroccoEntry = new WatchlistEntry();
        moroccoEntry.setFirstName("Karim");
        moroccoEntry.setLastName("Benali");
        moroccoEntry.setCountry(morocco);
        moroccoEntry.setSource("OFAC-FICTIVE");
        moroccoEntry.setActive(true);
        watchlistEntryRepository.save(moroccoEntry);
    }

    private Currency saveCurrency(String code, String name, String symbol, int scale) {
        Currency currency = new Currency();
        currency.setCode(code);
        currency.setName(name);
        currency.setSymbol(symbol);
        currency.setScale(scale);
        currency.setActive(true);
        return currencyRepository.save(currency);
    }

    private Country saveCountry(String isoCode, String name, String phonePrefix, Currency currency) {
        Country country = new Country();
        country.setIsoCode(isoCode);
        country.setName(name);
        country.setPhonePrefix(phonePrefix);
        country.setCurrency(currency);
        country.setActive(true);
        return countryRepository.save(country);
    }

    private void saveRate(Currency source, Currency target, BigDecimal rate) {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setSourceCurrency(source);
        exchangeRate.setTargetCurrency(target);
        exchangeRate.setRate(rate);
        exchangeRate.setSource(RateSource.SYSTEM);
        exchangeRate.setActive(true);
        exchangeRateRepository.save(exchangeRate);
    }
}

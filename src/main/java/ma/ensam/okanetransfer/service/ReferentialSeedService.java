package ma.ensam.okanetransfer.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import ma.ensam.okanetransfer.domain.agency.Agency;
import ma.ensam.okanetransfer.domain.agency.Corridor;
import ma.ensam.okanetransfer.domain.agency.FeeGrid;
import ma.ensam.okanetransfer.domain.compliance.WatchlistEntry;
import ma.ensam.okanetransfer.domain.referential.Country;
import ma.ensam.okanetransfer.domain.referential.Currency;
import ma.ensam.okanetransfer.domain.referential.ExchangeRate;
import ma.ensam.okanetransfer.enums.AgencyStatus;
import ma.ensam.okanetransfer.enums.RateSource;
import ma.ensam.okanetransfer.repository.AgencyRepository;
import ma.ensam.okanetransfer.repository.CorridorRepository;
import ma.ensam.okanetransfer.repository.CountryRepository;
import ma.ensam.okanetransfer.repository.CurrencyRepository;
import ma.ensam.okanetransfer.repository.ExchangeRateRepository;
import ma.ensam.okanetransfer.repository.FeeGridRepository;
import ma.ensam.okanetransfer.repository.WatchlistEntryRepository;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReferentialSeedService {

    private final CurrencyRepository currencyRepository;
    private final CountryRepository countryRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final WatchlistEntryRepository watchlistEntryRepository;
    private final AgencyRepository agencyRepository;
    private final CorridorRepository corridorRepository;
    private final FeeGridRepository feeGridRepository;

    public ReferentialSeedService(
            CurrencyRepository currencyRepository,
            CountryRepository countryRepository,
            ExchangeRateRepository exchangeRateRepository,
            WatchlistEntryRepository watchlistEntryRepository,
            AgencyRepository agencyRepository,
            CorridorRepository corridorRepository,
            FeeGridRepository feeGridRepository
    ) {
        this.currencyRepository = currencyRepository;
        this.countryRepository = countryRepository;
        this.exchangeRateRepository = exchangeRateRepository;
        this.watchlistEntryRepository = watchlistEntryRepository;
        this.agencyRepository = agencyRepository;
        this.corridorRepository = corridorRepository;
        this.feeGridRepository = feeGridRepository;
    }

    @EventListener(ContextRefreshedEvent.class)
    @Transactional
    public void seedIfEmpty() {
        if (currencyRepository.count() == 0) {
            seedReferential();
        }
        if (agencyRepository.count() == 0) {
            seedBusinessFinance();
        }
    }

    private void seedReferential() {
        Currency mad = saveCurrency("MAD", "Moroccan Dirham", "DH", 2);
        Currency xof = saveCurrency("XOF", "West African CFA Franc", "CFA", 0);
        Currency eur = saveCurrency("EUR", "Euro", "€", 2);
        Currency usd = saveCurrency("USD", "US Dollar", "$", 2);

        Country morocco = saveCountry("MA", "Morocco", "+212", mad);
        Country senegal = saveCountry("SN", "Senegal", "+221", xof);
        saveCountry("FR", "France", "+33", eur);

        saveRate(mad, xof, new BigDecimal("61.00"));
        saveRate(mad, eur, new BigDecimal("0.092"));
        saveRate(mad, usd, new BigDecimal("0.098"));

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

    private void seedBusinessFinance() {
        Country morocco = countryRepository.findByIsoCode("MA")
                .orElse(countryRepository.findAll().stream().findFirst().orElse(null));
        Country senegal = countryRepository.findByIsoCode("SN")
                .orElse(countryRepository.findAll().stream().skip(1).findFirst().orElse(null));

        if (morocco == null || senegal == null) {
            return;
        }

        saveAgency("AG-CAS-01", "Okane Casablanca Centre", "Boulevard Mohammed V",
                "Casablanca", morocco, new BigDecimal("500000"));
        saveAgency("AG-DAK-01", "Okane Dakar Plateau", "Avenue Léopold Sédar Senghor",
                "Dakar", senegal, new BigDecimal("300000"));

        Corridor maToSn = saveCorridor(morocco, senegal, new BigDecimal("200000"), new BigDecimal("2000000"));
        saveFeeGrid(maToSn, new BigDecimal("100"), new BigDecimal("5000"),
                new BigDecimal("25"), new BigDecimal("1.5"));
        saveFeeGrid(maToSn, new BigDecimal("5001"), new BigDecimal("50000"),
                new BigDecimal("50"), new BigDecimal("1.2"));
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

    private Agency saveAgency(String code, String name, String address, String city,
                              Country country, BigDecimal dailyLimit) {
        Agency agency = new Agency();
        agency.setCode(code);
        agency.setName(name);
        agency.setAddress(address);
        agency.setCity(city);
        agency.setCountry(country);
        agency.setDailyLimit(dailyLimit);
        agency.setStatus(AgencyStatus.ACTIVE);
        return agencyRepository.save(agency);
    }

    private Corridor saveCorridor(Country source, Country destination,
                                  BigDecimal dailyLimit, BigDecimal monthlyLimit) {
        return corridorRepository.findBySourceCountryIdAndDestinationCountryId(
                        source.getId(), destination.getId())
                .orElseGet(() -> {
                    Corridor corridor = new Corridor();
                    corridor.setSourceCountry(source);
                    corridor.setDestinationCountry(destination);
                    corridor.setDailyLimit(dailyLimit);
                    corridor.setMonthlyLimit(monthlyLimit);
                    corridor.setActive(true);
                    return corridorRepository.save(corridor);
                });
    }

    private void saveFeeGrid(Corridor corridor, BigDecimal min, BigDecimal max,
                             BigDecimal fixedFee, BigDecimal percentageFee) {
        FeeGrid grid = new FeeGrid();
        grid.setCorridor(corridor);
        grid.setMinAmount(min);
        grid.setMaxAmount(max);
        grid.setFixedFee(fixedFee);
        grid.setPercentageFee(percentageFee);
        grid.setAgencyCommissionRate(new BigDecimal("40"));
        grid.setCentralCommissionRate(new BigDecimal("60"));
        grid.setValidFrom(LocalDate.now().minusMonths(1));
        grid.setValidTo(LocalDate.now().plusYears(1));
        grid.setActive(true);
        feeGridRepository.save(grid);
    }
}

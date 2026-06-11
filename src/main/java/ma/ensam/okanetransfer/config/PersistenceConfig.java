package ma.ensam.okanetransfer.config;

import jakarta.persistence.EntityManagerFactory;
import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "ma.ensam.okanetransfer.repository")
@ComponentScan(basePackages = {
        "ma.ensam.okanetransfer.service",
        "ma.ensam.okanetransfer.security"
})
public class PersistenceConfig {
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env("OKANE_DB_DRIVER", "org.postgresql.Driver"));
        dataSource.setUrl(env("OKANE_DB_URL", "jdbc:postgresql://localhost:5432/okane-transfer"));
        dataSource.setUsername(env("OKANE_DB_USERNAME", "okane"));
        dataSource.setPassword(env("OKANE_DB_PASSWORD", "1234"));
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(dataSource);
        entityManagerFactory.setPackagesToScan("ma.ensam.okanetransfer.domain");
        entityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactory.setJpaProperties(jpaProperties());
        return entityManagerFactory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    private Properties jpaProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", env("OKANE_HIBERNATE_DDL_AUTO", "none"));
        properties.setProperty("hibernate.dialect", env("OKANE_HIBERNATE_DIALECT", "org.hibernate.dialect.PostgreSQLDialect"));
        properties.setProperty("hibernate.show_sql", env("OKANE_HIBERNATE_SHOW_SQL", "false"));
        properties.setProperty("hibernate.format_sql", env("OKANE_HIBERNATE_FORMAT_SQL", "true"));
        return properties;
    }

    private String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}

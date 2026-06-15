package ma.ensam.okanetransfer.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI okaneTransferOpenApi() {
        final String bearerScheme = "BearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("OkaneTransfer API")
                        .description("Money transfer platform REST API — referential, KYC/AML, notifications and finance modules.")
                        .version("1.0.0")
                        .contact(new Contact().name("OkaneTransfer Team").email("support@okane.ma")))
                .tags(List.of(
                        new Tag().name("Countries").description("M3 — Country referential"),
                        new Tag().name("Currencies").description("M3 — Currency referential"),
                        new Tag().name("Exchange Rates").description("M3 — FX rates, conversion and external sync"),
                        new Tag().name("KYC").description("M3 — Identity verification"),
                        new Tag().name("AML & Compliance").description("M3 — AML alerts and OFAC watchlist"),
                        new Tag().name("Notifications").description("M3 — Multi-channel notifications"),
                        new Tag().name("Mobile Money").description("Simulated mobile wallet payouts"),
                        new Tag().name("Reports").description("Operational reports for admin and manager")
                ))
                .addSecurityItem(new SecurityRequirement().addList(bearerScheme))
                .components(new Components().addSecuritySchemes(
                        bearerScheme,
                        new SecurityScheme()
                                .name(bearerScheme)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT access token from POST /api/v1/auth/login")
                ));
    }
}

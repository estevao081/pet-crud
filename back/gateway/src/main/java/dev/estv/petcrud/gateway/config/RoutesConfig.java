package dev.estv.petcrud.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tabela de roteamento: prefixo de URL -> microserviço.
 * Os paths públicos são os MESMOS do monólito, então o frontend não muda.
 * Note que "/internal/**" NÃO está aqui de propósito: é a API service-to-service
 * e nunca deve ser alcançável pela internet.
 */
@Configuration
public class RoutesConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder,
                               @Value("${services.auth.url}") String authUrl,
                               @Value("${services.user.url}") String userUrl,
                               @Value("${services.pet.url}") String petUrl) {
        return builder.routes()
                .route("auth-service", r -> r.path("/auth/**").uri(authUrl))
                .route("user-service", r -> r.path("/users/**").uri(userUrl))
                .route("pet-service", r -> r.path("/pets/**", "/images/**").uri(petUrl))
                .build();
    }
}

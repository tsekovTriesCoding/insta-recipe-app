package app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "favorites.service")
public class WebClientConfig {

    private String url;

    @Bean
    public WebClient webClient() {
        return WebClient.builder().baseUrl(url).build();
    }
}

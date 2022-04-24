package helpingfriendlybook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HFBConfig {
    @Value("${twitter.access.token}")
    private String accessToken;

    @Value("${twitter.access.token.secret}")
    private String accessTokenSecret;

    @Value("${twitter.api.key}")
    private String apiKey;

    @Value("${twitter.api.key.secret}")
    private String apiKeySecret;

    @Value("${twitter.bearerToken}")
    private String bearerToken;

    public String getAccessToken() {
        return accessToken;
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiKeySecret() {
        return apiKeySecret;
    }

    public String getBearerToken() {
        return bearerToken;
    }
}

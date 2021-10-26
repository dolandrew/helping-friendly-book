package helpingfriendlybook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogliConfig {

    @Value("${twitter.googli.api.key}")
    private String apiKey;

    @Value("${twitter.googli.api.key.secret}")
    private String apiKeySecret;

    @Value("${twitter.googli.access.token}")
    private String accessToken;

    @Value("${twitter.googli.access.token.secret}")
    private String accessTokenSecret;

    public String getApiKey() {
        return apiKey;
    }

    public String getApiKeySecret() {
        return apiKeySecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }
}

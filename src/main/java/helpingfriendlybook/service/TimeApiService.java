package helpingfriendlybook.service;

import helpingfriendlybook.dto.TimeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static java.lang.String.format;

@Service
public class TimeApiService {
    private static final Logger LOG = LoggerFactory.getLogger(TimeApiService.class);

    private final RestTemplate restTemplate;

    @Value("${show.timezone}")
    private String showTimezone;

    public TimeApiService(final RestTemplate template) {
        this.restTemplate = template;
    }

    public String getTimeAtShow() {
        LOG.warn(format("Getting current time in %s...", showTimezone));
        return restTemplate.getForObject(format("https://www.timeapi.io/api/Time/current/zone?timeZone=%s", showTimezone), TimeDTO.class).getTime();
    }
}

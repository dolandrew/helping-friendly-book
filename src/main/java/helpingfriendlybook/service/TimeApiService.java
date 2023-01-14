package helpingfriendlybook.service;

import helpingfriendlybook.dto.TimeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TimeApiService {
    private static final Logger LOG = LoggerFactory.getLogger(TimeApiService.class);

    private final RestTemplate restTemplate;

    public TimeApiService(final RestTemplate template) {
        this.restTemplate = template;
    }

    public String getTimeInNewYork() {
        LOG.warn("Getting current time in New York...");
        return restTemplate.getForObject("https://www.timeapi.io/api/Time/current/zone?timeZone=America/New_York", TimeDTO.class).getTime();
    }
}

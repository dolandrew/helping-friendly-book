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

    public String getTimeInLosAngeles() {
        LOG.warn("Getting current time in Los Angeles...");
        return restTemplate.getForObject("https://www.timeapi.io/api/Time/current/zone?timeZone=America/Los_Angeles", TimeDTO.class).getTime();
    }
}

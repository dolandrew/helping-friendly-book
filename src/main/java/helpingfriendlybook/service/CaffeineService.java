package helpingfriendlybook.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CaffeineService {

    private static final Logger LOG = LoggerFactory.getLogger(CaffeineService.class);

    private final RestTemplate restTemplate;

    private static final String APP_NAME = "helping-friendly-book";

    public CaffeineService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 1000 * 60 * 15)
    public void guayusa() {
        LOG.warn("Caffeinating...");
        restTemplate.getForObject("http://" + APP_NAME + ".herokuapp.com", Void.class);
    }
}
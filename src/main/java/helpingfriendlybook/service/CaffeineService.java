package helpingfriendlybook.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CaffeineService {
    private static final String APP_NAME = "helping-friendly-book";

    private static final Logger LOG = LoggerFactory.getLogger(CaffeineService.class);

    private final RestTemplate restTemplate;

    private final GoogliTweeter googliTweeter;

    public CaffeineService(RestTemplate restTemplate, GoogliTweeter googliTweeter) {
        this.restTemplate = restTemplate;
        this.googliTweeter = googliTweeter;
    }

    @Scheduled(cron = "0 */15 * * * *")
    public void caffeinate() {
        LOG.warn("Caffeinating...");
        try {
            restTemplate.getForObject("http://" + APP_NAME + ".herokuapp.com/index.html", String.class);
        } catch (Exception e) {
            googliTweeter.tweet("Is HFB down? Caught exception: ", e);
        }
    }
}
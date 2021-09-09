package helpingfriendlybook.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Tweeter {

    private static final Logger LOG = LoggerFactory.getLogger(Tweeter.class);

    @Autowired
    private RestTemplate restTemplate;

    public void tweet() {
    }
}
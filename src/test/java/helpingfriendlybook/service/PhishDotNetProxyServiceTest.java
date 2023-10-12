package helpingfriendlybook.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;

class PhishDotNetProxyServiceTest {

    @Test
    void getShowsForDate() {

        final LocalDate today = LocalDate.now(ZoneId.of("UTC-8"));

        String song =
                new PhishDotNetProxyService(new RestTemplate()).getLastPlayedSongForDate(today.getDayOfMonth(), today.getMonthValue(), today.getYear() );

        System.out.println(song);
    }
}
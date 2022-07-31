package helpingfriendlybook.service;

import helpingfriendlybook.HelpingFriendlyBookApplication;
import helpingfriendlybook.dto.TweetResponseDTO;
import io.restassured.RestAssured;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static helpingfriendlybook.service.PhishDotNetProxyService.getShowsFromResponse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        classes = HelpingFriendlyBookApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@RunWith(SpringRunner.class)
public class OnThisDayServiceIntegrationTest {

    @LocalServerPort
    private int serverPort;

    @MockBean
    private TwitterService twitterService;

    @Autowired
    private OnThisDayService onThisDayService;

     @MockBean
     private PhishDotNetProxyService phishDotNetProxyService;

    @Before
    public void setup() throws IOException {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.port = serverPort;

        String show1 = IOUtils.toString(this.getClass().getResource("/show_1_27_1988.html"));
        doReturn(getShowsFromResponse(show1))
                .when(phishDotNetProxyService).getShowsForDate(anyInt(), anyInt(), isNull());

        TweetResponseDTO tweetResponse = new TweetResponseDTO();
        tweetResponse.setId(2L);
        doReturn(tweetResponse).when(twitterService).tweet(anyString());
        doReturn(tweetResponse).when(twitterService).tweet(anyString(), anyLong());
    }

    @Test
    public void testOnThisDay() {
        onThisDayService.tweetOnThisDayOrRandomShow();

        verify(twitterService).tweet(contains("#OnThisDay\n" +
                "Wednesday 01/27/1988\n" +
                "Gallagher's                     Waitsfield,                     VT  \n" +
                "phish.net/setlists/phish-january-27-1988-gallaghers-waitsfield-vt-usa.html\n" +
                "\n" +
                "\n" +
                "\n" +
                "#phish #phishcompanion"));
        verify(twitterService).tweet(contains("Set 1: Funky Bitch, Mustang Sally, Bag, Possum, JJLC, Sneakin' Sally, Alumni, LTJP, Alumni, 'A' Train, GTBT\n" +
                "Set 2: Wilson, Slave, Corinna, Fire, Fluffhead, Divided, Curtis Loew, YEM, Sloth, Whipping Post\n" +
                "Set 3: Fee, Lizards, Suzy, Golgi, Bike, BBFCFM, Camel Walk, Hood"), anyLong());
        verify(twitterService).tweet(contains("This show contained the 1st known version of The Lizards."), anyLong());
    }
}

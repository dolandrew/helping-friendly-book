package helpingfriendlybook.service;

import helpingfriendlybook.HelpingFriendlyBookApplication;
import helpingfriendlybook.dto.TweetResponseDTO;
import io.restassured.RestAssured;
import org.apache.commons.io.IOUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static helpingfriendlybook.service.PhishDotNetProxyService.getShowsFromResponse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        classes = HelpingFriendlyBookApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ExtendWith(SpringExtension.class)
public class OnThisDayServiceIntegrationTest {

    @LocalServerPort
    private int serverPort;

    @MockBean
    private TwitterService twitterService;

    @Autowired
    private OnThisDayService onThisDayService;

    @MockBean
    private GoogliTweeter googliTweeter;

    @MockBean
    private PhishDotNetProxyService phishDotNetProxyService;

    @BeforeEach
    public void setup() throws IOException {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.port = serverPort;

        String show1 = IOUtils.toString(this.getClass().getResource("/show_1_27_1988.html"));
        doReturn(getShowsFromResponse(show1))
                .when(phishDotNetProxyService).getShowsForDate(anyInt(), anyInt(), isNull());

        Document document = mock(Document.class);
        Element element = mock(Element.class);
        Elements elements = mock(Elements.class);
        Element childElement = mock(Element.class);
        doReturn("Overall: 5.0/5 (1 rating)").when(childElement).wholeText();
        doReturn(childElement).when(element).child(1);
        doReturn(element).when(elements).get(0);
        doReturn(elements).when(document).getElementsByClass(eq("permalink-rating"));


        Element element2 = mock(Element.class);
        Elements elements2 = mock(Elements.class);
        doReturn("https://phish.in/2022-12-31").when(element2).attr("href");
        doReturn(element2).when(elements2).get(0);
        doReturn(elements2).when(document).getElementsByClass(eq("linktrack"));

        doReturn(document).when(phishDotNetProxyService).getShow(anyString());

        TweetResponseDTO tweetResponse = new TweetResponseDTO();
        tweetResponse.setId("2");
        doReturn(tweetResponse).when(twitterService).tweet(anyString(), isNull());
        doReturn(tweetResponse).when(twitterService).tweet(anyString(), anyString());
    }

    @Test
    public void testOnThisDay() {
        onThisDayService.tweetOnThisDayOrRandomShow();

        verify(twitterService).tweet(contains("""
                #OnThisDay
                1/27/1988
                Gallagher's                     Waitsfield,                     VT\s
                phish.net/setlists/phish-january-27-1988-gallaghers-waitsfield-vt-usa.html

                ⭐ 5.0/5 (1 rating)
                https://phish.in/2022-12-31

                #phish #phishcompanion"""), isNull());
        verify(twitterService).tweet(contains("""
                Set 1: Funky Bitch, Mustang Sally, Bag, Possum, JJLC, Sneakin' Sally, Alumni, LTJP, Alumni, 'A' Train, GTBT
                Set 2: Wilson, Slave, Corinna, Fire, Fluffhead, Divided, Curtis Loew, YEM, Sloth, Whipping Post
                Set 3: Fee, Lizards, Suzy, Golgi, Bike, BBFCFM, Camel Walk, Hood
                Encore: Fee Reprise"""), anyString());
        verify(twitterService).tweet(contains("This show contained the 1st known tz of The Lizards."), anyString());
    }
}

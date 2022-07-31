package helpingfriendlybook.controller;

import helpingfriendlybook.HelpingFriendlyBookApplication;
import helpingfriendlybook.dto.DataDTO;
import helpingfriendlybook.dto.TwitterResponseDTO;
import helpingfriendlybook.service.SongStatsService;
import helpingfriendlybook.service.TwitterService;
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SpringBootTest(
        classes = HelpingFriendlyBookApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@RunWith(SpringRunner.class)
@ActiveProfiles("local")
public class IntegrationTest {

    @LocalServerPort
    private int serverPort;

    @MockBean
    private TwitterService twitterService;

    @Autowired
    private SongStatsService songStatsService;

    // TODO
    // @MockBean
    // private PhishDotNetProxyService phishDotNetProxyService;

    private final String phishFromTheRoadId = "153850397";

    @Before
    public void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.port = serverPort;
    }

    @Test
    public void test_songStats_happyPath() {
        TwitterResponseDTO body = mockResponse("The Lizards");

        songStatsService.listenToPhishFTR();

        verify(twitterService).favoriteTweetById(body.getData().get(0).getId());
        verify(twitterService).tweet(contains("First played: 1988-01-27 at Gallagher's \n" +
                "https://www.phish.net/song/the-lizards\n\n" +
                "#phish #phishstats #phishcompanion #test"));
    }

    @Test
    public void test_songStats_setStart() {
        TwitterResponseDTO body = mockResponse("SET ONE: The Lizards");

        songStatsService.listenToPhishFTR();

        verify(twitterService).favoriteTweetById(body.getData().get(0).getId());
        verify(twitterService).tweet(contains("\uD83C\uDF89 SET ONE started at "));
        verify(twitterService).tweet(contains("First played: 1988-01-27 at Gallagher's \n" +
                "https://www.phish.net/song/the-lizards\n\n" +
                "#phish #phishstats #phishcompanion #test"));
    }

    @Test
    public void test_songStats_ignoresPhotos() {
        TwitterResponseDTO body = mockResponse("\uD83D\uDCF8");

        songStatsService.listenToPhishFTR();

        verify(twitterService).favoriteTweetById(body.getData().get(0).getId());
        verify(twitterService).getTweetsForUserIdInLast(eq("153850397"), anyString());
        verifyNoMoreInteractions(twitterService);
    }

    @Test
    public void test_songStats_ignoresReneHuemer() {
        TwitterResponseDTO body = mockResponse("@rene_huemer");

        songStatsService.listenToPhishFTR();

        verify(twitterService).favoriteTweetById(body.getData().get(0).getId());
        verify(twitterService).getTweetsForUserIdInLast(eq("153850397"), anyString());
        verifyNoMoreInteractions(twitterService);
    }

    @Test
    public void test_songStats_ignoresGoingBackIntoASong() {
        TwitterResponseDTO body = mockResponse(">> The Lizards");

        songStatsService.listenToPhishFTR();

        verify(twitterService).favoriteTweetById(body.getData().get(0).getId());
        verify(twitterService).getTweetsForUserIdInLast(eq(phishFromTheRoadId), anyString());
        verifyNoMoreInteractions(twitterService);
    }

    @Test
    public void test_songStats_ignoresLinks() {
        TwitterResponseDTO body = mockResponse("https://t.co/someLink");

        songStatsService.listenToPhishFTR();

        verify(twitterService).favoriteTweetById(body.getData().get(0).getId());
        verify(twitterService).getTweetsForUserIdInLast(eq("153850397"), anyString());
        verifyNoMoreInteractions(twitterService);
    }

    @Test
    public void test_songStats_ignoresRetweets() {
        TwitterResponseDTO body = mockResponse("RT Check out this tweet!");

        songStatsService.listenToPhishFTR();

        verify(twitterService).favoriteTweetById(body.getData().get(0).getId());
        verify(twitterService).getTweetsForUserIdInLast(eq("153850397"), anyString());
        verifyNoMoreInteractions(twitterService);
    }

    private TwitterResponseDTO mockResponse(String text) {
        TwitterResponseDTO body = new TwitterResponseDTO();
        List<DataDTO> tweets = new ArrayList<>();
        DataDTO tweet = new DataDTO();
        tweet.setText(text);
        String tweetId = "tweetId";
        tweet.setId(tweetId);
        tweets.add(tweet);
        body.setData(tweets);

        doReturn(ResponseEntity.ok(body)).when(twitterService).getTweetsForUserIdInLast(eq("153850397"), anyString());
        return body;
    }
}

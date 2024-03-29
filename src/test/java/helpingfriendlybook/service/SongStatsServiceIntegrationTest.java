//package helpingfriendlybook.service;
//
//import helpingfriendlybook.HelpingFriendlyBookApplication;
//import helpingfriendlybook.dto.DataDTO;
//import helpingfriendlybook.dto.TwitterResponseDTO;
//import io.restassured.RestAssured;
//import org.apache.commons.io.IOUtils;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.boot.web.server.LocalServerPort;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import static helpingfriendlybook.service.PhishDotNetProxyService.getShowsFromResponse;
//import static org.mockito.ArgumentMatchers.anyInt;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.contains;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.doReturn;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoMoreInteractions;
//
//@SpringBootTest(
//        classes = HelpingFriendlyBookApplication.class,
//        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
//)
//@ExtendWith(SpringExtension.class)
//public final class SongStatsServiceIntegrationTest {
//
//    @LocalServerPort
//    private int serverPort;
//
//    @MockBean
//    private TwitterService twitterService;
//
//    @Autowired
//    private SongStatsService songStatsService;
//
//    @MockBean
//    private TimeApiService timeApiService;
//
//    @MockBean
//    private PhishDotNetProxyService phishDotNetProxyService;
//
//    @BeforeEach
//    public void setup() throws IOException {
//        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
//        RestAssured.port = serverPort;
//        String songs = IOUtils.toString(this.getClass().getResource("/songs_response.html"));
//        doReturn(songs).when(phishDotNetProxyService).getSongs();
//
//        String show1 = IOUtils.toString(this.getClass().getResource("/show_1_27_1988.html"));
//        String show2 = IOUtils.toString(this.getClass().getResource("/show_7_30_2022.html"));
//        doReturn(getShowsFromResponse(show1)).doReturn(getShowsFromResponse(show2))
//                .when(phishDotNetProxyService).getShowsForDate(anyInt(), anyInt(), anyInt());
//
//        doReturn("9:35").when(timeApiService).getTimeAtShow();
//    }
//
//    @Test
//    public void testSongStatsDividedSky() {
//        TwitterResponseDTO body = mockResponse("Divided Sky");
//
//        songStatsService.listenToPhishFTR();
//
//        verify(twitterService).favoriteTweetById(body.getData().get(0).getId());
//        verify(twitterService).tweet(contains("""
//                428th Divided Sky
//                Last played: 2022-07-23 at Gallagher's\s
//                Gap: 5
//                First played: 1987-05-11 at Merriweather Post Pavilion\s
//                https://www.phish.net/song/divided-sky
//
//                #phish #phishstats #phishcompanion\s"""));
//    }
//
//    @Test
//    public void testSongStatsSetOneACDCBag() {
//        TwitterResponseDTO body = mockResponse("SET ONE: AC/DC Bag");
//
//        songStatsService.listenToPhishFTR();
//
//        verify(twitterService).favoriteTweetById(body.getData().get(0).getId());
//        verify(twitterService).tweet(contains("\uD83C\uDF89 SET ONE started at "));
//        verify(twitterService).tweet(contains("""
//                325th AC/DC Bag
//                Last played: 2022-07-20 at Gallagher's\s
//                Gap: 7
//                First played: 1986-04-01 at Merriweather Post Pavilion\s
//                https://www.phish.net/song/acdc-bag
//
//                #phish #phishstats #phishcompanion\s"""));
//    }
//
//    @Test
//    public void testSongStatsSetTwoAlumniBlues() {
//        TwitterResponseDTO body = mockResponse("SET TWO: Alumni Blues");
//
//        songStatsService.listenToPhishFTR();
//
//        verify(twitterService).favoriteTweetById(body.getData().get(0).getId());
//        verify(twitterService).tweet(contains("\uD83D\uDC20 SET TWO started at "));
//        verify(twitterService).tweet(contains("""
//                201st Alumni Blues
//                Last played: 2021-09-04 at Gallagher's\s
//                Gap: 48
//                First played: 1985-03-16 at Merriweather Post Pavilion\s
//                https://www.phish.net/song/alumni-blues
//
//                #phish #phishstats #phishcompanion\s"""));
//    }
//
//    @Test
//    public void testSongStatsSetThreeAndSoToBed() {
//        TwitterResponseDTO body = mockResponse("SET THREE: And So To Bed");
//
//        songStatsService.listenToPhishFTR();
//
//        verify(twitterService).favoriteTweetById(body.getData().get(0).getId());
//        verify(twitterService).tweet(contains("\uD83D\uDD7A SET THREE started at "));
//        verify(twitterService).tweet(contains("""
//                2nd And So To Bed
//                Last played: 2021-10-15 at Gallagher's\s
//                Gap: 46
//                First played: 2021-10-15 at Merriweather Post Pavilion\s
//                https://www.phish.net/song/and-so-to-bed
//
//                #phish #phishstats #phishcompanion\s"""));
//    }
//
//    @Test
//    public void testSongStatsEncoreYEM() {
//        TwitterResponseDTO body = mockResponse("ENCORE: You Enjoy Myself");
//
//        songStatsService.listenToPhishFTR();
//
//        verify(twitterService).favoriteTweetById(body.getData().get(0).getId());
//        verify(twitterService).tweet(contains("⭕️ ENCORE started at "));
//        verify(twitterService).tweet(contains("""
//                624th You Enjoy Myself
//                Last played: 2022-07-22 at Gallagher's\s
//                Gap: 6
//                First played: 1986-02-03 at Merriweather Post Pavilion\s
//                https://www.phish.net/song/you-enjoy-myself
//
//                #phish #phishstats #phishcompanion\s"""));
//    }
//
//    @Test
//    public void testSongStatsIgnoresPhotos() {
//        TwitterResponseDTO body = mockResponse("\uD83D\uDCF8");
//
//        songStatsService.listenToPhishFTR();
//
//        verify(twitterService).favoriteTweetById(body.getData().get(0).getId());
//        verify(twitterService).getTweetsForUserIdInLast(eq("153850397"), anyString());
//        verifyNoMoreInteractions(twitterService);
//    }
//
//    @Test
//    public void testSongStatsIgnoresReneHuemer() {
//        TwitterResponseDTO body = mockResponse("@rene_huemer");
//
//        songStatsService.listenToPhishFTR();
//
//        verify(twitterService).favoriteTweetById(body.getData().get(0).getId());
//        verify(twitterService).getTweetsForUserIdInLast(eq("153850397"), anyString());
//        verifyNoMoreInteractions(twitterService);
//    }
//
//    @Test
//    public void testSongStatsIgnoresGoingBackIntoASong() {
//        TwitterResponseDTO body = mockResponse(">> The Lizards");
//
//        songStatsService.listenToPhishFTR();
//
//        verify(twitterService).getTweetsForUserIdInLast(eq("153850397"), anyString());
//        verify(twitterService).favoriteTweetById(body.getData().get(0).getId());
//        verifyNoMoreInteractions(twitterService);
//    }
//
//    @Test
//    public void testSongStatsIgnoresLinks() {
//        TwitterResponseDTO body = mockResponse("https://t.co/someLink");
//
//        songStatsService.listenToPhishFTR();
//
//        verify(twitterService).favoriteTweetById(body.getData().get(0).getId());
//        verify(twitterService).getTweetsForUserIdInLast(eq("153850397"), anyString());
//        verifyNoMoreInteractions(twitterService);
//    }
//
//    @Test
//    public void testSongStatsIgnoresRetweets() {
//        TwitterResponseDTO body = mockResponse("RT Check out this tweet!");
//
//        songStatsService.listenToPhishFTR();
//
//        verify(twitterService).favoriteTweetById(body.getData().get(0).getId());
//        verify(twitterService).getTweetsForUserIdInLast(eq("153850397"), anyString());
//        verifyNoMoreInteractions(twitterService);
//    }
//
//    private TwitterResponseDTO mockResponse(final String text) {
//        TwitterResponseDTO body = new TwitterResponseDTO();
//        List<DataDTO> tweets = new ArrayList<>();
//        DataDTO tweet = new DataDTO();
//        tweet.setText(text);
//        String tweetId = "tweetId";
//        tweet.setId(tweetId);
//        tweets.add(tweet);
//        body.setData(tweets);
//
//        doReturn(ResponseEntity.ok(body)).when(twitterService).getTweetsForUserIdInLast(eq("153850397"), anyString());
//        return body;
//    }
//}

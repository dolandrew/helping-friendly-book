# Helping Friendly Book

www.twitter.com/PhishCompanion

The Helping Friendly Book is a Java Spring Boot application deployed to Heroku that runs the Helping Friendly Bot (@PhishCompanion) account on Twitter. It does 4 main things:

1. Listens to another Twitter account, @Phish_FTR, which tweets the song name as soon as Phish plays it live. When it finds a song name, it goes to phish.net to look up additional stats about the song and tweets that.
![Screen Shot 2022-07-30 at 5 14 09 PM](https://user-images.githubusercontent.com/28452598/182011942-d328e560-3843-4c70-9cff-44ec0027c229.jpg)

2. Posts a “Show On This Day” once per day at 11AM PT. This tweet is threaded and includes the date, location, setlist and setlist notes of the random selected show on this day.
![OnThisDay](https://user-images.githubusercontent.com/28452598/182011946-78ed3fe7-2b51-4e08-8df0-679a090754ca.jpg)

3. Posts set start times.
![setstarttime](https://user-images.githubusercontent.com/28452598/182011949-7be3a89e-0995-4bcf-b1a7-2725b5646cda.jpg)

4. Posts a #Pick5 on show days -- HFB uses an algorithm to find five songs that are likely to be played and tweets them out before the show starts. 

*All data is pulled from phish.net.*

Helping Friendly Book has some other features that leverage the Twitter API:
- Favoriting a tweet (this is used to like @Phish_FTR’s tweets)
- Following users that like another users tweet
- Unfollowing users that don’t follow back
- Threaded tweets (tweets with replies)

![IMG_2115](https://user-images.githubusercontent.com/28452598/181653852-cf85cb39-95fc-4c61-9a75-d22c48362f0f.PNG)

# Helping Friendly Book

twitter.com/PhishCompanion

Helping Friendly Book is a Java Spring Boot application deployed to Heroku that runs the Helping Friendly Bot (@PhishCompanion) account on Twitter.

The Helping Friendly Bot does 3 main things:

- Listens to another Twitter account, @Phish_FTR, which tweets the song name as soon as Phish plays it live. When it finds a song name, it goes to phish.net to look up additional stats about the song and tweets that.
- Posts a “Show On This Day” once per day at 11AM PT. This tweet is threaded and includes the date, location, setlist and setlist notes of the random selected show on this day. This data is pulled from phish.net.
- Posts set start times.

All data is pulled from phish.net.

Helping Friendly Book has some other features that leverage the Twitter API:
- Favoriting a tweet (this is used to like @Phish_FTR’s tweets)
- Following users that like another users tweet
- Unfollowing users that don’t follow back

Started in September 2021, it has almost 1,000 followers!

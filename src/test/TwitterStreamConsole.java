package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * タイムラインをキーワードで検索してデータを取得するサンプル。
 */
public class TwitterStreamConsole {
	
	private static Logger logger = Logger.getLogger(TwitterStreamConsole.class.getName());
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String keyword = (args.length > 0) ? args[0] : "aaa";
			TwitterStreamConsole app = new TwitterStreamConsole();
			//app.start_statusnet();
			app.start_twitter(keyword, Boolean.getBoolean("bExpire"));
		} catch(Throwable ex) {
			logger.log(Level.SEVERE, ex.getClass().toString());
			logger.log(Level.SEVERE, ex.toString(), ex);
		}
	}

	// 「つぶマップ」のキー
    private static final String CONSUMER_KEY = "cyiWV4WGg9WkQq93AMURg";
    private static final String CONSUMER_SECRET = "3qHf93ajA8SJPhl2r1cV9kT3Z5ocaiXiWLBTIT6nwUg";
    
    private void start_twitter(final String keyword, boolean bExpire)
    		throws TwitterException, IOException, BackingStoreException {
    	
    	TwitterStream twStream = new TwitterStreamFactory().getInstance();
    	twStream.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        String _accessToken = prefs.get("accessToken.accessToken", null);
        String _accessSecret = prefs.get("accessToken.tokenSecret", null);
        if (!bExpire && _accessToken != null) {
        	System.out.println("+++ accessToken.accessToken=" + _accessToken);
        	System.out.println("+++ accessToken.tokenSecret=" + _accessSecret);
        	AccessToken accessToken = new AccessToken(_accessToken, _accessSecret);
        	twStream.setOAuthAccessToken(accessToken);
        } else {
          System.out.println("Open the following URL and grant access to your account:");
          RequestToken requestToken = twStream.getOAuthRequestToken();
          System.out.println(requestToken.getAuthorizationURL());
          System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
          BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
          String pin = br.readLine();
          if(pin.length() > 0){
            AccessToken accessToken = twStream.getOAuthAccessToken(requestToken, pin);
            if (accessToken != null) {
            	prefs.put("accessToken.accessToken", accessToken.getToken());
            	prefs.put("accessToken.tokenSecret", accessToken.getTokenSecret());
            	prefs.flush();
            }
          }
        }

		twStream.addListener(new StatusListener() {

			@Override
			public void onStatus(Status tweet) {
				User tweetuser = tweet.getUser();
				GeoLocation pos = tweet.getGeoLocation();
				System.out.printf("keyword=%s; ID=%d; %s %s; pos=%f,%f%n%s %n-----------%n",
						keyword,
						tweet.getId(),
						tweet.getCreatedAt().toString(),
						tweetuser.getName(),
						(pos != null ? pos.getLatitude() : 0),
						(pos != null ? pos.getLongitude() : 0),
						tweet.getText());
			}

			@Override
			public void onException(Exception e) {
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice sdn) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onScrubGeo(long lat, long lng) {
			}

			@Override
			public void onStallWarning(StallWarning arg0) {
			}

			@Override
			public void onTrackLimitationNotice(int i) {
			}
			
		});
        
		FilterQuery filterquery = new FilterQuery();

		//streamをユーザIDで絞込
//		String screenName = "ユーザID";
//		User user = twStream.showUser(screenName);
//		long[] followers = new long[] {user.getId()};
//		filterquery.follow(followers);

		String[] track = keyword.split(",");
		//String[] track = new String[] { keyword };
		FilterQuery fQuery =  filterquery.track(track);
		
		twStream.filter(filterquery);
		
		/*
		Query q = new Query();
        q.setGeoCode(loc, 5, Query.KILOMETERS);
        //q.setQuery("渋滞 OR 事故 OR 激混み -RT");
        q.setQuery("人身事故 OR 運転を見合わせています OR 運転中止 OR 運転再開 OR 運転を再開 OR 遅延 -RT");
        QueryResult qr = twitter.search(q);
        List<Status> tweets = qr.getTweets();
        int i = 0;
		for(Status tweet : tweets) {
			i++;
			User tweetuser = tweet.getUser();
			GeoLocation pos = tweet.getGeoLocation();
			System.out.printf("[%d] %s %s%npos=%f,%f%n%s %n-----------%n",
					tweet.getId(),
					tweet.getCreatedAt().toString(),
					tweetuser.getName(),
					(pos != null ? pos.getLatitude() : 0),
					(pos != null ? pos.getLongitude() : 0),
					tweet.getText());
		}
		*/
	}
}

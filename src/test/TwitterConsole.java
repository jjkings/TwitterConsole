package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * タイムラインをキーワードで検索してツイート取得するサンプル。
 */
public class TwitterConsole {
	
	private static Logger logger = Logger.getLogger(TwitterConsole.class.getName());
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			TwitterConsole app = new TwitterConsole();
			//app.start_statusnet();
			app.start_twitter(Boolean.getBoolean("bExpire"));
		} catch(Throwable ex) {
			logger.log(Level.SEVERE, ex.getClass().toString());
			logger.log(Level.SEVERE, ex.toString(), ex);
		}
	}

	/*
    private void start_statusnet() throws TwitterException {
    	
        String baseURL = "http://localhost/statusnet/api/";

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setRestBaseURL(baseURL);
//        cb.setOAuthAccessTokenURL(baseURL + "oauth/access_token");
//        cb.setOAuthAuthorizationURL(baseURL + "oauth/authorize");
//        cb.setOAuthRequestTokenURL(baseURL + "oauth/request_token");
        cb.setIncludeEntitiesEnabled(true);
        cb.setJSONStoreEnabled(true);
        Configuration conf = cb.build() ;

        //BasicAuthorization auth = new BasicAuthorization("DejimaSaburo", "ianywhere");
        BasicAuthorization auth = new BasicAuthorization("admin", "admin");
        Twitter twitter = new TwitterFactory(conf).getInstance(auth);
        
        ResponseList<Status> tweets = twitter.getHomeTimeline();
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
	}
	*/
	
	// 「つぶマップ」のキー
    private static final String CONSUMER_KEY = "cyiWV4WGg9WkQq93AMURg";
    private static final String CONSUMER_SECRET = "3qHf93ajA8SJPhl2r1cV9kT3Z5ocaiXiWLBTIT6nwUg";
    
    private void start_twitter(boolean bExpire) throws TwitterException, IOException, BackingStoreException {
    	
        // OAuth
    	Twitter twitter = TwitterFactory.getSingleton();
        twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        String _accessToken = prefs.get("accessToken.accessToken", null);
        String _accessSecret = prefs.get("accessToken.tokenSecret", null);
        if (!bExpire && _accessToken != null) {
        	System.out.println("+++ accessToken.accessToken=" + _accessToken);
        	System.out.println("+++ accessToken.tokenSecret=" + _accessSecret);
        	AccessToken accessToken = new AccessToken(_accessToken, _accessSecret);
            twitter.setOAuthAccessToken(accessToken);
        } else {
          System.out.println("Open the following URL and grant access to your account:");
          RequestToken requestToken = twitter.getOAuthRequestToken();
          System.out.println(requestToken.getAuthorizationURL());
          System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
          BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
          String pin = br.readLine();
          if(pin.length() > 0){
            AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, pin);
            if (accessToken != null) {
            	prefs.put("accessToken.accessToken", accessToken.getToken());
            	prefs.put("accessToken.tokenSecret", accessToken.getTokenSecret());
            	prefs.flush();
            }
          }
        }
        // Timeline
        //ResponseList<Status> tweets = twitter.getHomeTimeline();
        
        //GeoLocation loc = new GeoLocation(35.170721,136.88098); 	// Nagoya
        //GeoLocation loc = new GeoLocation(35.172335,136.908102); 	// Sakae
        //GeoLocation loc = new GeoLocation(34.94287,137.198982); 	// Okazaki IC
        GeoLocation loc = new GeoLocation(35.505819,139.482079); 	// Yokohama Machida IC
        //GeoLocation loc = new GeoLocation(35.663015,139.732189); 	// Roppongi
        //GeoLocation loc = new GeoLocation(37.420754,141.033125); 	// Fukushima dai1
        Query q = new Query();
        q.setGeoCode(loc, 5, Query.KILOMETERS);
        String query = "人身事故 OR 運転を見合わせています OR 運転中止 OR 運転再開 OR 運転を再開 OR 遅延 -RT";
		System.out.printf("Query=%s%n", query);
		//q.setQuery("渋滞 OR 事故 OR 激混み -RT");
        q.setQuery(query);
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
	}
}

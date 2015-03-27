package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
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
			String keyword = (args.length > 0) ? args[0] : null;
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
    
    private int count;
    
    private SimpleDateFormat dtformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
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
				String text = tweet.getText();
				if (_isValidText(text)) {
					System.out.println("Count: " + (++count));
					System.out.println("Keyword: " + keyword);
					System.out.println("ID: " + tweet.getId());
					System.out.println("Date: " + dtformat.format(tweet.getCreatedAt()));
					System.out.println("Pos: " + (pos != null ? pos.getLatitude() : 0) + "," + (pos != null ? pos.getLongitude() : 0));
					System.out.println("User: " + tweetuser.getScreenName());
					System.out.println("UserName: " + tweetuser.getName());
					System.out.println("StatusesCount: " + tweetuser.getStatusesCount());
					System.out.println("Friends: " + tweetuser.getFriendsCount());
					System.out.println("Favourites: " + tweetuser.getFavouritesCount());
					System.out.println("Followers: " + tweetuser.getFollowersCount());
					System.out.println(text);
					System.out.println("-----------");
				}
			}

			// 日本語のツイートのみ選別
			private boolean _isValidText(String text) {
				for(int p = 0; p < text.length(); p ++) {
					char c = text.charAt(p);
					if ((c >= '\u3000' && c <= '\u30ff') || (c >= '\u4e00' && c <= '\u9fff') || (c >= '\uff00' && c <= '\uff9f')) {
						return true;
					}
				}
				return false;
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
        

		//streamをユーザIDで絞込
//		FilterQuery filterquery = new FilterQuery();
//		String screenName = "ユーザID";
//		User user = twStream.showUser(screenName);
//		long[] followers = new long[] {user.getId()};
//		filterquery.follow(followers);

		if (keyword != null) {
			// キーワード指定時はフィルタ抽出
			String[] track = keyword.split(",");
			FilterQuery filterquery = new FilterQuery();
			FilterQuery fQuery =  filterquery.track(track);
			twStream.filter(filterquery);
		} else {
			// キーワード未指定時はサンプル抽出
			twStream.sample();
		}
	}
}

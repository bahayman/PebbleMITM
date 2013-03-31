package me.baruch.pebblemitm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;

public class LocalService extends Service {

	public static final String ORIGINAL_INTENT_KEY = "me.baruch.pebblemitm.ORIGINAL_INTENT";
	public static final String ACTION_UPDATE_DISPLAY = "me.baruch.pebblemitm.UPDATE_DISPLAY";
	public static final String ACTION_RECEIVE_INTERACTION = "me.baruch.pebblemitm.RECEIVE_INTERACTION";
	
	private List<MenuItem> mMenu = null;
	private Integer mCurrentMenuItemIndex = null;
	private MenuItem mSelectedApp = null;
	
	private long mLastPlayPausePressTimestamp = 0;
	
	@Override
	public void onCreate() {
		super.onCreate();
		//updateDisplay("Pebble", "MITM", "Created");
		
		Log.d("DEBUG", "MITM Service Created");
		
		mMenu = new ArrayList<MenuItem>();
		mMenu.add(new MenuItem("Music", "music", false));
		mMenu.add(new MenuItem("Authenticator", "com.google.android.apps.authenticator2"));
		mMenu.add(new MenuItem("About", "about"));
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		//updateDisplay("Pebble", "MITM", "Destroyed");
		
		Log.d("DEBUG", "MITM Service Destroyed");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("DEBUG", "MITM OnStartCommand: " + intent);
		try {
			Intent originalIntent = (Intent) intent.getParcelableExtra(ORIGINAL_INTENT_KEY);
			processIntent(originalIntent, startId);
		} catch (NullPointerException exception) { }
		
		return START_STICKY;
	}
	
	private void processIntent(Intent intent, int startId) {		
		if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
			KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

			if (event == null) {
				return;
			}
			
			int action = event.getAction();
			int keycode = event.getKeyCode();

			if (action == KeyEvent.ACTION_DOWN &&
				keycode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE &&
				mSelectedApp != null) {
				if (event.getDownTime() - mLastPlayPausePressTimestamp < 500) {
					mSelectedApp = null;
					displayMenu();
					return;
				}
				mLastPlayPausePressTimestamp = event.getDownTime();
			} 
			
			if (mSelectedApp == null) {
				if (action == KeyEvent.ACTION_DOWN) {
					if (keycode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
						if (mCurrentMenuItemIndex == null) {
							mCurrentMenuItemIndex = 0;
							displayMenu();
						} else {
							mSelectedApp = mMenu.get(mCurrentMenuItemIndex);
							processIntent(intent, startId);
						}
					} else if (keycode == KeyEvent.KEYCODE_MEDIA_NEXT ||
							   keycode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
						mCurrentMenuItemIndex = Utilities.coalesce(mCurrentMenuItemIndex, 0) + (keycode == KeyEvent.KEYCODE_MEDIA_NEXT ? 1 : -1);
						if (mCurrentMenuItemIndex >= mMenu.size()) {
							mCurrentMenuItemIndex = 0;
						} else if (mCurrentMenuItemIndex < 0) {
							mCurrentMenuItemIndex = mMenu.size() - 1;
						}
						
						displayMenu();
					}
				}
			} else {
				if (mSelectedApp.packageName.equals("about")) {
					if (action == KeyEvent.ACTION_DOWN) {
						sendNotification("Pebble MITM", "Pebble MITM (Man in the Middle) allows one to use the Pebble Music application to control other applications.");
						mSelectedApp = null;
						displayMenu();
					}
				} else {
					if (mSelectedApp.packageName.equals("music")) {
						mSelectedApp.packageName = PreferenceManager.getDefaultSharedPreferences(this).getString("music_package_name", null);
					}

					startActivity(getPackageManager().getLaunchIntentForPackage(mSelectedApp.packageName));
					
					if (mSelectedApp.replaceAction) {
						intent.setAction(ACTION_RECEIVE_INTERACTION);
					}
					intent.setPackage(mSelectedApp.packageName);
					intent.setComponent(null);
					
					Log.d("DEBUG", "Sending intent: " + intent);
					
					sendBroadcast(intent);
				}
			}
		} else if (ACTION_UPDATE_DISPLAY.equals(intent.getAction())) {
			updateDisplay(
					intent.getStringExtra("line1"),
					intent.getStringExtra("line2"),
					intent.getStringExtra("line3")
			);
		}
	}
	
	private void displayMenu() {
		String line1 = null, line2 = null, line3 = null;
		if ((mCurrentMenuItemIndex - 1) >= 0) {
			line1 = mMenu.get(mCurrentMenuItemIndex - 1).title;
		}
		if (mCurrentMenuItemIndex >= 0) {
			line2 = mMenu.get(mCurrentMenuItemIndex).title;
		}
		if ((mCurrentMenuItemIndex + 1) >= 0 &&
			(mCurrentMenuItemIndex + 1) < mMenu.size()) {
			line3 = mMenu.get(mCurrentMenuItemIndex + 1).title;
		}
		updateDisplay(line1, line2, line3);
	}
	
	private void updateDisplay(String line1, String line2, String line3) {
		Intent nowPlayingIntent = new Intent("com.getpebble.action.NOW_PLAYING");
		nowPlayingIntent.setPackage("com.getpebble.android");
		nowPlayingIntent.putExtra("artist", Utilities.coalesce(line1, ""));
		nowPlayingIntent.putExtra("track", Utilities.coalesce(line2, ""));
		nowPlayingIntent.putExtra("album", Utilities.coalesce(line3, ""));
		sendBroadcast(nowPlayingIntent);
	}
	
	private void sendNotification(String title, String message) {
		final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");
		final Map<String, Object> data = new HashMap<String, Object>();
		data.put("title", title);
		data.put("body", message);
		final JSONObject jsonData = new JSONObject(data);
		final String notificationData = new JSONArray().put(jsonData).toString();
		 
		i.putExtra("messageType", "PEBBLE_ALERT");
		i.putExtra("sender", "PebbleMITM");
		i.putExtra("notificationData", notificationData);
		 
		sendBroadcast(i);
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		//return mBinder;
		return null;
	}
	/*
	public class LocalBinder extends Binder {
        LocalService getService() {
            return LocalService.this;
        }
    }
    private final IBinder mBinder = new LocalBinder();
	*/
	
	private class MenuItem {
		public String title;
		public String packageName;
		public boolean replaceAction;
		
		public MenuItem(String title, String packageName) {
			this(title, packageName, true);
		}
		public MenuItem(String title, String packageName, boolean replaceAction) {
			this.title = title;
			this.packageName = packageName;
			this.replaceAction = replaceAction;
		}
	}
}

package me.baruch.pebblemitm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class PebbleIntentReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
//			Log.d("DEBUG", "Received Intent: " + intent);
			KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

			if (event == null) {
				return;
			}
			
			Intent serviceIntent = new Intent(context, LocalService.class);
			serviceIntent.putExtra(LocalService.ORIGINAL_INTENT_KEY, intent);
			context.startService(serviceIntent);
		}
	}
}
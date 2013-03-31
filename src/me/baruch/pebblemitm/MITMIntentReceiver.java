package me.baruch.pebblemitm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MITMIntentReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
//		Log.d("DEBUG", "Received Intent: " + intent);
		
		Intent serviceIntent = new Intent(context, LocalService.class);
		serviceIntent.putExtra(LocalService.ORIGINAL_INTENT_KEY, intent);
		context.startService(serviceIntent);
	}
}
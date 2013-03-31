package me.baruch.pebblemitm;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		String selectedPackageName = PreferenceManager
				.getDefaultSharedPreferences(MainActivity.this).getString(
						"music_package_name", null);
		int selectedIndex = 0;

		ArrayList<String> musicApplicationList = new ArrayList<String>();

		Intent intent = new Intent("android.intent.action.MEDIA_BUTTON");
		final List<ResolveInfo> list = getPackageManager()
				.queryBroadcastReceivers(intent, 60);
		for (ResolveInfo resolveInfo : list) {
			if (resolveInfo.activityInfo.packageName.equals(getPackageName())) {
				list.remove(resolveInfo);
				continue;
			}
			musicApplicationList.add(resolveInfo.activityInfo.applicationInfo.loadLabel(
					getPackageManager()).toString());
			if (resolveInfo.activityInfo.packageName
					.equals(selectedPackageName)) {
				selectedIndex = musicApplicationList.size() - 1;
			}
		}

		ListView lv = (ListView) findViewById(R.id.listView);
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lv.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice, musicApplicationList));
		lv.setItemChecked(selectedIndex, true);
		lv.setSelection(selectedIndex);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				PreferenceManager
						.getDefaultSharedPreferences(MainActivity.this)
						.edit()
						.putString("music_package_name",
								list.get(position).activityInfo.packageName)
						.commit();
			}
		});
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.activity_main, menu);
	// return true;
	// }

}

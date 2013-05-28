package edu.kaist.wst660.bmaingret.android;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;

/**
 * The Main Activity.
 * 
 * This activity starts up the RegisterActivity immediately, which communicates
 * with your App Engine backend using Cloud Endpoints. It also receives push
 * notifications from backend via Google Cloud Messaging (GCM).
 * 
 * Check out RegisterActivity.java for more details.
 */
public class LocalCloud extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_localcloud);

		SharedPreferences preferences = getSharedPreferences(VAR.PREF, MODE_PRIVATE);
		String accountName = preferences.getString(VAR.PREF_ACCOUNT_NAME, null);
		Intent intent = null;
		if ((null == accountName) || (VAR.DEV)){
			intent = new Intent(this, RegisterActivity.class);
		}
		else{
			intent = new Intent(this, MainActivity.class);
		}
		startActivity(intent);
		finish();
	}
}

package edu.kaist.wst660.bmaingret.android;

import java.io.IOException;

import android.os.Bundle;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.util.Log;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient.Builder;
import com.google.android.gms.common.AccountPicker;


import com.google.api.services.androidclient.Androidclient;
import com.google.api.services.androidclient.model.ApiMessagesUserMessage;

public class RegisterActivity extends Activity {
	static final int REQUEST_ACCOUNT_PICKER = 2;
	private SharedPreferences settings = null;
	private GoogleAccountCredential credential = null;
	private static final String TAG = "RegisterActivity";
	private String accountName = null;
	private Androidclient service = null;
	private RegisterTask registerTask = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		registerTask = new RegisterTask(this);
		
        final Button button = (Button) findViewById(R.id.register_connect_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	settings = getSharedPreferences(VAR.PREF, 0);
        		credential = GoogleAccountCredential.usingAudience(v.getContext(),
        		   VAR.AUDIENCE);
        		setAccountName(settings.getString(VAR.PREF_ACCOUNT_NAME, null));

        		if ((credential.getSelectedAccountName() == null) || (VAR.DEV)) {
        			chooseAccount();
        		}

        		Builder endpointBuilder = new Androidclient.Builder(
        				AndroidHttp.newCompatibleTransport(),
        				new JacksonFactory(), credential);
        	
        		service = (Androidclient) CloudEndpointUtils.updateBuilder(endpointBuilder).build();
        		
        		((Button)v).setEnabled(false);
        		ProgressBar progress = (ProgressBar) findViewById(R.id.register_progressBar);
        		progress.setVisibility(View.VISIBLE);
        		RegisterActivity.this.registerTask.execute();             
            }
        });
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}

	private void setAccountName(String accountName) {
		 SharedPreferences.Editor editor = settings.edit();
		 editor.putString(VAR.PREF_ACCOUNT_NAME, accountName);
		 editor.commit();
		 credential.setSelectedAccountName(accountName);
		 this.accountName = accountName;
	}
	
	void chooseAccount() {
		 startActivityForResult(credential.newChooseAccountIntent(),
		   REQUEST_ACCOUNT_PICKER);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	   Intent data) {
	 super.onActivityResult(requestCode, resultCode, data);
	 switch (requestCode) {
	   case REQUEST_ACCOUNT_PICKER:
	     if (data != null && data.getExtras() != null) {
	       String accountName =
	           data.getExtras().getString(
	               AccountManager.KEY_ACCOUNT_NAME);
	       if (accountName != null) {
	         setAccountName(accountName);
	         SharedPreferences.Editor editor = settings.edit();
	         editor.putString(VAR.PREF_ACCOUNT_NAME, accountName);
	         editor.commit();
	       }
	     }
	     break;
	 }
	}
	
	private class RegisterTask extends AsyncTask<Void, Void, ApiMessagesUserMessage>{
		 Context context;

		 public RegisterTask(Context context) {
		   this.context = context;
		 }

		 protected ApiMessagesUserMessage doInBackground(Void... unused) {
			 ApiMessagesUserMessage user = null;
			 try {
			   Log.d(TAG, "Registration attempt");
			   user = service.ls().register().execute();
		   } catch (IOException e) {
		     Log.d(TAG, e.getMessage(), e);
		   }
		   return user;
		 }

		 protected void onPostExecute(ApiMessagesUserMessage user) {
			 Log.d(TAG, "Registration done: " + user.getUserId() + ":" + user.getToken());
			Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
			startActivity(intent);
		 }
	}
}

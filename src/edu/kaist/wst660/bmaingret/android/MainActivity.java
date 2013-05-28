package edu.kaist.wst660.bmaingret.android;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends Activity {
	private WifiManager.MulticastLock lock;
	private Handler handler = new Handler();
	private String type = "_http._tcp.local.";
	private JmDNS jmdns = null;
	private ServiceListener listener = null;
	private Thread serviceThread;
	private ConcurrentMap<String, ServiceInfo> services = new ConcurrentHashMap<String, ServiceInfo>();
	private Spinner servicesSpinner = null;
	private String[] servicesList = new String[0];
	private ArrayAdapter<String> spinnerAdapter = null;
	private Button fileBrowserButton = null;

	private static final String TAG = "MainActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set the service discvoery
		servicesSpinner = (Spinner) findViewById(R.id.main_servers);
		spinnerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item,
				servicesList);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		servicesSpinner.setAdapter(spinnerAdapter);

		// Set the file browser
		fileBrowserButton = (Button) findViewById(R.id.main_file_browser);
		fileBrowserButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				File mPath = new File(Environment.getExternalStorageDirectory().toString());
				FileDialog fileDialog = new FileDialog(MainActivity.this, mPath);
				fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
					public void fileSelected(File file) {
						Log.d(getClass().getName(), "selected file " + file.toString());
					}
				});
				fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
					public void directorySelected(File directory) {
						Log.d(getClass().getName(), "selected dir " + directory.toString());
					}
				});
				fileDialog.showDialog();
			}
		});
	}    


	private void setUp() {
		android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) getSystemService(android.content.Context.WIFI_SERVICE);
		lock = wifi.createMulticastLock("mylockthereturn");
		lock.setReferenceCounted(true);
		lock.acquire();
		try {
			String ip = Formatter.formatIpAddress(wifi.getConnectionInfo().getIpAddress());
			InetAddress bindingAddress = InetAddress.getByName(ip);
			jmdns = JmDNS.create(bindingAddress);
			jmdns.addServiceListener(type, listener = new ServiceListener() {

				@Override
				public void serviceResolved(ServiceEvent ev) {
					if (ev.getInfo().getInetAddresses() != null && ev.getInfo().getInetAddresses().length > 0) {
						String host = ev.getInfo().getInetAddresses()[0].getHostAddress();
						services.put(host, ev.getInfo());
						onServiceDiscovered();
						Log.d(TAG, "service discovered " + host);
					}
				}

				@Override
				public void serviceRemoved(ServiceEvent ev) {
					services.remove(ev.getInfo().getInetAddresses()[0].getHostAddress());
					onServiceDiscovered();
					Log.d(TAG, "Service removed " + ev.getInfo().getQualifiedName());
				}

				@Override
				public void serviceAdded(ServiceEvent event) {
					// Required to force serviceResolved to be called again (after the first search)
					jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}


	private void onServiceDiscovered() {
		handler.postDelayed(new Runnable() {
			public void run() {
				servicesList = services.keySet().toArray(new String[0]);
				spinnerAdapter = new ArrayAdapter<String>(MainActivity.this,
						android.R.layout.simple_spinner_item,
						servicesList);
				servicesSpinner.setAdapter(spinnerAdapter);
				Log.d(TAG, "Spinner updated" + servicesList[0]);
			}
		}, 1);

	}

	@Override
	protected void onStart() {
		super.onStart();
		serviceThread = startService();
		serviceThread.start();
		Log.d(TAG, "Service started");
	}

	@Override
	protected void onStop() {
		stopService();
		super.onStop();
	}

	protected Thread startService(){
		return new Thread(){public void run() {setUp();}};
	}

	protected void stopService(){
		if (jmdns != null) {
			if (listener != null) {
				jmdns.removeServiceListener(type, listener);
				listener = null;
			}
			jmdns.unregisterAllServices();
			try {
				jmdns.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			jmdns = null;
		}
		lock.release();
		//serviceThread.interrupt();
	}
}
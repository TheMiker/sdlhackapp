package com.sdl.hellosdlandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.smartdevicelink.protocol.enums.ControlFrameTags;
import com.smartdevicelink.proxy.rpc.GPSData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.smartdevicelink.proxy.constants.Names.GPSData;
import static com.smartdevicelink.proxy.constants.Names.url;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//If we are connected to a module we want to start our SdlService
		if(BuildConfig.TRANSPORT.equals("MULTI") || BuildConfig.TRANSPORT.equals("MULTI_HB")) {
			SdlReceiver.queryForConnectedService(this);
		}else if(BuildConfig.TRANSPORT.equals("TCP")) {
			Intent proxyIntent = new Intent(this, SdlService.class);
			startService(proxyIntent);
		}
		System.out.println("TABLE: ");
		get_table("http://18.225.6.140:3000/messages");
		System.out.println("after: ");
		System.out.println("THE LOCATION PAIR" + get_location());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	public void get_table(String myUrl) {
		System.out.println(myUrl);
		OkHttpClient client = new OkHttpClient();
		final Request request = new Request.Builder()
				.url(myUrl)
				.build();
		System.out.println("WHAT");
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				System.out.println("onFailure: ");
				e.printStackTrace();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					throw new IOException("Unexpected code " + response);
				} else {
					try {
						JSONObject jsonObject = new JSONObject(response.body().string());
						System.out.println("reponse: " + jsonObject);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

	public void get_message(Float latitude, Float longitude) {
		OkHttpClient client = new OkHttpClient();
		RequestBody formBody = new FormBody.Builder()
				.add("lat", latitude.toString())
				.add("long", longitude.toString())
				.build();
		Request request = new Request.Builder()
				.url("http://18.225.6.140:8080")
				.post(formBody)
				.build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				System.out.println("onFailure: ");
				e.printStackTrace();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					throw new IOException("Unexpected code " + response);
				} else {
					try {
						JSONObject jsonObject = new JSONObject(response.body().string());
						System.out.println("reponse: " + jsonObject);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

	public void post_message(String statement) {
		OkHttpClient client = new OkHttpClient();
		RequestBody formBody = new FormBody.Builder()
				.add("message", statement)
				.build();
		Request request = new Request.Builder()
				.url("http://18.225.6.140:8080")
				.post(formBody)
				.build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				System.out.println("onFailure: ");
				e.printStackTrace();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					throw new IOException("Unexpected code " + response);
				} else {
					try {
						JSONObject jsonObject = new JSONObject(response.body().string());
						System.out.println("reponse: " + jsonObject);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

	public ArrayList get_location(){
		ArrayList<Double> pair = new ArrayList<>();
		GPSData gps = new GPSData();
		pair.add(gps.getLongitudeDegrees());
		pair.add(gps.getLatitudeDegrees());
		return pair;
	}

}

package com.sdl.hellosdlandroid;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.Manifest;
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
import java.lang.Math;
import static com.smartdevicelink.proxy.constants.Names.GPSData;
import static com.smartdevicelink.proxy.constants.Names.url;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	public Context mContext;
	static public LocationManager mLocationManager;
	public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
	public static LocationListener myLocationListenerGPS;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		//If we are connected to a module we want to start our SdlService
		if (BuildConfig.TRANSPORT.equals("MULTI") || BuildConfig.TRANSPORT.equals("MULTI_HB")) {
			SdlReceiver.queryForConnectedService(this);
		} else if (BuildConfig.TRANSPORT.equals("TCP")) {
			Intent proxyIntent = new Intent(this, SdlService.class);
			startService(proxyIntent);
		}
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mLocationManager = locationManager;
		System.out.println("before post");
		post_message("this is 0, 0", 0,0);
		System.out.println("after post before get");
		get_message(0.000000, 0.000000);

		LocationListener locationListenerGPS = new LocationListener() {
			@Override
			public void onLocationChanged(android.location.Location location) {
				double latitude = location.getLatitude();
				double longitude = location.getLongitude();
				String msg = "New Latitude: " + latitude + "New Longitude: " + longitude;
				Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {

			}

			@Override
			public void onProviderEnabled(String provider) {

			}

			@Override
			public void onProviderDisabled(String provider) {

			}
		};
		myLocationListenerGPS = locationListenerGPS;
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					2000,
					10, locationListenerGPS);
		} else {
			checkLocationPermission();
		}
		double gpsLat = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
		System.out.println("GPSLAT: " + gpsLat);
		double gpsLon = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();
		post_message("I'm just here ... chillin'... at HackMIT", gpsLat, gpsLon);
//		System.out.println("WHOA: " + gpsLat +" "+ gpsLon);
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

	public static void get_message(double latitude, double longitude) {
		System.out.println("In Get");
		OkHttpClient client = new OkHttpClient();
		String lat = Double.toString(latitude);
		String lon = Double.toString(longitude);
		RequestBody formBody = new FormBody.Builder()
				.add("lat", lat)
				.add("long", lon)
				.build();
		Request request = new Request.Builder()
				.url("http://18.225.6.140:8080")
				.post(formBody)
				.build();
		System.out.println(request);
		System.out.println();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				System.out.println("get_message: onFailure: ");
				e.printStackTrace();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					throw new IOException("get_message: Unexpected code " + response);
				} else {
					try {
						System.out.println("on response: " + response.body());
						JSONObject jsonObject = new JSONObject(response.body().string());
						System.out.println("get_message: reponse: " + jsonObject);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

	public static void post_message(String statement, double latitude, double longitude) {
		System.out.println("In Post");
		latitude = reduceCollisions(latitude);
		longitude = reduceCollisions(longitude);
		OkHttpClient client = new OkHttpClient();
		RequestBody formBody = new FormBody.Builder()
				.add("lat", Double.toString(latitude))
				.add("long", Double.toString(longitude))
				.add("message", statement)
				.build();
		Request request = new Request.Builder()
				.addHeader("Prefer", "resolution=merge-duplicates")
				.url("http://18.225.6.140:3000/messages")
				.post(formBody)
				.build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				System.out.println("post_message: onFailure: ");
				e.printStackTrace();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					throw new IOException("post_message: Unexpected code " + response);
				} else {
					try {
						JSONObject jsonObject = new JSONObject(response.body().string());
						System.out.println("post_message: reponse: " + jsonObject);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});

	}


	public boolean checkLocationPermission() {
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
					Manifest.permission.ACCESS_FINE_LOCATION)) {

				// Show an explanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.
				new AlertDialog.Builder(this)
						.setTitle("Permissions Time")
						.setMessage("please just accept them")
						.setPositiveButton("YEAH!", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								//Prompt the user once explanation has been shown
								ActivityCompat.requestPermissions(MainActivity.this,
										new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
										MY_PERMISSIONS_REQUEST_LOCATION);
							}
						})
						.create()
						.show();


			} else {
				// No explanation needed, we can request the permission.
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						MY_PERMISSIONS_REQUEST_LOCATION);
			}
			return false;
		} else {
			return true;
		}
	}

	public static double reduceCollisions(double coord) {
		double addition = Math.random();
		if (addition > .000001) {
			addition = addition/100000;
		}
		return coord + addition;
	}
}

/*
 * Copyright 2015 Mihnea Cinteza
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mkcz.mobile.android.gpsrecord;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity
		implements GoogleApiClient.ConnectionCallbacks,
		           GoogleApiClient.OnConnectionFailedListener,
		           LocationListener
{
	private static final long UPDATE_INTERVAL_MS = 500;
	private static final long FASTEST_INTERVAL_MS = 100;
	private long m_numLocationUpdates;
	private TextView m_textView;
	private TextView m_textLat;
	private TextView m_textLong;
	private GoogleApiClient m_googleApiClient;
	private LocationRequest m_locationRequest;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
		stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener()
		{
			@Override
			public void onLayoutInflated(WatchViewStub stub)
			{
				m_textView = (TextView) stub.findViewById(R.id.text);
				m_textView.setText("Acquiring GPS");
				m_textLat = (TextView) stub.findViewById(R.id.latitude);
				m_textLong = (TextView) stub.findViewById(R.id.longitude);
			}
		});

		m_googleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API)
				.addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();

		m_locationRequest = LocationRequest.create()
		                                   .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
		                                   .setInterval(UPDATE_INTERVAL_MS);

		this.m_numLocationUpdates = 0;
		Log.i("HELLO_WORLD", "This system has gps: " + hasGPS());
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		m_googleApiClient.connect();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (m_googleApiClient.isConnected())
		{
//			LocationServices.FusedLocationApi.removeLocationUpdates(m_googleApiClient, this);
		}
		m_googleApiClient.disconnect();
	}

	private boolean hasGPS()
	{
		return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
	}

	@Override
	public void onConnected(Bundle bundle)
	{
		LocationServices.FusedLocationApi
				.requestLocationUpdates(m_googleApiClient, m_locationRequest, this)
				.setResultCallback(new ResultCallback()
				{
					@Override
					public void onResult(Result result)
					{
						Log.i("HELLO_WORLD", String.valueOf(result));
						if (result.getStatus().isSuccess())
						{
							updateDisplay(DISPLAY_FIELD.UPDATE_NO, "SUCC");
						}
						else
						{

							String text = "FAIL --  "
							              + result.getStatus().getStatusCode()
							              + " --  "
							              + result.getStatus().getStatusMessage();
							updateDisplay(DISPLAY_FIELD.UPDATE_NO, text);
						}
					}
				});
		Location m_location = LocationServices.FusedLocationApi.getLastLocation(
				m_googleApiClient);
		if (m_location != null)
		{
			updateDisplay(DISPLAY_FIELD.LATITUTDE, m_location.getLatitude());
			updateDisplay(DISPLAY_FIELD.LONGITUDE, m_location.getLongitude());
		}
	}

	@Override
	public void onConnectionSuspended(int i)
	{
		updateDisplay(DISPLAY_FIELD.UPDATE_NO, "SUSP");
	}

	private void updateDisplay(final DISPLAY_FIELD what, final Object value)
	{
		String stringValue = String.valueOf(value);
		switch (what)
		{
			case UPDATE_NO:
			{
				m_textView.setText("Location updates: " + stringValue);
				break;
			}

			case LATITUTDE:
			{
				m_textLat.setText("Lat  : " + stringValue);
				break;
			}

			case LONGITUDE:
			{
				m_textLong.setText("Long : " + stringValue);
				break;
			}
		}
	}

	@Override
	public void onLocationChanged(Location location)
	{
		Log.i("HELLO_WORLD", "Received location: " + location);
		updateDisplay(DISPLAY_FIELD.UPDATE_NO, ++m_numLocationUpdates);
		if (location != null)
		{
			updateDisplay(DISPLAY_FIELD.LATITUTDE, location.getLatitude());
			updateDisplay(DISPLAY_FIELD.LONGITUDE, location.getLongitude());
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult)
	{
		updateDisplay(DISPLAY_FIELD.UPDATE_NO, "FAIL");
	}

	private enum DISPLAY_FIELD
	{
		UPDATE_NO, LATITUTDE, LONGITUDE
	}
}

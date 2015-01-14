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
		                                        .setInterval(UPDATE_INTERVAL_MS)
		                                        .setFastestInterval(FASTEST_INTERVAL_MS);


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
			LocationServices.FusedLocationApi.removeLocationUpdates(m_googleApiClient, this);
		}
		m_googleApiClient.disconnect();
	}

	@Override
	public void onConnected(Bundle bundle)
	{
		LocationServices.FusedLocationApi
				.requestLocationUpdates(this.m_googleApiClient, m_locationRequest, this)
				.setResultCallback(new ResultCallback()
				{
					@Override
					public void onResult(Result result)
					{
						if (result.getStatus().isSuccess())
						{
							m_textView.setText("Location request: SUCCESS");
						}
						else
						{
							m_textView.setText("Location request: FAILED --  "
							                   + result.getStatus().getStatusCode()
							                   + " --  "
							                   + result.getStatus().getStatusMessage());
						}
					}
				});
		Location m_location = LocationServices.FusedLocationApi.getLastLocation(
				m_googleApiClient);
		if (m_location != null)
		{
			m_textLat.setText(String.valueOf(m_location.getLatitude()));
			m_textLong.setText(String.valueOf(m_location.getLongitude()));
		}
	}

	@Override
	public void onConnectionSuspended(int i)
	{
		m_textView.setText("Location request: SUSPENDED");
	}

	@Override
	public void onLocationChanged(Location location)
	{
		m_textView.setText("Location request: DING!");
		if (location != null)
		{
			m_textLat.setText("Lat  : " + String.valueOf(location.getLatitude()));
			m_textLong.setText("Long : " + String.valueOf(location.getLongitude()));
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult)
	{
		m_textView.setText("Location request: FAILED");
	}

	private boolean hasGPS()
	{
		return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
	}
}

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
			}
		});

		this.m_googleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API)
				.addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();

		this.m_locationRequest = LocationRequest.create()
		                                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
		                                        .setInterval(UPDATE_INTERVAL_MS)
		                                        .setFastestInterval(FASTEST_INTERVAL_MS);


	}

	@Override
	protected void onResume()
	{
		super.onResume();
		this.m_googleApiClient.connect();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (this.m_googleApiClient.isConnected())
		{
			LocationServices.FusedLocationApi.removeLocationUpdates(this.m_googleApiClient, this);
		}
		this.m_googleApiClient.disconnect();
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
							m_textView.setText("Successfully requested location updates");
						}
						else
						{
							m_textView.setText("Failed in requesting location updates, "
							                   + "status code: "
							                   + result.getStatus().getStatusCode()
							                   + ", message: "
							                   + result.getStatus().getStatusMessage());
						}
					}
				});
	}

	@Override
	public void onConnectionSuspended(int i)
	{

	}

	@Override
	public void onLocationChanged(Location location)
	{

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult)
	{

	}

	private boolean hasGPS()
	{
		return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
	}
}

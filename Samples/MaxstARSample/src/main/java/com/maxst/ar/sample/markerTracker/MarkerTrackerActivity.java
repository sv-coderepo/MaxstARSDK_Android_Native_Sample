/*
 * Copyright 2017 Maxst, Inc. All Rights Reserved.
 */

package com.maxst.ar.sample.markerTracker;

import android.app.Activity;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.maxst.ar.CameraDevice;
import com.maxst.ar.MaxstAR;
import com.maxst.ar.ResultCode;
import com.maxst.ar.TrackerManager;
import com.maxst.ar.sample.R;
import com.maxst.ar.sample.util.SampleUtil;
import com.maxst.ar.sample.util.TrackerResultListener;

public class MarkerTrackerActivity extends AppCompatActivity implements View.OnClickListener {

	private MarkerTrackerRenderer markerTargetRenderer;
	private GLSurfaceView glSurfaceView;
	private int preferCameraResolution = 0;
	private TextView recognizedMarkerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_marker_tracker);

		markerTargetRenderer = new MarkerTrackerRenderer(this);
		glSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);
		glSurfaceView.setEGLContextClientVersion(2);
		glSurfaceView.setRenderer(markerTargetRenderer);

		recognizedMarkerView = (TextView)findViewById(R.id.recognized_marker);
		markerTargetRenderer.listener = resultListener;

		MaxstAR.init(this.getApplicationContext(), getResources().getString(R.string.app_key));
		MaxstAR.setScreenOrientation(getResources().getConfiguration().orientation);

		TrackerManager.getInstance().addTrackerData("{\"marker\":\"scale\",\"all\":1}", true);
		findViewById(R.id.normal_tracking).setOnClickListener(this);
		findViewById(R.id.enhanced_tracking).setOnClickListener(this);

		TrackerManager.getInstance().loadTrackerData();
		preferCameraResolution = getSharedPreferences(SampleUtil.PREF_NAME, Activity.MODE_PRIVATE).getInt(SampleUtil.PREF_KEY_CAM_RESOLUTION, 0);
	}

	@Override
	protected void onResume() {
		super.onResume();

		glSurfaceView.onResume();
		TrackerManager.getInstance().startTracker(TrackerManager.TRACKER_TYPE_MARKER);

		ResultCode resultCode = ResultCode.Success;
		switch (preferCameraResolution) {
			case 0:
				resultCode = CameraDevice.getInstance().start(0, 640, 480);
				break;

			case 1:
				resultCode = CameraDevice.getInstance().start(0, 1280, 720);
				break;

			case 2:
				resultCode = CameraDevice.getInstance().start(0, 1920, 1080);
				break;
		}

		if (resultCode != ResultCode.Success) {
			Toast.makeText(this, R.string.camera_open_fail, Toast.LENGTH_SHORT).show();
			finish();
		}

		MaxstAR.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();

		glSurfaceView.queueEvent(new Runnable() {
			@Override
			public void run() {
			}
		});

		glSurfaceView.onPause();

		TrackerManager.getInstance().stopTracker();
		CameraDevice.getInstance().stop();
		MaxstAR.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		TrackerManager.getInstance().destroyTracker();
		MaxstAR.deinit();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.normal_tracking:
				TrackerManager.getInstance().setTrackingOption(TrackerManager.TrackingOption.NORMAL_TRACKING);
				//TrackerManager.getInstance().setTrackingOption(TrackerManager.TrackingOption.JITTER_REDUCTION_DEACTIVATION);
				TrackerManager.getInstance().setTrackingOption(TrackerManager.TrackingOption.JITTER_REDUCTION_ACTIVATION);
				break;

			case R.id.enhanced_tracking:
				TrackerManager.getInstance().setTrackingOption(TrackerManager.TrackingOption.ENHANCED_TRACKING);
				TrackerManager.getInstance().setTrackingOption(TrackerManager.TrackingOption.JITTER_REDUCTION_ACTIVATION);
				break;
		}
	}

	private TrackerResultListener resultListener = new TrackerResultListener() {
		@Override
		public void sendData(final String metaData) {
			(MarkerTrackerActivity.this).runOnUiThread(new Runnable(){
				@Override
				public void run() {
					recognizedMarkerView.setText("Recognized Marker ID : "+ metaData);
				}
			});
		}

		@Override
		public void sendFusionState(final int state) {
			(MarkerTrackerActivity.this).runOnUiThread(new Runnable(){
				@Override
				public void run() {
				}
			});

		}
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
		}

		MaxstAR.setScreenOrientation(newConfig.orientation);
	}
}

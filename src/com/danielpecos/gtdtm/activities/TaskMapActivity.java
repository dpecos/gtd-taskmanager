package com.danielpecos.gtdtm.activities;

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import com.danielpecos.gtdtm.R;
import com.danielpecos.gtdtm.model.TaskManager;
import com.danielpecos.gtdtm.model.beans.Task;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import de.android1.overlaymanager.ManagedOverlay;
import de.android1.overlaymanager.ManagedOverlayItem;
import de.android1.overlaymanager.OverlayManager;

public class TaskMapActivity extends MapActivity {
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";

	private Task task;
	private GeoPoint point;
	private ManagedOverlayItem item;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Long task_id = (Long)getIntent().getSerializableExtra("task_id");
		this.task = new Task(this.getBaseContext(), task_id);

		setContentView(R.layout.activity_layout_task_map);

		final MapView mapView = (MapView) findViewById(R.id.task_mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setHapticFeedbackEnabled(true);

		SharedPreferences p = TaskManager.getPreferences();
		String mapType = p.getString("settings_map_type", null);
		Log.i(TaskManager.TAG, "Using map mode " + mapType);

		Boolean satelliteMapType = (mapType != null && mapType.equalsIgnoreCase(this.getString(R.string.settings_map_type_default)));
		mapView.setSatellite(satelliteMapType);
		mapView.setTraffic(!satelliteMapType);

		int maxZoom = mapView.getMaxZoomLevel();
		final int initZoom = maxZoom-5;
		final MapController mapControl = mapView.getController();

		List<Overlay> mapOverlays = mapView.getOverlays();
		mapOverlays.clear();

		Drawable marker = this.getResources().getDrawable(R.drawable.marker);
		//marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());

		final OverlayManager overlayManager = new OverlayManager(this, mapView);
		final ManagedOverlay managedOverlay = overlayManager.createOverlay(marker);
		if (task.getLocation() != null) {
			Log.i(TaskManager.TAG, "Initial mark set on " + task.getLocation().getLatitudeE6() + ", " + task.getLocation().getLongitudeE6());
			this.item = managedOverlay.createItem(task.getLocation(), task.getName(), task.getDescription());
		}
		overlayManager.populate();

		managedOverlay.setOnGestureListener(new GestureDetector.SimpleOnGestureListener(){
			@Override
			public boolean onSingleTapUp(MotionEvent event) {
				point = mapView.getProjection().fromPixels(
						(int) event.getX(),
						(int) event.getY()
				);

				Toast.makeText(getBaseContext(), 
						point.getLatitudeE6() / 1E6 + "," + 
						point.getLongitudeE6() /1E6 , 
						Toast.LENGTH_SHORT).show();

				if (item != null) {
					managedOverlay.remove(item);
					Log.d(TaskManager.TAG, "Removed previous map marker");
				}
				Log.i(TaskManager.TAG, "Mark set on " + point.getLatitudeE6() + ", " + point.getLongitudeE6());
				item = managedOverlay.createItem(point, task.getName(), task.getDescription());
				overlayManager.populate();

				return false;
			}
		});

		final MyLocationOverlay myLocationOverlay = new MyLocationOverlay(this, mapView);
		myLocationOverlay.enableMyLocation();

		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mapControl.setZoom(initZoom);
				mapControl.animateTo(myLocationOverlay.getMyLocation());
			}
		});

		mapOverlays.add(myLocationOverlay);

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onBackPressed() {
		Intent resultIntent = new Intent();
		if (this.point != null) {
			resultIntent.putExtra(TaskMapActivity.LATITUDE, this.point.getLatitudeE6());
			resultIntent.putExtra(TaskMapActivity.LONGITUDE, this.point.getLongitudeE6());
			this.setResult(RESULT_OK, resultIntent);
		} else {
			this.setResult(RESULT_CANCELED, resultIntent);
		}
		this.finish();  
	}
}
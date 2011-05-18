package com.danielpecos.gtm.activities;

import java.util.List;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.LinearLayout;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class TaskMapActivity extends MapActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.task_map);

		MapView mapView = (MapView) findViewById(R.id.task_mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setHapticFeedbackEnabled(true);

//		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this); 
//		String tipoMapa = p.getString("pref_tipo_mapa", null);
//		Log.i(TaskManager.TAG, "Mapa: Modo de mapa: " + tipoMapa);
//
//		Boolean tipoMapaSatelite = tipoMapa != null && tipoMapa.equalsIgnoreCase(this.getString(R.string.pref_default_tipo_mapa));
//		mapView.setSatellite(tipoMapaSatelite);
//		mapView.setTraffic(!tipoMapaSatelite);

		
		//Drawable task_position = this.getResources().getDrawable(R.drawable.entrada);
		//itemizedOverlay = new CustomItemizedOverlay(drawable, this);


		//Log.d(TaskManager.TAG, "Mapa: Fijo el nivel de zoom predeterminado");
		int maxZoom = mapView.getMaxZoomLevel();
		int initZoom = maxZoom-5;
		MapController mapControl = mapView.getController();
		mapControl.setZoom(initZoom);

		//		Log.d(HomeActivity.TAG, "Mapa: Añado la capa con el dibujo de la puerta");
		//		GeoPoint point = new GeoPoint(latitud,longitud);
		//		OverlayItem overlayitem = new OverlayItem(point, mision, direccion);
		//
		//		itemizedOverlay.addOverlay(overlayitem);
		//		mapOverlays.add(itemizedOverlay);

		List<Overlay> mapOverlays = mapView.getOverlays();
		mapOverlays.clear();
		
		MyLocationOverlay overlay = new MyLocationOverlay(this, mapView);
		mapOverlays.add(overlay);

//		Log.d(TaskManager.TAG, "Mapa: Posiciono la puerta en el centro de la pantalla");
		mapControl.animateTo(overlay.getMyLocation()); 
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}
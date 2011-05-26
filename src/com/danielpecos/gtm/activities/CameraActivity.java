package com.danielpecos.gtm.activities;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.utils.FileUtils;

public class CameraActivity extends Activity implements SurfaceHolder.Callback, OnClickListener {
	Camera mCamera;
	boolean mPreviewRunning = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.surface_camera);
		
		mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
		mSurfaceView.setOnClickListener(this);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] imageData, Camera c) {
			if (imageData != null) {
				mCamera.stopPreview();
				
				File file = null;
				try {
					file = File.createTempFile("photo_", ".jpg", null);
				} catch (IOException ioe) {
					Log.e(TaskManager.TAG, "Exception while creating temp file " + ioe.getMessage(), ioe);
				}
				
				Log.d(TaskManager.TAG, "Using picture temp file: " + file.getAbsolutePath());
				FileUtils.StoreByteImage(imageData, 50, file);

				Intent intent = new Intent();
				intent.putExtra(TaskActivity.FILE_NAME, file.getAbsolutePath());
				
				setResult(RESULT_OK, intent);
				finish();
				
				Log.d(TaskManager.TAG, "Closing CameraActivity");
			}
		}
	};

	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// XXX stopPreview() will crash if preview is not running
		if (mPreviewRunning) {
			mCamera.stopPreview();
		}

		Camera.Parameters p = mCamera.getParameters();
		p.setPreviewSize(w, h);
		mCamera.setParameters(p);
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			Log.e(TaskManager.TAG, "Error with camera: " + e.getMessage(), e);
		}
		mCamera.startPreview();
		mPreviewRunning = true;
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mPreviewRunning = false;
		mCamera.release();
	}

	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;

	public void onClick(View arg0) {
		mCamera.takePicture(null, mPictureCallback, mPictureCallback);
	}

}

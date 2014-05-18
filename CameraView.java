package com.FaivreBideauCharriere.projet_sudoku;

import java.io.FileOutputStream;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.AttributeSet;
import android.util.Log;

public class CameraView extends JavaCameraView implements PictureCallback {
	private static final String TAG = "Sample::CameraView";
	private String mPictureFileName;

	/**
	 * Constructor.
	 * 
	 * @param context 
	 * 				A context.
	 * @param attrs
	 * 				List of attributes.
	 */    
	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Take a picture from the camera.
	 * 
	 * @param fileName 
	 * 				Path of the file where the picture will be stored.
	 */
	public void takePicture(final String fileName) {
		Log.i(TAG, "Taking picture");
		this.mPictureFileName = fileName;

		mCamera.setPreviewCallback(null);

		mCamera.takePicture(null, null, this);
	}

	/**
	 * Take a picture from the camera.
	 * 
	 * @param data 
	 * 				The image.
	 * @param camera
	 * 				Camera device.
	 */
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.i(TAG, "Saving image - fos");

		// The camera preview was automatically stopped. Start it again.
		mCamera.startPreview();
		mCamera.setPreviewCallback(this);

		try {
			FileOutputStream fos = new FileOutputStream(mPictureFileName);

			fos.write(data);
			fos.close();

		} catch (java.io.IOException e) {
			Log.e("PictureDemo", "Exception in photoCallback", e);
		}

	}
}

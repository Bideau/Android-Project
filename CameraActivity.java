package com.FaivreBideauCharriere.projet_sudoku;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Camera Activity.
 * 
 * @author Quentin FAIVRE
 */

public class CameraActivity extends Activity implements CvCameraViewListener2, OnTouchListener {
	private static final String TAG = "Camera::Activity"; //Debug trace

	private boolean showResult = false;
	private boolean draw = false;
	private CameraView mOpenCvCameraView;
	private MenuItem mItemTakeScreenshot;
	private MenuItem mItemGrid;
	private int[][] sudokuResult = new int[9][9];

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
				mOpenCvCameraView.setOnTouchListener(CameraActivity.this);
			} break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	}; 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.camera_view);

		mOpenCvCameraView = (CameraView) findViewById(R.id.camera_view);

		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

		mOpenCvCameraView.setCvCameraViewListener(this);


	}    

	@Override
	public void onPause()
	{
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
	}

	public void onCameraViewStopped() {
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		Mat rgba = inputFrame.rgba();
		if(showResult){
			rgba=drawSudokuGrid(rgba);
			rgba=showSudokuResult(rgba);
		} else if (draw){
			rgba=drawSudokuGrid(rgba);
		}
		return rgba;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		mItemTakeScreenshot = menu.add("Take a screenshot !");
		mItemGrid = menu.add("Show/Hide Grid");
		//---

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
		if (item == mItemTakeScreenshot)
		{
			String filepath = takeScreen();
			toasted(filepath + " saved");
		}
		else if (item == mItemGrid)
		{
			toasted("GridStance changed");
			draw = !draw;
		}

		return true;
	}

	public boolean onTouch(View v, MotionEvent event) {
		String imagePath = takeScreen();
		toasted("Analysis in progress ...");
		//sudokuResult=papaPart(bideauPart(imagePath)); //Tous les calculs
		sudokuResult=Resolution.Retour_Resolution(Grid.run(imagePath));
		showResult = !showResult; //show Sudoku Grid
		draw=false; //Hide grid
		return false;
	}

	/**
	 * Draws all the numbers on the Sudoku grid.
	 * 
	 * @author Quentin
	 * 
	 * @param myMat
	 * 			Where the numbers will be put.
	 * 
	 * @return A Mat with numbers.
	 * 
	 */
	public Mat showSudokuResult(Mat myMat){

		for(int j=0;j<9;j++){
			for(int i=0;i<9;i++){
				myMat=drawNumber(myMat,Integer.toString(sudokuResult[i][j]),i,j);
			}
		}

		return myMat;
	}

	/**
	 * Draw a number on a cell of the sudoku grid
	 * 
	 * @author Quentin
	 * 
	 * @param myMat
	 * 			Where the number will be put.
	 * 
	 * @param number
	 * 			Number to draw.
	 * 
	 * @param x
	 * 			Column of the grid.
	 * 
	 * @param y
	 * 			Line of the grid.
	 * 
	 * @return A Mat with the number.
	 * 
	 */
	public Mat drawNumber(Mat myMat,String number,int x, int y){
		int height = mOpenCvCameraView.getHeight(); //y
		int width = mOpenCvCameraView.getWidth(); //x
		float cellSize = height/9;
		Scalar redColor = new Scalar (255,0,0);
		float xOrigin = ((width/2)-cellSize/2)-cellSize*4;
		float yOrigin = 0;

		Core.putText(myMat,number,new Point(xOrigin+(cellSize/3)+(x*cellSize),yOrigin+(cellSize/4)+(y*cellSize)+(cellSize/2)),Core.FONT_HERSHEY_SIMPLEX,1,redColor,2,0,false);

		return myMat;
	}

	/**
	 * Draws lines to make a Sudoku grid.
	 * 
	 * @author Quentin
	 * 
	 * @param myMat
	 * 			Where the grid will be put.
	 * 
	 * @return A Mat with a grid on.
	 * 
	 */
	public Mat drawSudokuGrid(Mat myMat){
		int height = mOpenCvCameraView.getHeight(); //y
		int width = mOpenCvCameraView.getWidth(); //x
		int lineThickness = 1;
		float cellSize = height/9;
		Scalar whiteColor = new Scalar (255,255,255);

		for(int i=0;i<10;i++){ 
			//Adapt thickness
			if(i%3==0){lineThickness = 3;}else{lineThickness=1;}

			//horizontal lines
			Core.line(myMat, new Point((((width/2)-cellSize/2)-cellSize*4),i*cellSize), 
					new Point((((width/2)+cellSize/2)+cellSize*4),i*cellSize), whiteColor, lineThickness);

			//vertical lines
			Core.line(myMat, new Point((((width/2)-cellSize/2)-cellSize*4)+i*cellSize,0), 
					new Point((((width/2)-cellSize/2)-cellSize*4)+i*cellSize,(((width/2)+cellSize/2)+cellSize*4)), whiteColor, lineThickness);
		}
		return myMat;
	}

	/**
	 * Take a picture from the camera and put it in a directory
	 * (or create it if it doesn't exist).
	 *
	 * @author Quentin
	 * 
	 * @return Path of the image.
	 */
	@SuppressLint("SimpleDateFormat")
	public String takeScreen(){
		Log.i(TAG,"ScreenshotEvent");

		//Directory part
		File myDirectory = new File(Environment.getExternalStorageDirectory(),"PS");
		if (!myDirectory.exists()) {
			if (!myDirectory.mkdirs()) {
				toasted("Failed to create a screenshot in:" + myDirectory.getPath());
				return null;
			}
		}

		//File part
		SimpleDateFormat sdfFileDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String imageDate = sdfFileDate.format(new Date());
		String imageName = "IMG_" + imageDate + ".jpg";
		File image = new File(myDirectory.getPath() + File.separator + imageName);

		mOpenCvCameraView.takePicture(image.getPath());

		return image.getPath();
	}

	/**
	 * Toast a message.
	 * 
	 * @author Quentin
	 * 
	 * @param text 
	 * 				Message to display.
	 */
	public void toasted(String text){
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Call Bideau part.
	 * 
	 * @param imagePath 
	 * 				Path of an image.
	 * 
	 * @return Matrix with all the sudoku's numbers.
	 * 
	 */
	private int[][] bideauPart(String imagePath){
		//----Bideau part
		int[][] Matrice = new int[9][9];
		return Matrice;
	}
}

package com.FaivreBideauCharriere.projet_sudoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.opencv.core.Point;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.xpath.XPathExpression;




import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import analyse.NumberPos;
import analyse.SaferExec;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.JavaCV;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_highgui.*;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;



public class Grid {
	
	public Grid() {
		
		//System.out.println("java.library.path="+System.getProperty("java.library.path"));
	}
	// in the GridVisualizer class 
	// globals 
	private CanvasFrame procCanvas; // a debugging canvas for OpenCV 
	private IplImage binaryImg; // copy of grayscale webcam image 
	private Point[] pts; // four corners of sudoku grid 


	private static final int NUM_POINTS = 4; // num coords in shape 
	private static final int IM_SIZE = 600; // size of grid square 
	private static final double SMALLEST_QUAD = 50000.0; 	// ignore contours smaller than SMALLEST_QUAD pixels
	private static final int NUM_BOXES = 9; 
	private static final int LINE_WIDTH = 6; 	 // width of lines drawn onto grid 
	private static final String XPATH_EXPR = "//box"; 
	private static final int GRID_SIZE = 9; 
	private static int[][] table; 
	 // each box contains a Sudoku number (or 0 meaning no value) 



	private CvSeq findBiggestQuad(IplImage img) 
	{ 
		CvMemStorage storage = CvMemStorage.create(); 
		CvSeq bigQuad = null; 

		// generate all the contours in the image as a list 
		CvSeq contours = new CvSeq(null); 
		cvFindContours(img, storage, contours, 
				Loader.sizeof(CvContour.class), 
				CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE); 

		// find the largest quad in the list of contours 
		double maxArea = SMALLEST_QUAD; 
		while (contours != null && !contours.isNull()) { 
			if (contours.elem_size() > 0) { 
				CvSeq quad = cvApproxPoly(contours, 
						Loader.sizeof(CvContour.class), storage, 
						CV_POLY_APPROX_DP, cvContourPerimeter(contours)*0.08, 0); 
				CvSeq convexHull = cvConvexHull2(quad, storage, 
						CV_CLOCKWISE, 1); 
				if(convexHull.total() == NUM_POINTS) { 
					double area = Math.abs( 
							cvContourArea(convexHull, CV_WHOLE_SEQ, 0) ); 
					if (area > maxArea) { 
						maxArea = area; 
						bigQuad = convexHull; 
					} 
				} 
			} 
			contours = contours.h_next(); 
		} 
		return bigQuad; 
	} // end of findBiggestQuad() 


	public Point[] findOutline(IplImage img) 
	{ 
		long startTime = System.currentTimeMillis(); 
		IplImage im = enhance(img); 
		procCanvas.showImage(im); 
		binaryImg = cvCloneImage(im); // save for later 

		CvSeq quad = findBiggestQuad(im); 
		if (quad == null) { 
			// System.out.println("Sudoku grid not found"); 
			return null; 
		} 

		pts = clockSort(quad); 
		long duration = System.currentTimeMillis() - startTime; 
		System.out.println("Grid outline found in " + 
				Math.round(duration) + "ms"); 
		System.out.println(); 

		return pts; 
	} // end of findOutline() 
	private IplImage enhance(IplImage img) 
	{ 
		// convert to grayscale 
		IplImage im = IplImage.create(img.width(), img.height(), 
				IPL_DEPTH_8U, 1); 
		cvCvtColor(img, im, CV_BGR2GRAY); 

		// remove image noise 
		cvSmooth(im, im, CV_GAUSSIAN, 7, 7, 0, 0); 

		/* compensate for glare, and convert image to inverse b&w image 
	 -- black background, white letters, border, etc. */ 
		cvAdaptiveThreshold(im, im, 255, 
				CV_ADAPTIVE_THRESH_MEAN_C, 
				CV_THRESH_BINARY_INV, 
				5, 2); // block size and offset 
		return im; 
	} // end of enhance() 



	private Point[] clockSort(CvSeq quad) 
	{ 
		// copy the CvSeq points into an array 
		Point[] pts = new Point[NUM_POINTS]; 
		CvPoint pt; 
		for(int i=0; i < NUM_POINTS; i++) { 
			pt = new CvPoint( cvGetSeqElem(quad, i)); 
			pts[i] = new Point(pt.x(), pt.y()); 
		} 

		// move the point closest to the origin into pts[0] 
		int minDist = dist2(pts[0]); 
		Point temp; 
		for (int i=1; i < pts.length; i++) { 
			int d2 = dist2(pts[i]); 
			if (d2 < minDist) { 
				temp = pts[i]; // swap points 
				pts[i] = pts[0]; 
				pts[0] = temp; 
				minDist = d2; 
			} 
		} 

		// sort the array into clockwise order using pts[0] 
		//ClockwiseComparator pComp = new ClockwiseComparator(pts[0]); 
		//Arrays.sort(pts, pComp); 
		return pts; 
	} // end of clockSort() 
	private int dist2(Point pt) { 
		return (int) ((pt.x*pt.x) + (pt.y*pt.y)); 
	} 	 

	public boolean extractGrid(String outFnm) 
	// extract grid from image, clean it, and save to outFnm 
	{ 
		long startTime = System.currentTimeMillis(); 
		if (pts == null) { 
			System.out.println("No grid found"); 
			return false; 
		} 

		IplImage squareIm = warp(binaryImg, pts); 
		// extract grid image and warp it into a square 
		procCanvas.showImage(squareIm); 

		IplImage gridIm = cleanGrid(squareIm); 

		long duration = System.currentTimeMillis() - startTime; 
		System.out.println("Grid extracted in " + 
				Math.round(duration) + "ms"); 
		System.out.println(); 
		procCanvas.showImage(gridIm); 

		System.out.println("Saving grid image to " + outFnm); 
		cvSaveImage(outFnm, gridIm); 
		return true; 
	} // end of extractGrid() 




	private IplImage warp(IplImage im, Point[] pts) 
	{ 
		CvMat srcPts = CvMat.create(NUM_POINTS, 2); 
		for(int i=0; i < NUM_POINTS; i++) { 
			srcPts.put(i, 0, pts[i].x); 
			srcPts.put(i, 1, pts[i].y); 
		} 

		CvMat dstPts = CvMat.create(4, 2); 
		dstPts.put(0, 0, 0); // clockwise ordering: point 0 at (0,0) 
		dstPts.put(0, 1, 0); 

		dstPts.put(1, 0, 0); // point 1 at (0,sz-1) 
		dstPts.put(1, 1, IM_SIZE-1); 

		dstPts.put(2, 0, IM_SIZE-1); // point 2 at (sz-1, sz-1) 
		dstPts.put(2, 1, IM_SIZE-1); 

		dstPts.put(3, 0, IM_SIZE-1); // point 3 at (sz-1, 0) 
		dstPts.put(3, 1, 0); 

		CvMat totalWarp = CvMat.create(3, 3); 
		JavaCV.getPerspectiveTransform( 
				srcPts.get(), dstPts.get(), totalWarp); 

		IplImage warpImg = IplImage.create(IM_SIZE, IM_SIZE, IPL_DEPTH_8U, 1); 
		cvWarpPerspective(im, warpImg, totalWarp); 
		return warpImg; 
	} // end of warp() 

	private IplImage cleanGrid(IplImage im) 
	{ 
		cvDilate(im, im, null, 2); 
		// make white stuff larger (e.g. letters, lines) 
		procCanvas.showImage(im); 

		highlightVerticals(im); 
		// make the vertical grid lines continuous and thicker 

		// flood-fill at small intervals across the top of the image 
		for(int i=0; i < im.width(); i=i+3) // across 
			cvFloodFill(im, new CvPoint(i, 1), CvScalar.BLACK, 
					cvScalarAll(5), cvScalarAll(5), null, 4, null); 
		// max lower & upper brightness diffs; 4 is pixel connectivity 

		cvErode(im,im, null, 2); // return white stuff to previous size 
		return im; 
	} // end of cleanGrid() 

	private void highlightVerticals(IplImage im) 
	{ 
		double lineStep = ((double)(IM_SIZE-1))/NUM_BOXES; 
		int imHeight = im.height(); 
		int imWidth = im.width(); 
		int offset = LINE_WIDTH/2; 

		// draw vertical lines 
		for(int x=0; x < imWidth; x+= lineStep) 
			cvRectangle(im, cvPoint((int)x-offset, 0), 
					cvPoint((int)x+offset, imHeight), 
					CvScalar.WHITE, CV_FILLED, CV_AA, 0); 
	} // end of highlightVerticals() 

	private void applyOCR(String inFnm, String xmlFnm) 
	{ 
		System.out.println("Applying OCR to " + inFnm + 
				" using gocr..."); 
		System.out.println("Saving XML results to " + xmlFnm); 
		long startTime = System.currentTimeMillis(); 

		SaferExec se = new SaferExec(10); // timeout is 10 secs 
		se.exec("xmlGocr.bat", inFnm, xmlFnm); 

		long duration = System.currentTimeMillis() - startTime; 
		System.out.println("OCR processing took " + 
				Math.round(duration) + "ms"); 
		System.out.println(); 
	} // end of applyOCR() 

	private ArrayList<NumberPos> readNumbersXML(String xmlFnm) 
	{ 
		System.out.println("Reading in XML results from " + xmlFnm); 
		ArrayList<NumberPos> sudoNumbers = new ArrayList<NumberPos>(); 

		try { 
			DocumentBuilderFactory domFactory = 
					DocumentBuilderFactory.newInstance(); 
			domFactory.setNamespaceAware(true); 
			DocumentBuilder builder = domFactory.newDocumentBuilder(); 
			Document doc = builder.parse(xmlFnm); 

			XPathFactory factory = XPathFactory.newInstance(); 
			XPath xpath = factory.newXPath(); 
			XPathExpression expr = (XPathExpression) xpath.compile(XPATH_EXPR); 
			/* all the "box" nodes in the XML are collected, 
	 since each one represents an OCR result */ 
			Object result = expr.evaluate(doc, (short) 0, XPathConstants.NODESET); 
			NodeList nodes = (NodeList) result; 
			NumberPos np; 
			for (int i = 0; i < nodes.getLength(); i++) { 
				np = new NumberPos( nodes.item(i).getAttributes() ); 
				// each NumberPos object is built from a box node 
				if (np.isValid()) 
					sudoNumbers.add(np); 
			} 
		} 
		catch(Exception e) 
		{ System.out.println(e); } 

		return sudoNumbers; 
	} // end of readNumbersXML() 
	
	 
	 
	private void fillTable(ArrayList<NumberPos> sudoNumbers) 
	{ 
	 for (int row = 0; row < GRID_SIZE; row++) 
	 for (int col = 0; col < GRID_SIZE; col++) 
	 table[row][col] = 0; // reset table 
	 
	 for(NumberPos np : sudoNumbers) 
	 table[np.getYIndex()][np.getXIndex()] = np.getValue(); 
	 
	} // end of fillTable() 


	public static int[][] run(String path){
		
		
		IplImage image = cvLoadImage(path);
		test.findBiggestQuad(image);
		return table;
		
	}


}

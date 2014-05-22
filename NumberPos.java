package com.FaivreBideauCharriere.projet_sudoku;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;

public class NumberPos 
{ 
	// 600x600 grid of 9x9 squares 
	private static final int IM_SIZE = 600; // size of image 
	private static final int GRID_SIZE = 9; 

	private boolean isValid = true; 
	private int value; 

	private int xCenter, yCenter; 
	// image coords for center of value 
	private int xIdx, yIdx; 
	// sudoku indicies for this position 


	public NumberPos(NamedNodeMap nodeMap) 
	{ 
		value = getNodeIntValue(nodeMap, "value"); 
		if ((value < 1) || (value > 9)) 
			setValid(false); 

		int x = getNodeIntValue(nodeMap, "x"); // location of box 
		int y = getNodeIntValue(nodeMap, "y"); 
		int width = getNodeIntValue(nodeMap, "dx"); // size of box 
		int height = getNodeIntValue(nodeMap, "dy"); 

		xCenter = x + width/2; // center of value in the box 
		yCenter = y + height/2; 

		double digitSpacing = ((double) IM_SIZE)/GRID_SIZE; 
		// space between digits 
		double digitOffset = digitSpacing/2.0; 
		// space from edge to first digit 

		setxIdx((int)Math.round((xCenter-digitOffset)/digitSpacing)); 
		setyIdx((int)Math.round((yCenter-digitOffset)/digitSpacing)); 
	} // end of NumberPos() 
	private int getNodeIntValue(NamedNodeMap nodeMap, String itemName) 
	// extract value of itemName attribute, returning it as an int 
	{ 
		String valStr = null; 
		try { 
			valStr = nodeMap.getNamedItem(itemName).getNodeValue(); 
			return Integer.parseInt(valStr); 
		} 
		catch(DOMException e) 
		{ System.out.println("Parsing error"); 
		setValid(false); 
		} 
		catch (NumberFormatException ex){ 
			setValid(false); // occurs when value is '_' 
		} 
		return -1; 
	} // end of getNodeIntValue() 
	public int getYIndex() {
		return yIdx;
	}
	public void setyIdx(int yIdx) {
		this.yIdx = yIdx;
	}
	public int getXIndex() {
		return xIdx;
	}
	public void setxIdx(int xIdx) {
		this.xIdx = xIdx;
	}
	public boolean isValid() {
		return isValid;
	}
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}


} // end of NumberPos class 


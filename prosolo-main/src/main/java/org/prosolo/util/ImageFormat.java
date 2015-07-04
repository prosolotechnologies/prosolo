
package org.prosolo.util;
/**
 * @author Zoran Jeremic
 * @date Jul 6, 2012
 */

public enum ImageFormat {
	size120x120(120, 120),
	size70x70(70, 70),
	size60x60(60, 60),
	size48x48(48, 48),
	size34x34(34, 34),
	size30x30(30, 30),
	size22x22(22, 22);
	
	private int width;
	private int height;
	
	ImageFormat (int width, int height){
		this.width = width; 
		this.height = height; 
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
}
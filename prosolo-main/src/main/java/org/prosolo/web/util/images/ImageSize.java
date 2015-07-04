/**
 * 
 */
package org.prosolo.web.util.images;

/**
 * @author "Nikola Milikic"
 * 
 */
public enum ImageSize {

	size0x50(0, 50), 
	size0x100(0, 100), ;

	private int width;
	private int height;

	private ImageSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		if (height <= 0) {
			return String.valueOf(width);
		} else if (width <= 0) {
			return String.valueOf(height);
		} else {
			return String.valueOf(width)+"x"+String.valueOf(height);
		}
	}

}

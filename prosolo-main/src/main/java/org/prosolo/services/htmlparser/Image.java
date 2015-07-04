/**
 * 
 */
package org.prosolo.services.htmlparser;

/**
 * @author "Nikola Milikic"
 * 
 */
public class Image {

	private String url;
	private int width;
	private int height;

	public Image(String url, int width, int height) {
		this.url = url;
		this.width = width;
		this.height = height;
	}

	public Image(String url) {
		this.url = url;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
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

	@Override
	public String toString() {
		return "Image [url=" + url + ", width=" + width + ", height=" + height
				+ "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.getUrl().equals( ((Image) obj).getUrl());
	}
	
}

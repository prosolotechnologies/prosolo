package org.prosolo.services.annotation.impl;

/**
zoran
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.drew.metadata.Directory;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;

/**
 * This class implements a tika parser. To activate the parser we have to change
 * the tika-config.xml.
 * 
 * Compared to the default Tika Image handling we read the Jpeg Exif data and
 * return these values as metadata for Lucene
 * 
 */
@Deprecated
public class ImageParser implements Parser {

	private static final long serialVersionUID = 6799737490741895373L;

	private static Logger logger = Logger.getLogger(ImageParser.class);

	public void parse(InputStream stream, ContentHandler handler,
			Metadata metadata) throws IOException, SAXException, TikaException {

		String type = metadata.get(Metadata.CONTENT_TYPE);

		if (type != null) {
			// hey we get a jpeg lets read the exif
			if (type.equals("image/jpeg")) {
				extractJPEGMetaData(stream, metadata);
			}
			// if picture is unknown do the default tika handling
			else {

				Iterator<ImageReader> iterator = ImageIO
						.getImageReadersByMIMEType(type);
				if (iterator.hasNext()) {
					ImageReader reader = iterator.next();
					reader.setInput(ImageIO
							.createImageInputStream(new CloseShieldInputStream(
									stream)));
					metadata.set("height", Integer
							.toString(reader.getHeight(0)));
					metadata.set("width", Integer.toString(reader.getWidth(0)));
					reader.dispose();
				}
			}
		}

		XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
		xhtml.startDocument();
		xhtml.endDocument();
	}

	/**
	 * get additional metadata for jpeg files
	 * 
	 * @param inputStream
	 * @param tikaMetaData
	 */
	@SuppressWarnings("null")
	@Deprecated
	private void extractJPEGMetaData(InputStream inputStream,
			Metadata tikaMetaData) {

		// read the exif meta data
		//com.drew.metadata.Metadata jpegMetaData=null;
	//	try {
			//jpegMetaData = JpegMetadataReader.readMetadata(inputStream);

			// iterate through metadata directories
		Iterator<?> directories=null;
			//Iterator<?> directories = jpegMetaData.getDirectoryIterator();
			
			while (directories.hasNext()) {
				Directory directory = (Directory) directories.next();
				// iterate through tags and print to System.out
				Iterator<?> tags = directory.getTagIterator();
				while (tags.hasNext()) {
					Tag tag = (Tag) tags.next();
	
					try {
						tikaMetaData.set(tag.getDirectoryName() + "." + tag.getTagName(),tag.getDescription());
						logger.debug(tag.getDirectoryName() + "." + tag.getTagName() + " -> " + tag.getDescription());
					} catch (MetadataException e) {
						logger.error(e);
					}
				}
			 }
	//	} catch (JpegProcessingException e) {
	//		logger.error(e);
	 //	}
	}

	@Override
	@Deprecated
	public Set<MediaType> getSupportedTypes(ParseContext arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public void parse(InputStream arg0, ContentHandler arg1, Metadata arg2,
			ParseContext arg3) throws IOException, SAXException, TikaException {
		// TODO Auto-generated method stub
		
	}
}
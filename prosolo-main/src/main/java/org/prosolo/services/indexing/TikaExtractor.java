package org.prosolo.services.indexing;

import java.io.InputStream;

import org.prosolo.services.indexing.impl.ExtractedTikaDocument;

/**
 *
 * @author Zoran Jeremic, May 9, 2014
 *
 */
public interface  TikaExtractor {

	ExtractedTikaDocument parseInputStream(InputStream inputStream);

}

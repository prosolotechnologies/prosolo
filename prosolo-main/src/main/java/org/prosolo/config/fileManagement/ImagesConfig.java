package org.prosolo.config.fileManagement;

import org.prosolo.util.StringUtils;
import org.simpleframework.xml.Element;

public class ImagesConfig {

	@Element(name = "file-type-icons-path")
	public String fileTypeIconsPath;
	
	@Override
	public String toString() {
		return StringUtils.toStringByReflection(this);
	}

}
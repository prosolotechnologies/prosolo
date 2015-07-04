package org.prosolo.web.activitywall.data;


public enum FileType {
	_BLANK,
	AVI,
	DOC,
	DOCX,
	FLV,
	GIF,
	JPG,
	JPEG,
	MP3,
	MP4,
	MPG,
	PDF,
	PNG,
	PPT,
	PPTX,
	RAR,
	RTF,
	TGZ,
	TXT,
	WAV,
	XLS,
	XLSX,
	ZIP,
	;
	
	public static FileType getFileType(String url){
		if (url != null) {
			int indexOfLastDot = url.lastIndexOf(".");
			
			if (indexOfLastDot >= 0) {
				String extension = url.substring(indexOfLastDot);
				
				if (extension != null) {
					// if there is no exception, file type is suported
					try {
						return valueOf(extension.toUpperCase().replace(".", ""));
					} catch (Exception e) {	}
				}
			}
		}
		return _BLANK;
	}
}

package org.prosolo.services.upload;

import java.io.IOException;
import java.io.InputStream;

/**
zoran
 */

public interface AvatarProcessor {

	String storeUserAvatar(long userId, InputStream imageInputStream, String avatarFilename, boolean createResizedCopies) throws IOException;

	String storeTempAvatar(long userId, InputStream imageInputStream,	String avatarFilename, int scaledWidth, int scaledHeight)
			throws IOException;

	String cropImage(long userId, String imagePath, int left, int top, int width,	int height) throws IOException;

}

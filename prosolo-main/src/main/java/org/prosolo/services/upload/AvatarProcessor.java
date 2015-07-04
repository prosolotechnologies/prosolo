package org.prosolo.services.upload;

import java.io.IOException;
import java.io.InputStream;

import org.prosolo.domainmodel.user.User;

/**
zoran
 */

public interface AvatarProcessor {

	String storeUserAvatar(User user, InputStream imageInputStream, String avatarFilename, boolean createResizedCopies) throws IOException;

	String storeTempAvatar(User user, InputStream imageInputStream,	String avatarFilename, int scaledWidth, int scaledHeight)
			throws IOException;

	String cropImage(User user, String imagePath, int left, int top, int width,	int height) throws IOException;

}

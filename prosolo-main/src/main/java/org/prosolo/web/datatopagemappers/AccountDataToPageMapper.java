package org.prosolo.web.datatopagemappers;

import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.web.settings.data.AccountData;
import org.prosolo.web.util.AvatarUtils;

public class AccountDataToPageMapper implements IDataToPageMapper<AccountData, UserData> {

	@Override
	public AccountData mapDataToPageObject(UserData user) {
		AccountData accountData = new AccountData();
		accountData.setId(user.getId());
		accountData.setEmail(user.getEmail());
		accountData.setAvatarPath(AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size120x120));
		accountData.setFirstName(user.getName());
		accountData.setLastName(user.getLastName());

		// position
		accountData.setPosition(user.getPosition());

		// location
		accountData.setLocationName(user.getLocationName());

		String lat = null;
		String lon = null;
		if (user.getLatitude() != null) {
			lat = String.valueOf(user.getLatitude());
		}
		if (user.getLongitude() != null) {
			lon = String.valueOf(user.getLongitude());
		}
		accountData.setLatitude(lat);
		accountData.setLongitude(lon);
		return accountData;
	}

}

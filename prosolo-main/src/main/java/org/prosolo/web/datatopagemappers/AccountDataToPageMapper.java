package org.prosolo.web.datatopagemappers;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.web.settings.data.AccountData;

public class AccountDataToPageMapper implements IDataToPageMapper<AccountData, User> {

	@Override
	public AccountData mapDataToPageObject(User user) {
		AccountData accountData = new AccountData();
		accountData.setId(user.getId());
		accountData.setAvatarPath(user.getAvatarUrl());
		accountData.setFirstName(user.getName());
		accountData.setLastName(user.getLastname());

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

package org.prosolo.web.datatopagemappers;

import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.settings.data.AccountData;

public class AccountDataToPageMapper implements IDataToPageMapper<AccountData, LoggedUserBean> {

	@Override
	public AccountData mapDataToPageObject(LoggedUserBean loggedUser) {
		AccountData accountData = new AccountData();
		accountData.setId(loggedUser.getUser().getId());
		accountData.setAvatarPath(loggedUser.getAvatar());
		accountData.setFirstName(loggedUser.getUser().getName());
		accountData.setLastName(loggedUser.getUser().getLastname());

		// position
		accountData.setPosition(loggedUser.getUser().getPosition());

		// location
		accountData.setLocationName(loggedUser.getUser().getLocationName());

		String lat = null;
		String lon = null;
		if (loggedUser.getUser().getLatitude() != null) {
			lat = String.valueOf(loggedUser.getUser().getLatitude());
		}
		if (loggedUser.getUser().getLongitude() != null) {
			lon = String.valueOf(loggedUser.getUser().getLongitude());
		}
		accountData.setLatitude(lat);
		accountData.setLongitude(lon);
		return accountData;
	}

}

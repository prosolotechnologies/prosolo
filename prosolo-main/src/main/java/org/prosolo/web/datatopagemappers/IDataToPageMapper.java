package org.prosolo.web.datatopagemappers;

import org.prosolo.web.data.IData;

public interface IDataToPageMapper<T extends IData, D> {
	/**
	 * Maps entity data to page objects
	 * @param data
	 * @return
	 */
	T mapDataToPageObject(D data);
}

package org.prosolo.web.datatopagemappers;

import org.prosolo.web.data.IData;

public interface IDataToPageMapper<T extends IData> {
	
	T mapDataToPageObject(T data);
}

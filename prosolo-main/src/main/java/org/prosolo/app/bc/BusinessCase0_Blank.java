package org.prosolo.app.bc;

import org.springframework.stereotype.Service;

@Service
public class BusinessCase0_Blank implements BusinessCase {

	@Override
	public void initRepository() {
		System.out.println("BusinessCaseTest - initRepository() with BC 0");
		
 	}

}

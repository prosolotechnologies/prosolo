package org.prosolo.web.administration;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Bojan
 *
 * May 24, 2017
 */

@ManagedBean(name = "adminAdmins")
@Component("adminAdmins")
@Scope("view")
public class AdminsBean implements Serializable,Paginable{

	private static final long serialVersionUID = -941411747259924715L;
	
	protected static Logger logger = Logger.getLogger(AdminsBean.class);
	
	
	public void init(){
		logger.debug("Hello from adminAdmins bean logger");
		System.out.println("Hello from adminAdmins bean");
	}
	

	@Override
	public void changePage(int page) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PaginationData getPaginationData() {
		// TODO Auto-generated method stub
		return null;
	}

	
}

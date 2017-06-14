package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;

import javax.faces.bean.ManagedBean;
import java.io.Serializable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
/**
 * Created by Bojan on 6/6/2017.
 */

@ManagedBean(name = "adminOrganizations")
@Component("adminOrganizations")
@Scope("view")
public class OrganizationsBean implements Serializable,Paginable {

    protected static Logger logger = Logger.getLogger(OrganizationsBean.class);


    public void init(){
        logger.debug("Hello from adminOrganizations bean logger");
        System.out.println("Hello from adminOrganizations bean");
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

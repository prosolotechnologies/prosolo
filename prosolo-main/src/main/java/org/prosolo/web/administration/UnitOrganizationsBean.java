package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import java.io.Serializable;

/**
 * @author Bojan
 * @date 2017-07-04
 * @since 0.7
 */
@ManagedBean(name = "unitOrganizationsBean")
@Component("unitOrganizationsBean")
@Scope("view")
public class UnitOrganizationsBean implements Serializable {

    protected static Logger logger = Logger.getLogger(UnitOrganizationsBean.class);

    public void init(){
        logger.info("Hello from unit organizations bean");
    }
}

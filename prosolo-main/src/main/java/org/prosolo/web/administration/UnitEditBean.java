package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.UnitData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.PageAccessRightsResolver;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author Bojan Trifkovic
 * @date 2017-07-14
 * @since 1.0.0
 */

@ManagedBean(name = "unitEditBean")
@Component("unitEditBean")
@Scope("view")
public class UnitEditBean implements Serializable {

    protected static Logger logger = Logger.getLogger(UnitEditBean.class);

    @Inject
    private LoggedUserBean loggedUser;
    @Inject
    private UrlIdEncoder idEncoder;
    @Inject
    private UnitManager unitManager;
    @Inject
    private OrganizationManager organizationManager;
    @Inject
    private PageAccessRightsResolver pageAccessRightsResolver;

    private UnitData unit;
    private String id;
    private long decodedId;
    private String organizationId;
    private String organizationTitle;

    public void init(){
        try{
            this.decodedId = idEncoder.decodeId(id);
            long decodedOrgId = idEncoder.decodeId(organizationId);

            if (pageAccessRightsResolver.getAccessRightsForOrganizationPage(decodedOrgId).isCanAccess()) {
                this.unit = unitManager.getUnitData(decodedId);
                this.organizationTitle = organizationManager.getOrganizationTitle(decodedOrgId);
            } else {
                PageUtil.accessDenied();
            }
        }catch (Exception e){
            logger.error(e);
            e.printStackTrace();
        }
    }

    public void updateUnit(){
        try {
            //TODO add organization id to user context
            unitManager.updateUnit(this.unit.getId(),this.unit.getTitle(), loggedUser.getUserContext(0));

            logger.debug("Unit (" + this.unit.getId() + ") updated by the user " + loggedUser.getUserId());
            PageUtil.fireSuccessfulInfoMessage("The unit has been updated");
        }catch (ConstraintViolationException | DataIntegrityViolationException e){
            logger.error(e);
            e.printStackTrace();

            FacesContext context = FacesContext.getCurrentInstance();
            UIInput input = (UIInput) context.getViewRoot().findComponent(
                    "formMainEditUnit:inputTextUnitTitle");
            input.setValid(false);
            context.addMessage("formMainEditUnit:inputTextUnitTitle",
                    new FacesMessage("Unit with this name already exists") );
        }catch (Exception e){
            logger.error(e);
            PageUtil.fireErrorMessage("Error updating the unit");
        }
    }

    public UnitData getUnit() {
        return unit;
    }

    public void setUnit(UnitData unit) {
        this.unit = unit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationTitle() { return organizationTitle; }
}

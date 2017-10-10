package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.OrganizationData;
import org.prosolo.services.nodes.data.UnitData;
import org.prosolo.services.nodes.factory.OrganizationDataFactory;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
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
import java.util.Collections;
import java.util.List;

/**
 * @author Bojan
 * @date 2017-07-04
 * @since 1.0.0
 */

@ManagedBean(name = "unitsBean")
@Component("unitsBean")
@Scope("view")
public class UnitsBean implements Serializable {

    protected static Logger logger = Logger.getLogger(UnitsBean.class);

    @Inject
    private LoggedUserBean loggedUser;
    @Inject
    private UrlIdEncoder idEncoder;
    @Inject
    private UnitManager unitManager;
    @Inject
    private OrganizationManager organizationManager;
    @Inject
    private OrganizationDataFactory organizationDataFactory;

    private UnitData unit;
    private UnitData unitToDelete;
    private String organizationId;
    private OrganizationData organizationData;
    private List<UnitData> units;
    private String id;

    public void init() {
        try {
            long orgId = idEncoder.decodeId(organizationId);
            if (loggedUser.getOrganizationId() == orgId || loggedUser.hasCapability("admin.advanced")) {
                this.unit = new UnitData();
                this.organizationData = organizationManager.getOrganizationDataWithoutAdmins(idEncoder.decodeId(organizationId));
                loadUnits();
            } else {
                PageUtil.accessDenied();
            }
        } catch (Exception e) {
            logger.error(e);
            PageUtil.fireErrorMessage("Error while loading page");
        }
    }

    private void loadUnits() {
        try {
            this.units = unitManager.getUnitsWithSubUnits(this.organizationData.getId());
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void setParentUnit(long unitId) {
        this.unit = new UnitData();
        UnitData parentUnit = unitManager.getUnitData(unitId);
        this.unit.setParentUnitId(parentUnit.getId());
    }

    public void createNewUnit() {
        try {
            UnitData unit = unitManager.createNewUnit(this.unit.getTitle(), this.organizationData.getId(),
                    this.unit.getParentUnitId(), loggedUser.getUserContext(idEncoder.decodeId(organizationId)));

            logger.debug("New Organization Unit (" + unit.getTitle() + ")");
            PageUtil.fireSuccessfulInfoMessage("New unit has been created");

            this.unit = new UnitData();
            loadUnits();
            Collections.sort(this.units);
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            logger.error(e);
            e.printStackTrace();

            FacesContext context = FacesContext.getCurrentInstance();
            UIInput input = (UIInput) context.getViewRoot().findComponent(
                    "newUnitModal:formNewUnitModal:inputTextOrganizationUnitName");
            input.setValid(false);
            context.addMessage("newUnitModal:formNewUnitModal:inputTextOrganizationUnitName",
                    new FacesMessage("Unit with this name already exists"));
            context.validationFailed();
        } catch (Exception e) {
            logger.error(e);
            PageUtil.fireErrorMessage("Error while trying to save unit data");
        }
    }

    public void setUnitToDelete(UnitData unitToDelete) {
        this.unitToDelete = unitToDelete;
    }

    public void delete() {
        if (unitToDelete != null) {
            try {
                unitManager.deleteUnit(this.unitToDelete.getId());

                PageUtil.fireSuccessfulInfoMessageAcrossPages("The unit " + unitToDelete.getTitle() + " has been deleted");
                this.unitToDelete = new UnitData();
                loadUnits();
            } catch (IllegalStateException ise) {
                logger.error(ise);
                PageUtil.fireErrorMessage(ise.getMessage());
            } catch (Exception ex) {
                logger.error(ex);
                PageUtil.fireErrorMessage("Error while trying to delete unit");
            }
        }
    }

    public UnitData getUnit() {
        return unit;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public List<UnitData> getUnits() {
        return units;
    }

    public OrganizationData getOrganizationData() {
        return organizationData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

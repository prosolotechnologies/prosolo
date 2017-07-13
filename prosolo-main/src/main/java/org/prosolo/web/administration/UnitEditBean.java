package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.common.event.context.data.LearningContextData;
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
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author Bojan
 * @date 2017-07-04
 * @since 0.7
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
    private OrganizationDataFactory organizationDataFactory;

    private UnitData unit;
    private String organizationId;
    private OrganizationData organizationData;
    private List<UnitData> units;
    private String id;
    private long decodedId;

    public void init(){
        try{
            decodedId = idEncoder.decodeId(id);
            if(decodedId > 0){
                this.unit = unitManager.getUnitDataById(decodedId);
            }else {
                this.unit = new UnitData();
                this.organizationData = organizationManager.getOrganizationDataWithoutAdmins(idEncoder.decodeId(organizationId));
            }
            loadUnits();
        }catch (Exception e){
            logger.error(e);
            PageUtil.fireErrorMessage("Error while loading page");
        }
    }

    private void loadUnits(){
        try{
            this.units = unitManager.getUnitsWithSubUnits(this.organizationData.getId());
        }catch (Exception e){
            logger.error(e);
        }
    }

    public void setParentUnit(long unitId){
        UnitData parentUnit = unitManager.getUnitDataById(unitId);
        this.unit.setParentUnit(parentUnit);
    }

    public void createNewUnit(){
        try{
            LearningContextData lcd = PageUtil.extractLearningContextData();

            UnitData unit = unitManager.createNewUnit(this.unit.getTitle(),this.organizationData.getId(),
                    this.unit.getParentUnit(), loggedUser.getUserId(),lcd);

            logger.debug("New Organization Unit (" + unit.getTitle() + ")");
            PageUtil.fireSuccessfulInfoMessage("New unit is created");

            if(unit.getParentUnit() == null) {
                this.units.add(unit);
            }
            this.unit.setParentUnit(null);
            loadUnits();
            Collections.sort(this.units);
        } catch (ConstraintViolationException | DataIntegrityViolationException e){
            logger.error(e);
            e.printStackTrace();

            FacesContext context = FacesContext.getCurrentInstance();
            UIInput input = (UIInput) context.getViewRoot().findComponent(
                    "newUnitModal:formNewUnitModal:inputTextOrganizationUnitName");
            input.setValid(false);
            context.addMessage("newUnitModal:formNewUnitModal:inputTextOrganizationUnitName",
                    new FacesMessage("Unit with this name already exists") );
            context.validationFailed();
        } catch (Exception e){
            logger.error(e);
            PageUtil.fireErrorMessage("Error while trying to save unit data");
        }
     }


    public void updateUnit(){
        try{
            LearningContextData lcd = PageUtil.extractLearningContextData();

            unitManager.updateUnit(this.unit.getId(),this.unit.getTitle(),loggedUser.getUserId(),lcd);

            logger.debug("Unit (" + this.unit.getId() + ") updated by the user " + loggedUser.getUserId());
            PageUtil.fireSuccessfulInfoMessage("Unit is updated");

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
            PageUtil.fireErrorMessage("Error while trying to update unit data");
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

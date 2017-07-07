package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.jdom.IllegalDataException;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Unit;
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
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author Bojan
 * @date 2017-07-04
 * @since 0.7
 */

@ManagedBean(name = "unitOrganizationEditBean")
@Component("unitOrganizationEditBean")
@Scope("view")
public class UnitOrganizationEditBean implements Serializable {

    protected static Logger logger = Logger.getLogger(UnitOrganizationEditBean.class);

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
    private OrganizationData organization;
    private String id;
    private long decodedId;

    public void init(){
        logger.debug("initializing");
        try{
            decodedId = idEncoder.decodeId(id);
            unit = new UnitData();


        }catch (Exception e){
            logger.error(e);
            PageUtil.fireErrorMessage("Error while loading page");
        }
    }

    public void saveUnit(){
        if(this.unit.getId() == 0){
            createNewUnit();
        }else{
            updateUnit();
        }
    }

    private void createNewUnit(){
        FacesContext context = FacesContext.getCurrentInstance();
        UIInput input = (UIInput) context.getViewRoot().findComponent(
                "newUnitModal:formNewUnitModal:inputTextOrganizationUnitName");
        try{
            LearningContextData lcd = PageUtil.extractLearningContextData();

            Unit unit = unitManager.createNewUnit(this.unit.getTitle(),this.unit.getOrganization().getId(),
                    loggedUser.getUserId(),lcd);

            logger.debug("New Organization Unit (" + unit.getTitle() + ")");

            PageUtil.fireSuccessfulInfoMessage("New unit is created");
            input.setValue("");
        } catch (ConstraintViolationException | DataIntegrityViolationException e){
            logger.error(e);
            e.printStackTrace();
            input.setValid(false);
            context.addMessage("newUnitModal:formNewUnitModal:inputTextOrganizationUnitName",
                    new FacesMessage("Unit with this name already exists") );
            FacesContext.getCurrentInstance().validationFailed();
        } catch (Exception e){
            logger.error(e);
            PageUtil.fireErrorMessage("Error while trying to save unit data");
        }
    }

    public void cancel(){
        FacesContext context = FacesContext.getCurrentInstance();
        UIInput input = (UIInput) context.getViewRoot().findComponent(
                "newUnitModal:formNewUnitModal:inputTextOrganizationUnitName");
        input.setValue("");
    }

    public void setUnitOrganization(long organizationId){
        this.unit.setOrganization(organizationManager.getOrganizationDataById(organizationId));
    }

    private void updateUnit(){

    }

    public UrlIdEncoder getIdEncoder() {
        return idEncoder;
    }

    public void setIdEncoder(UrlIdEncoder idEncoder) {
        this.idEncoder = idEncoder;
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

    public long getDecodedId() {
        return decodedId;
    }

    public void setDecodedId(long decodedId) {
        this.decodedId = decodedId;
    }
}

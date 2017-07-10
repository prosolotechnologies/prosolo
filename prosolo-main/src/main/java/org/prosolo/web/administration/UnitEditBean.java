package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.RoleManager;
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
    @Inject
    private RoleManager roleManager;

    private UnitData unit;
    private OrganizationData organization;
    private String id;
    private long decodedId;private String[] rolesArray;
    private List<Role> adminRoles;


    public void init(){
        logger.debug("initializing");
        try{
            rolesArray = new String[]{"Admin","Super Admin"};
            adminRoles = roleManager.getRolesByNames(rolesArray);
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

    public void setUnitOrganization(long organizationId){
        this.unit.setOrganization(organizationManager.getOrganizationDataById(organizationId,adminRoles));
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

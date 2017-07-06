/*
package org.prosolo.web.administration;

import org.apache.log4j.Logger;
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
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;

*
 * @author Bojan
 * @date 2017-07-04
 * @since 0.7


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
        try{
            LearningContextData lcd = PageUtil.extractLearningContextData();

            Unit unit = unitManager.createNewUnit(this.unit.getTitle(),this.unit.getOrganization().getId(),
                    loggedUser.getUserId(),lcd);

            this.unit.setId(unit.getId());

            logger.debug("New Organization Unit (" + unit.getTitle() + ")");

            PageUtil.fireSuccessfulInfoMessageAcrossPages("Unit successfully saved");
            PageUtil.redirect("/admin/organizations");

        } catch (IllegalDataException e){
            logger.error(e);
            e.printStackTrace();
            FacesContext.getCurrentInstance().validationFailed();
            PageUtil.fireErrorMessage(e.getMessage());
        } catch (Exception e){
            logger.error(e);
            PageUtil.fireErrorMessage("Error while trying to save unit data");
        }
    }

    public void setUnitOrganization(long organizationId){
        Organization organization = organizationManager.getOrganizationById(organizationId);
        this.organization = organizationDataFactory.getOrganizationData(organization,organization.getUsers());
        this.unit.setOrganization(this.organization);
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
*/

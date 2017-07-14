package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.data.UnitData;
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

/**
 * @author ivana
 * @date 2017-07-14
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

    private UnitData unit;
    private String id;
    private long decodedId;

    public void init(){
        try{
            decodedId = idEncoder.decodeId(id);
            this.unit = unitManager.getUnitData(decodedId);
        }catch (Exception e){
            logger.error(e);
            e.printStackTrace();
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

    public void setUnit(UnitData unit) {
        this.unit = unit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

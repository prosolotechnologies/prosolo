package org.prosolo.web.rubrics;

import org.apache.log4j.Logger;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.nodes.RubricManager;
import org.prosolo.services.nodes.data.RubricData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-24
 * @since 1.0.0
 */

@ManagedBean(name = "rubricsBean")
@Component("rubricsBean")
@Scope("view")
public class RubricsBean implements Serializable,Paginable {

    protected static Logger logger = Logger.getLogger(RubricsBean.class);

    @Inject
    private UrlIdEncoder idEncoder;
    @Inject
    private RubricManager rubricManager;
    @Inject
    private LoggedUserBean loggedUser;

    private List<RubricData> rubrics;
    private PaginationData paginationData = new PaginationData();
    private RubricData rubricToDelete;

    public void init(){
        loadRubrics();
    }

    public void loadRubrics(){
        this.rubrics = new ArrayList<>();
        try{
            PaginatedResult<RubricData> res = rubricManager.getRubrics(paginationData.getPage() - 1,
                    paginationData.getLimit(),loggedUser.getOrganizationId());
            rubrics = res.getFoundNodes();
            this.paginationData.update((int) res.getHitsNumber());
        }catch (Exception e){
            logger.error(e);
            e.printStackTrace();
        }
    }

    @Override
    public void changePage(int page) {
        if(this.paginationData.getPage() != page){
            this.paginationData.setPage(page);
            loadRubrics();
        }
    }

    public void setRubricForDelete(RubricData rubric){
        this.rubricToDelete = rubric;
    }

    public void delete(){
        if(rubricToDelete != null){
            try{
                rubricManager.deleteRubric(this.rubricToDelete.getId());

                PageUtil.fireSuccessfulInfoMessageAcrossPages("Rubric " + rubricToDelete.getName() + " is deleted.");
                PageUtil.redirect("/manage/rubrics");
            }catch (IllegalStateException ise) {
                logger.error(ise);
                PageUtil.fireErrorMessage(ise.getMessage());
            } catch (Exception ex) {
                logger.error(ex);
                PageUtil.fireErrorMessage("Error while trying to delete rubric");
            }
        }
    }

    @Override
    public PaginationData getPaginationData() {
        return this.paginationData;
    }

    public List<RubricData> getRubrics() {
        return rubrics;
    }

    public void setRubrics(List<RubricData> rubrics) {
        this.rubrics = rubrics;
    }

    public UrlIdEncoder getIdEncoder() {
        return idEncoder;
    }

    public void setIdEncoder(UrlIdEncoder idEncoder) {
        this.idEncoder = idEncoder;
    }

}

package org.prosolo.web.rubrics;

import org.apache.log4j.Logger;
import org.prosolo.services.nodes.RubricManager;
import org.prosolo.services.nodes.data.RubricData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

/**
 * @author Bojan Trifkovic
 * @date 2017-08-24
 * @since 1.0.0
 */

@ManagedBean(name = "rubricsBean")
@Component("rubricsBean")
@Scope("view")
public class RubricsBean implements Serializable{

    protected static Logger logger = Logger.getLogger(RubricsBean.class);

    @Inject
    private UrlIdEncoder idEncoder;
    @Inject
    private RubricManager rubricManager;


    private List<RubricData> rubrics;

    public void init(){
        try{
            //this.rubrics = rubricManager.getRubrics();
        }catch (Exception e){
            logger.error(e);
            e.printStackTrace();
        }
    }

}

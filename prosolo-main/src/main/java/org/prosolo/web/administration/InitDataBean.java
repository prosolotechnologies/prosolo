package org.prosolo.web.administration;

import org.apache.log4j.Logger;
import org.prosolo.app.bc.InitData;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.services.datainit.DataInitManager;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Bean that serves the page /admin/data-init that allows reinitializing the database state to a specific dataset
 * used for manual testing.
 *
 * @author Stefan Vuckovic
 * @date 2019-05-28
 * @since 1.3.2
 */

@ManagedBean(name = "initDataBean")
@Component("initDataBean")
@Scope("view")
public class InitDataBean implements Serializable {

    protected static Logger logger = Logger.getLogger(InitDataBean.class);

    @Inject private DataInitManager dataInitManager;

    private List<InitData> bcs;
    private InitData selectedBC;

    public void init() {
        InitData[] bcsArr = InitData.values();
        Arrays.sort(bcsArr, Comparator.comparing(InitData::isTest).thenComparing(InitData::name));
        bcs = List.of(bcsArr);
    }

    public List<InitData> getBcs() {
        return bcs;
    }

    public void setBC(InitData bc) {
        selectedBC = bc;
    }

    public void applyBc() {
        try {
            if (selectedBC != null) {
                dataInitManager.reinitializeDBData(selectedBC);
                PageUtil.fireSuccessfulInfoMessage("Data has been successfully reinitialized");
            } else {
                PageUtil.fireErrorMessage("Business case must be selected");
            }
        } catch(DbConnectionException e) {
            PageUtil.fireErrorMessage("Error reinitializing the data");
        }
    }

    public void test() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

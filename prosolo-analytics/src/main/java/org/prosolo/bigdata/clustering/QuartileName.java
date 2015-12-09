package org.prosolo.bigdata.clustering;/**
 * Created by Zoran on 05/12/15.
 */

import be.ac.ulg.montefiore.run.jahmm.ObservationDiscrete;

/**
 * Zoran 05/12/15
 */
public enum QuartileName {
    L, M, H;

    /**
     * @return
     */
    public ObservationDiscrete<QuartileName> observation(){
        return new ObservationDiscrete<>(this);
    }
}

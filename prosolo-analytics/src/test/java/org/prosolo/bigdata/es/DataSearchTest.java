package org.prosolo.bigdata.es;/**
 * Created by zoran on 31/07/16.
 */

import org.junit.Test;
import org.prosolo.bigdata.es.impl.DataSearchImpl;

import java.util.List;

/**
 * zoran 31/07/16
 */
public class DataSearchTest {
    @Test
    public void findCredentialMembersTest(){
        System.out.println("STARTING TEST");
        DataSearch ds=new DataSearchImpl();
        List<Long> members=ds.findCredentialMembers(1,0,100);
        for(Long id:members){
            System.out.println("MEMBER:"+id);
        }
    }
}

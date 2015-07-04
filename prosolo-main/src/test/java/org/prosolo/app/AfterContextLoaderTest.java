package org.prosolo.app;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:core/*/context.xml"})
public class AfterContextLoaderTest {

  @Test
  public void initElasticSearchIndexes() {
   
  }
}

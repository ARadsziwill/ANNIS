/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.service.internal;

import annis.AnnisBaseRunner;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import annis.service.AnnisService;
import annis.test.TestHelper;

public class TestAnnisServiceImpl
{

  @Autowired
  private AnnisService springManagedAnnisServiceImpl;

  @Test
  public void springManagedInstanceHasAllDependencies()
  {

    AnnisServiceImpl annisServiceImpl = (AnnisServiceImpl) AnnisBaseRunner.
      getBean("annisService", true,
      "file:src/main/distribution/conf/spring/Service.xml");


    assertThat(annisServiceImpl.getAnnisDao(), is(not(nullValue())));
  }
}

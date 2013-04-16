/*
 * Copyright 2013 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.visualizers.component.htmlvis;

import java.io.IOException;
import java.io.InputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class ParserTest
{ 
  
  private static final Logger log = LoggerFactory.getLogger(ParserTest.class);
  
  public ParserTest()
  {
  }
  
  @BeforeClass
  public static void setUpClass()
  {
  }
  
  @AfterClass
  public static void tearDownClass()
  {
  }
  
  @Before
  public void setUp()
  {
  }
  
  @After
  public void tearDown()
  {
  }

  @Test
  public void testBasicParsing()
  {
    try
    {
      InputStream inStream = ParserTest.class.getResourceAsStream("basicexample.config");
      Parser parser = new Parser(inStream);
      VisualizationDefinition[] definitions =  parser.getDefinitions();
      
      assertEquals("There must be 4 rules from the parsing", 4, definitions.length);
      
      assertEquals(definitions[0].getMatchingElement(), "p");
      assertEquals(definitions[0].getMatchingValue(), null);
      assertEquals(definitions[0].getOutputElement(), "p");
      assertEquals(definitions[0].getStyle(), "");
      
      assertEquals(definitions[1].getMatchingElement(), "pb");
      assertEquals(definitions[1].getMatchingValue(), null);
      assertEquals(definitions[1].getOutputElement(), "p");
      assertEquals(definitions[1].getStyle(), "");
      
      assertEquals(definitions[2].getMatchingElement(), "lang");
      assertEquals(definitions[2].getMatchingValue(), "foreign");
      assertEquals(definitions[2].getOutputElement(), "i");
      assertEquals(definitions[2].getStyle(), "");
      
      assertEquals(definitions[3].getMatchingElement(), "lang");
      assertEquals(definitions[3].getMatchingValue(), "foreign");
      assertEquals(definitions[3].getOutputElement(), "span");
      assertEquals(definitions[3].getStyle(), "font-style: italic; background-color: red");
      
    }
    catch (IOException ex)
    {
      log.error(null, ex);
      fail(ex.getLocalizedMessage());
    }
  }
}
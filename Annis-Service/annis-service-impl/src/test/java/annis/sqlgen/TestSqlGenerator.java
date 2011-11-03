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
package annis.sqlgen;

import org.junit.Test;


//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"SqlGenerator-context.xml", 
//		"classpath:de/deutschdiachrondigital/dddquery/parser/DddQueryParser-context.xml"})
public class TestSqlGenerator {

	// VR: functionality moved to BaseSqlGenerator and UnionBaseSqlGenerator
	@Test public void dummyTest() { };
	
	
//	// simple SqlGenerator instance with mocked dependencies
//	private SqlGenerator sqlGenerator;
//	private @Mock ClauseSqlGenerator clauseSqlGenerator;
//	private @Mock QueryAnalysis queryAnalysis;
//	
//	// SqlGenerator that is managed by Spring
//	@Autowired @Qualifier("find") private SqlGenerator springManagedSqlGenerator;
//	@Autowired private DddQueryParser parser;
//  @Autowired private QueryAnalysis springManagedQueryAnalysis;
//
//	@Before
//	public void setup() {
//		initMocks(this);
//		sqlGenerator = new SqlGenerator();
//		sqlGenerator.setClauseSqlGenerator(clauseSqlGenerator);
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void toSql() {
//		// statement that should be transformed into SQL
//		final Start statement = new Start();
//		
//		// is represented by the following data
//		QueryData queryData = mock(QueryData.class);
//		List<Long> corpusList = mock(List.class);
//    List<Long> documentList = mock(List.class);
//		when(queryData.getCorpusList()).thenReturn(corpusList);
//		List<AnnisNode> nodes1 = mock(List.class);
//		List<AnnisNode> nodes2 = mock(List.class);
//		when(queryData.getAlternatives()).thenReturn(Arrays.asList(nodes1, nodes2));
//		final Integer maxWidth = new Integer(5);
//		when(queryData.getMaxWidth()).thenReturn(maxWidth);
//		List<Annotation> metaData = mock(List.class);
//		when(queryData.getMetaData()).thenReturn(metaData);
//		
//		// stub in the sample query data
//		when(queryAnalysis.analyzeQuery(any(Start.class), anyList())).thenReturn(queryData);
//		
//		// stub SQL code of individual clauses
//		final String sql1 = "SELECT 1";
//		final String sql2 = "SELECT 2";
//		sqlGenerator.setClauseSqlGenerator(clauseSqlGenerator);
//		when(clauseSqlGenerator.toSql(anyList(), anyInt(), anyList(), anyList())).thenReturn(sql1, sql2);
//
//    // convert statement to SQL
//		String sql = sqlGenerator.toSql(queryData, corpusList, documentList);
//		
//		// verify flow control
//    
//		// each clause (list of nodes) is transformed to SQL with correct width
//		verify(clauseSqlGenerator).toSql(nodes1, maxWidth, corpusList, documentList);
//		verify(clauseSqlGenerator).toSql(nodes2, maxWidth, corpusList, documentList);
//
//		// check for correct SQL
//		assertEquals(sql1 + "\n\nUNION " + sql2, sql);
//		
//	}
//	
//	// Spring managed instance has its dependencies set
//	@Test
//	public void springManagedInstanceHasAllDependencies() {
//		assertThat(springManagedSqlGenerator.getClauseSqlGenerator(), is(not(nullValue())));
//		ClauseSqlGenerator clauseSqlGenerator = springManagedSqlGenerator.getClauseSqlGenerator();
//		assertThat(clauseSqlGenerator, is(not(nullValue())));
//		assertThat(clauseSqlGenerator.getSelectClauseSqlGenerators(), is(not(empty())));
//		assertThat(clauseSqlGenerator.getFromClauseSqlGenerators(), is(not(empty())));
//		assertThat(clauseSqlGenerator.getWhereClauseSqlGenerators(), is(not(empty())));
//	}
//	
//	// XXX: move me?
//	@Test
//	public void dump() {
////		dumpSql("/a#(n1)/b#(n2)");
////		dumpSql("element()#(n1)[. = \"N.*\"]");
//		dumpSql("element()#(n2)[@attribute(tiger:pos) = r\"S\"]");
////		dumpSql("element()#(n2)[@attribute(tiger:pos)][@attribute(urml:lemma) = r\"boink\"]");
////		dumpSql("element()#(n1)[@attribute(tiger:pos)]/right-child::element()#(n2)[@attribute(urml:lemma)]");
//	}
//
//	private void dumpSql(String input) {
//		System.out.println("-- " + input);
//		System.out.println(springManagedSqlGenerator.toSql(
//      springManagedQueryAnalysis.analyzeQuery(parser.parse((input)), null), null, null));
//		System.out.println();
//	}

}

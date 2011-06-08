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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import annis.model.Annotation;

public class TestListCorpusAnnotationsSqlHelper {

	private ListCorpusAnnotationsSqlHelper listCorpusAnnotationsHelper;
	
	@Before
	public void setup() {
		listCorpusAnnotationsHelper = new ListCorpusAnnotationsSqlHelper();
	}
	
	@Test
	public void createSqlQuery() {
		final long ID = 42L;
		final String expected =
			"SELECT corpus_annotation.* " +
			"FROM corpus_annotation, corpus this, corpus parent " +
			"WHERE this.id = " + String.valueOf(ID) + " " +
			"AND this.pre >= parent.pre AND this.post <= parent.post " +
			"AND corpus_annotation.corpus_ref = parent.id";
		assertThat(listCorpusAnnotationsHelper.createSqlQuery(ID), is(expected));
	}
	
	@Test
	public void mapRow() throws SQLException {
		// stub a row in the ResultSet
		ResultSet resultSet = mock(ResultSet.class);
		final String NAMESPACE = "NAMESPACE";
		final String NAME = "NAME";
		final String VALUE = "VALUE";
		when(resultSet.getString("namespace")).thenReturn(NAMESPACE);
		when(resultSet.getString("name")).thenReturn(NAME);
		when(resultSet.getString("value")).thenReturn(VALUE);
		
		// expected annotation
		Annotation expected = new Annotation(NAMESPACE, NAME, VALUE);
		
		// call and test
		assertThat(listCorpusAnnotationsHelper.mapRow(resultSet, 1), is(expected));
	}
	
}

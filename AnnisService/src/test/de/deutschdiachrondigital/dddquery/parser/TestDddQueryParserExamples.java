package de.deutschdiachrondigital.dddquery.parser;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static test.TestHelper.springFiles;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import test.SpringQueryExamples;
import test.SpringSyntaxTreeExamples;
import test.SyntaxTreeExample;
import de.deutschdiachrondigital.dddquery.node.Start;

// see http://junit.sourceforge.net/doc/ReleaseNotes4.4.html
// and http://popper.tigris.org/tutorial.html
@RunWith(Theories.class)
public class TestDddQueryParserExamples {
	
	// where to find query and syntax tree examples
	private static final String EXAMPLES = "de/deutschdiachrondigital/dddquery/parser/TestDddQueryParser-examples.xml";

	// access to DddQueryParser
	private static ApplicationContext ctx;

	// simple DddQueryParser instance
	private DddQueryParser parser;
	
	// load Spring application context once
	@BeforeClass
	public static void loadApplicationContext() {
		final String[] ctxFiles = 
			springFiles(TestDddQueryParserExamples.class, "DddQueryParser-context.xml");
		ctx = new ClassPathXmlApplicationContext(ctxFiles);
	}
	
	// setup a fresh parser
	@Before
	public void setup() {
		parser = (DddQueryParser) ctx.getBean("dddQueryParser");
	}
	
	///// Syntax-Tree tests
	
	@Theory
	public void testSyntaxTrees(
			@SpringSyntaxTreeExamples(exampleMap = "exampleSyntaxTrees", contextLocation=EXAMPLES) 
			SyntaxTreeExample example) {
		Start syntaxTree = parser.parse(example.getQuery());
		assertThat(syntaxTree, is(not(nullValue())));
		String actual = DddQueryParser.dumpTree(syntaxTree).trim();
		assertEquals("wrong syntax tree for: " + example.getQuery(), example.getSyntaxTree(), actual);
	}
	
	///// Examples for good and bad queries
	
	@Theory
	public void testGoodQueries(
			@SpringQueryExamples(exampleList = "good", contextLocation=EXAMPLES) String dddQuery) {
		assertThat(parser.parse(dddQuery), is(not(nullValue())));
	}
	
	@Theory
	public void testBadQueries(
			@SpringQueryExamples(exampleList = "bad", contextLocation=EXAMPLES) String dddQuery) {
		try {
			parser.parse(dddQuery);
			fail("bad query passed as good: " + dddQuery);
		} catch (ParseException e) {
			// ok
		}
	}
	
}

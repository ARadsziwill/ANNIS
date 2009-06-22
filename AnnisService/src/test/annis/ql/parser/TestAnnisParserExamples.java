package annis.ql.parser;

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
import annis.exceptions.AnnisException;
import annis.ql.node.Start;

// see http://junit.sourceforge.net/doc/ReleaseNotes4.4.html
// and http://popper.tigris.org/tutorial.html
@RunWith(Theories.class)
public class TestAnnisParserExamples {
	
	// where to find query and syntax tree examples
	private static final String EXAMPLES = "annis/ql/parser/TestAnnisParser-examples.xml";

	// access to AnnisParser
	static ApplicationContext ctx;

	// simple DddQueryParser instance
	private AnnisParser parser;
	
	// load Spring application context once
	@BeforeClass
	public static void loadApplicationContext() {
		final String[] ctxFiles = 
			springFiles(TestAnnisParserExamples.class, "AnnisParser-context.xml");
		ctx = new ClassPathXmlApplicationContext(ctxFiles);
	}
	
	// setup a fresh parser
	@Before
	public void setup() {
		parser = (AnnisParser) ctx.getBean("annisParser");
	}
	
	///// Syntax-Tree tests
	
	@Theory
	public void testSyntaxTrees(
			@SpringSyntaxTreeExamples(exampleMap = "exampleSyntaxTrees", contextLocation=EXAMPLES) 
			SyntaxTreeExample example) {
		Start syntaxTree = parser.parse(example.getQuery());
		assertThat(syntaxTree, is(not(nullValue())));
		String actual = parser.dumpTree(syntaxTree).trim();
		assertEquals("wrong syntax tree for: " + example.getQuery(), example.getSyntaxTree(), actual);
	}

	@Theory
	public void testGoodQueries(
			@SpringQueryExamples(exampleList = "good", contextLocation=EXAMPLES) 
			String annisQuery) {
		assertThat(parser.parse(annisQuery), is(not(nullValue())));
	}
	
	@Theory
	public void testBadQueries(
			@SpringQueryExamples(exampleList = "bad", contextLocation=EXAMPLES) 
			String annisQuery) {
		try {
			parser.parse(annisQuery);
			fail("bad query passed as good: " + annisQuery);
		} catch (AnnisException e) {
			// ok
		}
	}
	
}

package org.beanio.parser.constructor;

import org.beanio.*;
import org.beanio.parser.ParserTest;
import org.junit.*;

/**
 * JUnit test cases for fields and segments that use dynamic occurrences.
 * @author Kevin Seim
 */
public class ConstructorParserTest extends ParserTest {

    private StreamFactory factory;

    @Before
    public void setup() throws Exception {
        factory = newStreamFactory("constructor_mapping.xml");
    }
    
    @Test
    public void testBasic() {
        Unmarshaller u = factory.createUnmarshaller("c1");
        
        Color color = (Color) u.unmarshal("red,255,0,0");
        
        Assert.assertEquals("red", color.getName());
        Assert.assertEquals(255, color.getR());
        Assert.assertEquals(0, color.getG());
        Assert.assertEquals(0, color.getB());
    }
}


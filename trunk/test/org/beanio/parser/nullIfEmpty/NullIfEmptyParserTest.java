package org.beanio.parser.nullIfEmpty;

import java.util.Map;

import org.beanio.*;
import org.beanio.parser.ParserTest;
import org.junit.*;

/**
 * JUnit test cases for the 'nullIfEmpty' field attribute.
 * @author Kevin Seim
 */
public class NullIfEmptyParserTest extends ParserTest {

    private StreamFactory factory;

    @Before
    public void setup() throws Exception {
        factory = newStreamFactory("nullIfEmpty_mapping.xml");
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testBasic() {
        Unmarshaller u = factory.createUnmarshaller("n1");
        
        Map map = (Map) u.unmarshal(",");
        Assert.assertTrue(map.containsKey("field1"));
        Assert.assertEquals(null, map.get("field1"));
        Assert.assertEquals("", map.get("field2"));
        Assert.assertFalse(map.containsKey("field3"));
        
        map = (Map) u.unmarshal(" ,, ");
        Assert.assertEquals(" ", map.get("field1"));
        Assert.assertEquals("", map.get("field2"));
        Assert.assertTrue(map.containsKey("field3"));
        Assert.assertEquals(null, map.get("field3"));
    }
}

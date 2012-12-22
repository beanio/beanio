package org.beanio.parser.inlinemaps;

import java.util.Map;

import org.beanio.*;
import org.beanio.beans.Person;
import org.beanio.parser.ParserTest;
import org.junit.*;

/**
 * Unit test cases for inline maps (e.g. <tt>key1,value1,key2,value2</tt>).
 * 
 * @author Kevin Seim
 */
@SuppressWarnings("rawtypes")
public class InlineMapParserTest extends ParserTest {

    private StreamFactory factory;

    @Before
    public void setup() throws Exception {
        factory = newStreamFactory("map_mapping.xml");
    }
    
    @Test
    public void testMap_WithClass() {
        Unmarshaller u = factory.createUnmarshaller("stream1");
        Marshaller m = factory.createMarshaller("stream1");
        
        String text = "js,Joe,Smith,bm,Bob,Marshall";
        
        Person person;
        Map map = (Map) u.unmarshal(text);
        
        Assert.assertEquals(map.size(), 2);
        Assert.assertTrue(map.containsKey("js"));
        
        person = (Person) map.get("js");
        Assert.assertEquals("js", person.getId());
        Assert.assertEquals("Joe", person.getFirstName());
        Assert.assertEquals("Smith", person.getLastName());
        
        Assert.assertTrue(map.containsKey("bm"));
        person = (Person) map.get("bm");
        Assert.assertEquals("bm", person.getId());
        Assert.assertEquals("Bob", person.getFirstName());
        Assert.assertEquals("Marshall", person.getLastName());    
        
        Assert.assertEquals(text, m.marshal(map).toString());
    }
    
    @Test
    public void testMap_WithTarget() {
        Unmarshaller u = factory.createUnmarshaller("stream2");
        Marshaller m = factory.createMarshaller("stream2");
        
        String text = "js,Joe,Smith,bm,Bob,Marshall";
        
        Map map = (Map) u.unmarshal(text);
        
        Assert.assertEquals(map.size(), 2);
        Assert.assertTrue(map.containsKey("js"));
        Assert.assertEquals("Joe", map.get("js"));
        Assert.assertTrue(map.containsKey("bm"));
        Assert.assertEquals("Bob", map.get("bm"));   
        
        Assert.assertEquals("js,Joe,,bm,Bob,", m.marshal(map).toString());
    }
}

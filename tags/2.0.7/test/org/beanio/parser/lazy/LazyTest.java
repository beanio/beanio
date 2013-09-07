package org.beanio.parser.lazy;

import static org.junit.Assert.*;

import org.beanio.*;
import org.beanio.parser.ParserTest;
import org.junit.*;

/**
 * JUnit test cases for testing the <tt>lazy</tt> attribute of the <tt>segment</tt> element.
 * 
 * @author Kevin Seim
 * @since 2.0.2
 */
public class LazyTest extends ParserTest {

    private StreamFactory factory;

    @Before
    public void setup() throws Exception {
        factory = newStreamFactory("lazy_mapping.xml");
    }

    @Test
    public void testLazySegment() {
        Unmarshaller u = factory.createUnmarshaller("s1");
        
        LazyUser user = (LazyUser) u.unmarshal("kevin          ");
        assertEquals("kevin", user.name);
        assertNull(user.account);
        
        user = (LazyUser) u.unmarshal("kevin1         ");
        assertEquals("kevin", user.name);
        assertNotNull(user.account);
        assertEquals(new Integer(1), user.account.getNumber());
        assertEquals("", user.account.getText());
    }
    
    @Test
    public void testRepeatingLazySegments() {
        Unmarshaller u = factory.createUnmarshaller("s2");
        
        LazyUser user = (LazyUser) u.unmarshal("kevin      ");
        assertEquals("kevin", user.name);
        assertNotNull(user.accounts);
        assertEquals(0, user.accounts.size());
        
        user = (LazyUser) u.unmarshal("kevin   001");
        assertEquals("kevin", user.name);
        assertNotNull(user.accounts);
        assertEquals(1, user.accounts.size());        
    }
}

package org.beanio.beans

/**
 * A common bean object used by Groovy test cases.
 * @author Kevin Seim
 */
class Bean {

    // simple properties
	String type;
	String text;
    
    // collection properties
	Map map;
	List list;
	
    // bean properties
	Bean group;
	Bean record;
	Bean segment;
    
}

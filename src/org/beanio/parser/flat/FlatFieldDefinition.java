package org.beanio.parser.flat;

import java.util.List;

import org.beanio.parser.*;

/**
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public abstract class FlatFieldDefinition extends FieldDefinition {


    protected int getCurrentPosition(Record record) {
        List<Integer> offsetStack = record.getFieldOffsets();
        if (offsetStack.isEmpty()) {
            return getPosition();
        }
        
        int n = 0;
        PropertyDefinition property = this;
        for (Integer offset : offsetStack) {
            while (!property.isCollection()) {
                property = property.getParent();
            }
            
            n += offset * property.getLength();
            
            property = property.getParent();
        }
        return getPosition() + n;
    }
    
}

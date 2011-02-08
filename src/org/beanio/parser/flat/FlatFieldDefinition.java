package org.beanio.parser.flat;

import java.util.List;

import org.beanio.parser.*;

/**
 * Provides support for fields belonging to a fixed length or delimited record.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public abstract class FlatFieldDefinition extends FieldDefinition {

    /**
     * Returns the position of this field in the record, accounting for field collections
     * and children of bean collections.
     * @param record the record being parsed
     * @return the position of this field
     */
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

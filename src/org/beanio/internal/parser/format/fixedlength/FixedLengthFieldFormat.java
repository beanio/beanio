/*
 * Copyright 2011-2012 Kevin Seim
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.beanio.internal.parser.format.fixedlength;

import org.beanio.internal.parser.*;
import org.beanio.internal.parser.format.FieldPadding;
import org.beanio.internal.parser.format.flat.FlatFieldFormatSupport;

/**
 * A {@link FieldFormat} implementation for a field in a fixed length formatted stream.
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class FixedLengthFieldFormat extends FlatFieldFormatSupport implements FieldFormat {

    /**
     * Constructs a new <tt>FixedLengthFieldFormat</tt>.
     */
    public FixedLengthFieldFormat() { }
    
    @Override
    public String extract(UnmarshallingContext context, boolean reportErrors) {
        String text = extractFieldText(context, reportErrors);
        if (text == null) {
            return null;
        }
        
        FieldPadding padding = getPadding();
        if (text.length() != padding.getLength()) {
            if (reportErrors) {
                context.addFieldError(getName(), text, "length", padding.getLength());
            }
            return Value.INVALID;
        }
        else {
            return padding.unpad(text);
        }
    }
    
    @Override
    public String extractFieldText(UnmarshallingContext context, boolean reporting) {
        FixedLengthUnmarshallingContext ctx = ((FixedLengthUnmarshallingContext)context);
        return ctx.getFieldText(getName(), getPosition(), getSize());
    }

    @Override
    public void insertFieldText(MarshallingContext context, String fieldText, boolean commit) {
        FixedLengthMarshallingContext ctx = ((FixedLengthMarshallingContext)context);
        ctx.setFieldText(getPosition(), fieldText, commit);
    }

    @Override
    public int getSize() {
        return getPadding().getLength();
    }
}

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
package org.beanio.internal.parser;

import java.io.IOException;

/**
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public abstract class DelegatingParser extends ParserComponent {

    /**
     * Constructs a new <tt>DelegatingParser</tt>.
     */
    public DelegatingParser() {
        super(1);
    }
    
    public String getName() {
        return getParser().getName();
    }
    
    public boolean matches(UnmarshallingContext context) {
        return getParser().matches(context);
    }

    public boolean unmarshal(UnmarshallingContext context) {
        return getParser().unmarshal(context);
    }
    
    

    public boolean marshal(MarshallingContext context) throws IOException {
        return getParser().marshal(context);
    }

    public void clearValue() {
        getParser().clearValue();
    }

    public void setValue(Object value) {
        getParser().setValue(value);
    }

    public Object getValue() {
        return getParser().getValue();
    }

    public int getSize() {
        return getParser().getSize();
    }

    public boolean isLazy() {
        return getParser().isLazy();
    }
    
    public boolean isIdentifier() {
        return getParser().isIdentifier();
    }

    public boolean hasContent() {
        return getParser().hasContent();
    }
    
    protected Parser getParser() {
        return (Parser) getFirst();
    }
    
}

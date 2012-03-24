/*
 * Copyright 2010 Kevin Seim
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
package org.beanio.stream.fixedlength;

import java.io.Reader;

import org.beanio.stream.*;

/**
 * This record reader factory is used to create and configure a <tt>FixedLengthReader</tt>.
 * 
 * @author Kevin Seim
 * @since 1.0
 * @see FixedLengthReader
 */
public class FixedLengthReaderFactory extends FixedLengthReaderConfiguration implements RecordReaderFactory {

    /*
     * (non-Javadoc)
     * @see org.beanio.stream.RecordReaderFactory#createReader(java.io.Reader)
     */
    public RecordReader createReader(Reader in) {
        return new FixedLengthReader(in, this);
    }

}

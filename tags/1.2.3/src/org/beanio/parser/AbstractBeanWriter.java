/*
 * Copyright 2010-2011 Kevin Seim
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
package org.beanio.parser;

import java.io.IOException;

import org.beanio.*;
import org.beanio.stream.RecordWriter;

/**
 * The <tt>AbstractBeanWriter</tt> implements common methods for <tt>BeanWriter</tt>
 * implementations.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public abstract class AbstractBeanWriter implements BeanWriter {

    protected RecordWriter out;

    /**
     * Constructs a new <tt>AbstractBeanWriter</tt>.
     * @param out the output stream to write to
     */
    public AbstractBeanWriter(RecordWriter out) {
        this.out = out;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.BeanWriter#flush()
     */
    public void flush() {
        try {
            out.flush();
        }
        catch (IOException e) {
            throw new BeanWriterIOException("IOException caught flushing output stream", e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.BeanWriter#close()
     */
    public void close() {
        try {
            out.close();
        }
        catch (IOException e) {
            throw new BeanWriterIOException("IOException caught closing output stream", e);
        }
    }

}

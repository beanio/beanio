package org.beanio.stream;

import java.io.*;

/**
 * A base class for implementing a custom {@link RecordParserFactory}.  Unless
 * overridden, all createXXX() methods will throw an {@link UnsupportedOperationException}.
 * 
 * @author Kevin Seim
 * @since 2.0.4
 */
public class RecordParserFactorySupport implements RecordParserFactory {

    public void init() throws IllegalArgumentException { }

    public RecordReader createReader(Reader in) throws IllegalArgumentException {
        throw new UnsupportedOperationException("BeanReader not supported");
    }

    public RecordWriter createWriter(Writer out) throws IllegalArgumentException {
        throw new UnsupportedOperationException("BeanWriter not supported");
    }

    public RecordMarshaller createMarshaller() throws IllegalArgumentException {
        throw new UnsupportedOperationException("Marshaller not supported");
    }

    public RecordUnmarshaller createUnmarshaller() throws IllegalArgumentException {
        throw new UnsupportedOperationException("Unmarshaller not supported");
    }
}

package org.beanio.types;

import java.net.*;

/**
 * A type handler for {@link URL} values.
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class URLTypeHandler implements TypeHandler {

    /**
     * Parses a {@link URL} using its constructor {@link URL#URL(String)}.
     * @param text the text to parse
     * @return the parsed {@link URL} or null if <tt>text</tt>
     *   is null or an empty string
     */
    public Object parse(String text) throws TypeConversionException {
        if (text == null || "".equals(text)) {
            return null;
        }
        
        try {
            return new URL(text);
        }
        catch (MalformedURLException ex) {
            throw new TypeConversionException("Invalid URL " +
                "value '" + text + "'", ex);
        }
    }

    /**
     * Formats a {@link URL} by calling <tt>toString()</tt>.  If <tt>value</tt> is
     * null, <tt>null</tt> is returned.
     * @param value the {@link URL} to format
     * @return the formatted text
     */
    public String format(Object value) {
        if (value == null)
            return null;
        else
            return value.toString();
    }

    /**
     * Returns {@link URL}.
     */
    public Class<?> getType() {
        return URL.class;
    }

}

package org.beanio.types;

import java.util.UUID;

/**
 * A type handler for {@link UUID} values.
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class UUIDTypeHandler implements TypeHandler {

    /**
     * Parses a UUID using {@link UUID#fromString(String)}.
     * @param text the text to parse
     * @return the parsed {@link UUID} or null if <tt>text</tt>
     *   is null or an empty string
     */
    public Object parse(String text) throws TypeConversionException {
        if (text == null || "".equals(text)) {
            return null;
        }
        
        try {
            return UUID.fromString(text);
        }
        catch (IllegalArgumentException ex) {
            throw new TypeConversionException("Invalid UUID " +
                "value '" + text + "'", ex);
        }
    }

    /**
     * Formats a {@link UUID} by calling <tt>toString()</tt>.  If <tt>value</tt> is
     * null, <tt>null</tt> is returned.
     * @param value the {@link UUID} to format
     * @return the formatted text
     */
    public String format(Object value) {
        if (value == null)
            return null;
        else
            return value.toString();
    }

    /**
     * Returns {@link UUID}.
     */
    public Class<?> getType() {
        return UUID.class;
    }
}

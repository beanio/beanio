package org.beanio.types;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Properties;

/**
 * This type handler can parse/format {@link TemporalAccessor}s like {@link LocalDateTime} or {@link
 * java.time.ZonedDateTime} using a {@link DateTimeFormatter}.
 */
public class TemporalAccessorTypeHandler implements ConfigurableTypeHandler {

    private DateTimeFormatter formatter;
    private Class<?> type;

    public TemporalAccessorTypeHandler() {
    }

    public TemporalAccessorTypeHandler(Class<?> type, DateTimeFormatter formatter) {
        this.type = type;
        this.formatter = formatter;
    }

    @Override
    public TypeHandler newInstance(Properties properties) {
        String pattern = properties.getProperty(FORMAT_SETTING);
        if (pattern == null || "".equals(pattern)) {
            return this;
        }

        DateTimeFormatter newFormatter = DateTimeFormatter.ofPattern(pattern);

        TemporalAccessorTypeHandler clone = new TemporalAccessorTypeHandler();
        clone.type = type;
        clone.formatter = newFormatter;
        return clone;
    }

    @Override
    public Object parse(String text) throws TypeConversionException {
        try {
            TemporalAccessor temporalAccessor = formatter.parse(text);

            if (type == LocalDate.class) {
                return LocalDate.from(temporalAccessor);
            } else if (type == LocalTime.class) {
                return LocalTime.from(temporalAccessor);
            } else if (type == LocalDateTime.class) {
                return LocalDateTime.from(temporalAccessor);
            } else if (type == ZonedDateTime.class) {
                return ZonedDateTime.from(temporalAccessor);
            }

            return temporalAccessor;
        } catch (DateTimeException exception) {
            throw new TypeConversionException(exception);
        }
    }

    @Override
    public String format(Object value) {
        if (value == null) {
            return null;
        }
        TemporalAccessor temporalAccessor = (TemporalAccessor) value;
        return formatter.format(temporalAccessor);
    }

    /**
     * Used in custom type handlers defined in XML configurations to specify the actual
     * subclass of {@link TemporalAccessor} to handle.
     */
    @SuppressWarnings("unused")
    public void setTypeName(String typeName) throws ClassNotFoundException {
        type = Class.forName(typeName);
    }

    @Override
    public Class<?> getType() {
        return type;
    }
}
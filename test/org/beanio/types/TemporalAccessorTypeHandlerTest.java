package org.beanio.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Properties;
import org.beanio.BeanReader;
import org.beanio.BeanWriter;
import org.beanio.InvalidRecordException;
import org.beanio.StreamFactory;
import org.beanio.beans.JavaTime;
import org.beanio.internal.util.TypeHandlerFactory;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

public class TemporalAccessorTypeHandlerTest {

    private final TypeHandlerFactory typeHandlerFactory = TypeHandlerFactory.getDefault();

    @Test
    public void testNullValue() {
        // Given
        TypeHandler typeHandler = typeHandlerFactory.getTypeHandlerFor(LocalDate.class);

        // When
        String formatted = typeHandler.format(null);

        // Then
        assertNull(formatted);
    }

    @Test
    public void testLocalDate() throws TypeConversionException {
        // Given
        TypeHandler typeHandler = typeHandlerFactory.getTypeHandlerFor(LocalDate.class);
        LocalDate localDate = LocalDate.of(2021, 3, 14);

        // When
        String formatted = typeHandler.format(localDate);
        Object parsed = typeHandler.parse("2021-03-14");

        // Then
        assertEquals("2021-03-14", formatted);
        assertTrue(parsed instanceof LocalDate);
        assertEquals(localDate, parsed);
    }

    @Test
    public void testLocalTime() throws TypeConversionException {
        // Given
        TypeHandler typeHandler = typeHandlerFactory.getTypeHandlerFor(LocalTime.class);
        LocalTime localTime = LocalTime.of(20, 34, 14);

        // When
        String formatted = typeHandler.format(localTime);
        Object parsed = typeHandler.parse("20:34:14");

        // Then
        assertEquals("20:34:14", formatted);
        assertTrue(parsed instanceof LocalTime);
        assertEquals(localTime, parsed);
    }

    @Test
    public void testLocalDateTime() throws TypeConversionException {
        // Given
        TypeHandler typeHandler = typeHandlerFactory.getTypeHandlerFor(LocalDateTime.class);
        LocalDate localDate = LocalDate.of(2021, 3, 14);
        LocalTime localTime = LocalTime.of(20, 34, 14);
        LocalDateTime localDateTime = localDate.atTime(localTime);

        // When
        String formatted = typeHandler.format(localDateTime);
        Object parsed = typeHandler.parse("2021-03-14T20:34:14");

        // Then
        assertEquals("2021-03-14T20:34:14", formatted);
        assertTrue(parsed instanceof LocalDateTime);
        assertEquals(localDateTime, parsed);
    }

    @Test
    public void testLocalDateTimeWithCustomFormat() throws TypeConversionException {
        // Given
        TypeHandler typeHandler = typeHandlerFactory.getTypeHandlerFor(LocalDateTime.class);
        Properties properties = new Properties();
        properties.setProperty(ConfigurableTypeHandler.FORMAT_SETTING, "dd/MM/yy - HH:mm:ss");
        typeHandler = ((ConfigurableTypeHandler) typeHandler).newInstance(properties);

        LocalDate localDate = LocalDate.of(2021, 3, 14);
        LocalTime localTime = LocalTime.of(20, 34, 14);
        LocalDateTime localDateTime = localDate.atTime(localTime);

        // When
        String formatted = typeHandler.format(localDateTime);
        Object parsed = typeHandler.parse("14/03/21 - 20:34:14");

        // Then
        assertEquals("14/03/21 - 20:34:14", formatted);
        assertTrue(parsed instanceof LocalDateTime);
        assertEquals(localDateTime, parsed);
    }

    @Test
    public void testParseInvalidLocalDateTime() {
        // Given
        TypeHandler typeHandler = typeHandlerFactory.getTypeHandlerFor(LocalDateTime.class);

        // When
        ThrowingRunnable throwing = () -> typeHandler.parse("🤮");

        // Then
        assertThrows(TypeConversionException.class, throwing);
    }

    @Test
    public void testParseEmptyString() throws TypeConversionException {
        // Given
        TypeHandler typeHandler = typeHandlerFactory.getTypeHandlerFor(LocalDateTime.class);

        // When
        final Object result = typeHandler.parse("");

        // Then
        assertNull(result);
    }

    @Test
    public void testNewInstanceWithNullPatternReturnsSameInstance() {
        // Given
        TypeHandler typeHandler = typeHandlerFactory.getTypeHandlerFor(LocalDateTime.class);
        Properties properties = new Properties();

        // When
        TypeHandler newInstance = ((ConfigurableTypeHandler) typeHandler).newInstance(properties);

        // Then
        assertSame(typeHandler, newInstance);
    }

    @Test
    public void testNewInstanceWithEmptyPatternReturnsSameInstance() {
        // Given
        TypeHandler typeHandler = typeHandlerFactory.getTypeHandlerFor(LocalDateTime.class);
        Properties properties = new Properties();
        properties.setProperty(ConfigurableTypeHandler.FORMAT_SETTING, "");

        // When
        TypeHandler newInstance = ((ConfigurableTypeHandler) typeHandler).newInstance(properties);

        // Then
        assertSame(typeHandler, newInstance);
    }

    @Test
    public void testCustomTypeHandler() {
        // Given
        StreamFactory streamFactory = StreamFactory.newInstance();
        streamFactory.loadResource("org/beanio/types/custom-java-time.xml");

        JavaTime javaTime = new JavaTime();
        javaTime.setLocalDateTime(LocalDateTime.of(2021, 3, 14, 21, 40, 23));

        StringWriter writer = new StringWriter();

        // When
        BeanWriter beanWriter = streamFactory.createWriter("my-stream", writer);
        beanWriter.write(javaTime);

        BeanReader reader = streamFactory.createReader("my-date-stream", new StringReader("14/03-21"));
        Object read = reader.read();

        // Then
        assertEquals("14/03, 21:40\n", writer.toString());
        assertTrue(read instanceof JavaTime);
        assertEquals(LocalDate.of(2021, 3, 14), ((JavaTime) read).getLocalDate());
    }

    @Test
    public void testCustomTypeHandlerWithStrictParsing() {
        // Given
        StreamFactory streamFactory = StreamFactory.newInstance();
        streamFactory.loadResource("org/beanio/types/custom-strict-java-date.xml");

        // When
        BeanReader reader = streamFactory.createReader("my-strict-date-stream", new StringReader("20240202"));
        Object read = reader.read();

        // Then
        assertTrue(read instanceof JavaTime);
        assertEquals(LocalDate.of(2024, 2, 2), ((JavaTime) read).getLocalDate());
    }

    @Test(expected = InvalidRecordException.class)
    public void testFailedCustomTypeHandlerWithStrictParsing() {
        // Given
        StreamFactory streamFactory = StreamFactory.newInstance();
        streamFactory.loadResource("org/beanio/types/custom-strict-java-date.xml");

        // When
        BeanReader reader = streamFactory.createReader("my-strict-date-stream", new StringReader("20240231"));
        reader.read();
    }

    @Test
    public void testZonedDateTime() throws TypeConversionException {
        // Given
        TypeHandler typeHandler = typeHandlerFactory.getTypeHandlerFor(ZonedDateTime.class);
        LocalDate localDate = LocalDate.of(2021, 3, 14);
        LocalTime localTime = LocalTime.of(20, 34, 14);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDate, localTime, ZoneId.of("Europe/Paris"));

        // When
        String formatted = typeHandler.format(zonedDateTime);
        Object parsed = typeHandler.parse("2021-03-14T20:34:14+01:00[Europe/Paris]");

        // Then
        assertEquals("2021-03-14T20:34:14+01:00[Europe/Paris]", formatted);
        assertTrue(parsed instanceof ZonedDateTime);
        assertEquals(zonedDateTime, parsed);
    }

    @Test
    public void testOffsetDateTime() throws TypeConversionException {
        // Given
        TypeHandler typeHandler = typeHandlerFactory.getTypeHandlerFor(OffsetDateTime.class);
        LocalDate localDate = LocalDate.of(2021, 3, 14);
        LocalTime localTime = LocalTime.of(20, 34, 14);
        OffsetDateTime offsetDateTime = OffsetDateTime.of(localDate, localTime, ZoneOffset.ofHours(3));

        // When
        String formatted = typeHandler.format(offsetDateTime);
        Object parsed = typeHandler.parse("2021-03-14T20:34:14+03:00");

        // Then
        assertEquals("2021-03-14T20:34:14+03:00", formatted);
        assertTrue(parsed instanceof OffsetDateTime);
        assertEquals(offsetDateTime, parsed);
    }
}
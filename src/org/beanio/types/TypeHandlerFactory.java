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
package org.beanio.types;

import java.math.*;
import java.text.DateFormat;
import java.util.*;

import org.beanio.util.*;

/**
 * A factory class used to get a <tt>TypeHandler</tt> for parsing field text 
 * into field objects, and for formatting field objects into field text.
 * <p>
 * A <tt>TypeHandler</tt> is registered and retrieved by class, type alias, or name.  
 * In most cases, registering a type handler by type alias has the same effect as registering the
 * type handler using the target class associated with the alias.  There are two exceptions: 
 * type handlers can be specifically registered for '<tt>Date</tt>' and '<tt>Time</tt>' type aliases
 * without overriding the default Date type handler, which is registered for the class 
 * <tt>java.util.Date</tt> and type alias '<tt>DateTime</tt>'.
 * <p>
 * If a registered type handler implements the <tt>ConfigurableTypeHandler</tt> interface, 
 * handler properties can be overridden using a <tt>Properties</tt> object.  When the type handler
 * is retrieved, the factory calls {@link ConfigurableTypeHandler#newInstance(Properties)} to 
 * allow the type handler to return a customized version of itself.
 * <p>
 * By default, a <tt>TypeHandlerFactory</tt> holds a reference to a parent
 * factory.  If a factory cannot find a type handler, its parent will be checked
 * recursively until there is no parent left to check.
 * 
 * @author Kevin Seim
 * @since 1.0
 * @see TypeHandler
 * @see ConfigurableTypeHandler
 */
public class TypeHandlerFactory {

    private TypeHandlerFactory parent;
    private Map<String, TypeHandler> handlerMap = new HashMap<String, TypeHandler>();

    private static final String NAME_KEY = "name:";
    private static final String TYPE_KEY = "type:";
    
    /* The default type handler factory */
    private final static TypeHandlerFactory defaultFactory;
    static {
        defaultFactory = new TypeHandlerFactory(null);
        defaultFactory.registerHandlerFor(Character.class, new CharacterTypeHandler());
        defaultFactory.registerHandlerFor(String.class,  new StringTypeHandler());
        defaultFactory.registerHandlerFor(Byte.class, new ByteTypeHandler());
        defaultFactory.registerHandlerFor(Short.class, new ShortTypeHandler());
        defaultFactory.registerHandlerFor(Integer.class, new IntegerTypeHandler());
        defaultFactory.registerHandlerFor(Long.class, new LongTypeHandler());
        defaultFactory.registerHandlerFor(Float.class, new FloatTypeHandler());
        defaultFactory.registerHandlerFor(Double.class, new DoubleTypeHandler());
        defaultFactory.registerHandlerFor(BigDecimal.class, new BigDecimalTypeHandler());
        defaultFactory.registerHandlerFor(BigInteger.class, new BigIntegerTypeHandler());
        defaultFactory.registerHandlerFor(Boolean.class, new BooleanTypeHandler());

        Settings settings = Settings.getInstance();
        defaultFactory.registerHandlerFor(TypeUtil.DATETIME_ALIAS, new DateTypeHandler(
            settings.getProperty(Settings.DEFAULT_DATETIME_FORMAT)));
        defaultFactory.registerHandlerFor(TypeUtil.DATE_ALIAS, new DateTypeHandler(
            settings.getProperty(Settings.DEFAULT_DATE_FORMAT)) {
            protected DateFormat createDefaultDateFormat() {
                return DateFormat.getDateInstance();
            }
        });
        defaultFactory.registerHandlerFor(TypeUtil.TIME_ALIAS, new DateTypeHandler(
            settings.getProperty(Settings.DEFAULT_TIME_FORMAT)) {
            protected DateFormat createDefaultDateFormat() {
                return DateFormat.getTimeInstance();
            }
        });
    }

    /**
     * Constructs a new <tt>TypeHandlerFactory</tt> using the default type handler factory
     * for its parent.
     */
    public TypeHandlerFactory() {
        setParent(getDefault());
    }

    /**
     * Constructs a new <tt>TypeHandlerFactory</tt>.
     * @param parent the parent <tt>TypeHandlerFactory</tt>
     */
    public TypeHandlerFactory(TypeHandlerFactory parent) {
        setParent(parent);
    }

    /**
     * Returns a named type handler, or <tt>null</tt> if there is no type handler configured
     * for the given name in this factory or any of its ancestors.
     * @param name the name of type handler was registered under
     * @return the type handler, or <tt>null</tt> if there is no configured type handler
     *    registered for the name
     */
    public TypeHandler getTypeHandler(String name) {
        return getTypeHandler(name, null);
    }

    /**
     * Returns a named type handler, or <tt>null</tt> if there is no type handler configured
     * for the given name in this factory or any of its ancestors.
     * @param name the name the type handler was registered under
     * @param properties the custom properties for configuring the type handler
     * @return the type handler, or <tt>null</tt> if there is no configured type handler
     *    registered for the name
     * @throws IllegalArgumentException if a custom property value was invalid
     */
    public TypeHandler getTypeHandler(String name, Properties properties) throws IllegalArgumentException {
        if (name == null) {
            throw new NullPointerException();
        }
        return getHandler(NAME_KEY + name, properties);
    }

    /**
     * Returns the type handler for the given type, or <tt>null</tt> if there is no type 
     * handler configured for the type in this factory or any of its ancestors.
     * @param type the class name or type alias
     * @return the type handler, or <tt>null</tt> if there is no configured type handler
     *    registered for the type
     */
    public TypeHandler getTypeHandlerFor(String type) {
        return getTypeHandlerFor(type, null);
    }

    /**
     * Returns the type handler for the given type, or <tt>null</tt> if there is no type 
     * handler configured for the type in this factory or any of its ancestors.
     * @param type the property type
     * @param properties the custom properties for configuring the type handler
     * @return the type handler, or <tt>null</tt> if there is no configured type handler
     *    registered for the type
     * @throws IllegalArgumentException if a custom property value was invalid
     */
    public TypeHandler getTypeHandlerFor(String type, Properties properties) throws IllegalArgumentException {
        if (type == null) {
            throw new NullPointerException();
        }
        
        if (TypeUtil.isAliasOnly(type)) {
            type = type.toLowerCase();
        }
        else {
            Class<?> clazz = TypeUtil.toType(type);
            if (clazz == null) {
                return null;
            }
            type = clazz.getName();
        }

        return getHandler(TYPE_KEY + type, properties);
    }

    /**
     * Returns a type handler for a class, or <tt>null</tt> if there is no type 
     * handler configured for the class in this factory or any of its ancestors
     * @param clazz the target class to find a type handler for
     * @return the type handler, or null if the class is not supported
     */
    public TypeHandler getTypeHandlerFor(Class<?> clazz) {
        return getTypeHandlerFor(clazz, null);
    }
    
    /**
     * Returns a type handler for a class, or <tt>null</tt> if there is no type 
     * handler configured for the class in this factory or any of its ancestors
     * @param clazz the target class to find a type handler for
     * @param properties the custom properties for configuring the type handler
     * @return the type handler, or null if the class is not supported
     * @throws IllegalArgumentException if a custom property value was invalid
     */
    public TypeHandler getTypeHandlerFor(Class<?> clazz, Properties properties) throws IllegalArgumentException {
        if (clazz == null) {
            throw new NullPointerException();
        }
        clazz = TypeUtil.toWrapperClass(clazz);
        return getHandler(TYPE_KEY + clazz.getName(), properties);
    }

    private TypeHandler getHandler(String key, Properties properties) throws IllegalArgumentException {
        TypeHandler handler = null;
        TypeHandlerFactory factory = this;
        while (factory != null) {
            handler = factory.handlerMap.get(key);
            if (handler != null) {
                return getHandler(handler, properties);
            }
            factory = factory.parent;
        }
        return null;
    }

    private TypeHandler getHandler(TypeHandler handler, Properties properties) throws IllegalArgumentException {
        if (properties != null && !properties.isEmpty()) {
            if (handler instanceof ConfigurableTypeHandler) {
                handler = ((ConfigurableTypeHandler) handler).newInstance(properties);
            }
            else {
                String property = properties.keys().nextElement().toString();
                throw new IllegalArgumentException("'" + property + "' setting not supported by type handler");
            }
        }
        return handler;
    }

    /**
     * Registers a type handler in this factory.
     * @param name the name to register the type handler under
     * @param handler the type handler to register
     */
    public void registerHandler(String name, TypeHandler handler) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (handler == null) {
            throw new NullPointerException();
        }
        handlerMap.put(NAME_KEY + name, handler);
    }

    /**
     * Registers a type handler in this factory.
     * @param type the fully qualified class name or type alias to register the type handler for
     * @param handler the type handler to registere
     * @throws IllegalArgumentException if the type name is invalid or if the handler type is not 
     *   assignable from the type
     */
    public void registerHandlerFor(String type, TypeHandler handler) throws IllegalArgumentException {
        if (type == null) {
            throw new NullPointerException();
        }
        Class<?> clazz = TypeUtil.toType(type);
        if (clazz == null) {
            throw new IllegalArgumentException("Invalid type or type alias '" + type + "'");
        }
        if (TypeUtil.isAliasOnly(type)) {
            type = type.toLowerCase();
            registerHandlerFor(type, clazz, handler);
        }
        else {
            registerHandlerFor(clazz.getName(), clazz, handler);
        }
    }

    /**
     * Registers a type handler in this factory.
     * @param clazz the target class to register the type handler for
     * @param handler the type handler to register
     * @throws IllegalArgumentException if the handler type is not assignable from
     *   the registered class type
     */
    public void registerHandlerFor(Class<?> clazz, TypeHandler handler) throws IllegalArgumentException {
        if (clazz == null) {
            throw new NullPointerException();
        }
        clazz = TypeUtil.toWrapperClass(clazz);
        registerHandlerFor(clazz.getName(), clazz, handler);
    }
    
    /**
     * Sets the parent <tt>TypeHandlerFactory</tt>.
     * @param parent the parent <tt>TypeHandlerFactory</tt>
     * @since 1.1
     */
    public void setParent(TypeHandlerFactory parent) {
        this.parent = parent;
    }

    private void registerHandlerFor(String type, Class<?> clazz, TypeHandler handler) {
        if (!TypeUtil.isAssignable(clazz, handler.getType())) {
            throw new IllegalArgumentException("Type handler type '" +
                handler.getType().getName() + "' is not assignable from configured " +
                "type '" + clazz.getName() + "'");
        }
        handlerMap.put(TYPE_KEY + type, handler);
    }

    /**
     * Returns the default <tt>TypeHandlerFactory</tt>.
     * @return the default <tt>TypeHandlerFactory</tt>
     */
    public static TypeHandlerFactory getDefault() {
        return defaultFactory;
    }
}

/*
 * Copyright 2010-2012 Kevin Seim
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
package org.beanio;

import java.io.*;
import java.util.Locale;

import org.beanio.internal.util.*;

/**
 * A <tt>StreamFactory</tt> manages stream mapping configurations and is used create 
 * <tt>BeanWriter</tt> and <tt>BeanReader</tt> instances.
 * <p>
 * The default BeanIO <tt>StreamFactory</tt> implementation can be safely shared 
 * across multiple threads.
 * 
 * @author Kevin Seim
 * @since 1.0
 * @see BeanReader
 * @see BeanWriter
 */
public abstract class StreamFactory {

    private ClassLoader classLoader;
    
    /**
     * Constructs a new <tt>StreamFactory</tt>.
     */
    public StreamFactory() { }

    /**
     * Creates a new <tt>BeanReader</tt> for reading from a file.
     * @param name the configured stream mapping name
     * @param filename the name of the file to read
     * @return the new <tt>BeanReader</tt>
     * @throws IllegalArgumentException if there is no stream configured for the given name, or
     *   if the stream mapping mode does not support reading an input stream
     * @throws BeanReaderIOException if the file could not be opened for reading
     */
    public BeanReader createReader(String name, String filename) throws IllegalArgumentException, BeanReaderIOException {
        return createReader(name, new File(filename));
    }
    
    /**
     * Creates a new <tt>BeanReader</tt> for reading from a file.
     * @param name the configured stream mapping name
     * @param file the file to read
     * @return the new <tt>BeanReader</tt>
     * @throws IllegalArgumentException if there is no stream configured for the given name, or
     *   if the stream mapping mode does not support reading an input stream
     * @throws BeanReaderIOException if the file could not be opened for reading
     */
    public BeanReader createReader(String name, File file) throws IllegalArgumentException, BeanReaderIOException {
        if (!isMapped(name)) {
            throw new IllegalArgumentException("No stream mapping configured for name '" + name + "'");
        }
        
        Reader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            return createReader(name, in);
        }
        catch (IOException ex) {
            IOUtil.closeQuietly(in);
            throw new BeanReaderException("Failed to open file '" + file + "' for reading", ex);
        }
        catch (RuntimeException ex) {
            IOUtil.closeQuietly(in);
            throw ex;            
        }
    }

    /**
     * Creates a new <tt>BeanReader</tt> for reading from the given input stream.
     * @param name the configured stream mapping name
     * @param in the input stream to read from
     * @return the new <tt>BeanReader</tt>
     * @throws IllegalArgumentException if there is no stream configured for the given name, or
     *   if the stream mapping mode does not support reading an input stream
     */
    public BeanReader createReader(String name, Reader in) throws IllegalArgumentException {
        return createReader(name, in, Locale.getDefault());
    }

    /**
     * Creates a new <tt>BeanReader</tt> for reading from a stream.
     * @param name the configured stream mapping name
     * @param in the input stream to read from
     * @param locale the locale used to format error messages, or if null, {@link Locale#getDefault()} is used
     * @return the new <tt>BeanReader</tt>
     * @throws IllegalArgumentException if there is no stream configured for the given name, or
     *   if the stream mapping mode does not support reading an input stream
     */
    public abstract BeanReader createReader(String name, Reader in, Locale locale)
        throws IllegalArgumentException;

    /**
     * Creates a new <tt>BeanWriter</tt> for writing to the given file.
     * @param name the configured stream mapping name
     * @param file the file to write to
     * @return the new <tt>BeanReader</tt>
     * @throws IllegalArgumentException if there is no stream configured for the given name, or
     *   if the stream mapping mode does not support writing to an output stream
     * @throws BeanWriterIOException if the file could not be opened for writing
     */
    public BeanWriter createWriter(String name, File file) throws IllegalArgumentException, BeanWriterIOException {
        if (!isMapped(name)) {
            throw new IllegalArgumentException("No stream mapping configured for name '" + name + "'");
        }
        
        Writer out = null;
        try {
            out = new BufferedWriter(new FileWriter(file));
            return createWriter(name, out);
        }
        catch (IOException ex) {
            IOUtil.closeQuietly(out);
            throw new BeanWriterIOException("Failed to open file '" + file + "' for writing", ex);
        }
        catch (RuntimeException ex) {
            IOUtil.closeQuietly(out);
            throw ex;
        }
    }

    /**
     * Creates a new <tt>BeanWriter</tt> for writing to a stream.
     * @param name the configured stream mapping name
     * @param out the output stream to write to
     * @return the new BeanWriter
     * @throws IllegalArgumentException if there is no stream configured for the given name, or
     *   if the stream mapping mode does not support writing to an output stream
     */
    public abstract BeanWriter createWriter(String name, Writer out)
        throws IllegalArgumentException;

    /**
     * Loads a BeanIO configuration file from the application classpath.
     * @param resource the configuration resource name
     * @throws BeanIOException if an IOException or other fatal error is caught while
     *   loading the file
     * @throws BeanIOConfigurationException if the configuration could not be loaded or is invalid
     */
    public void loadResource(String resource) throws BeanIOException, BeanIOConfigurationException {
        InputStream in = null;
        try {
            in = getClassLoader().getResourceAsStream(resource);
            if (in == null) {
                throw new BeanIOConfigurationException("BeanIO configuration not found on classpath '" + resource + "'");
            }
            load(in);
        }
        catch (IOException ex) {
            throw new BeanIOException("Failed to load resource '" + resource + "' from classpath", ex);
        }
        finally {
            IOUtil.closeQuietly(in);
        }
    }
    
    /**
     * Loads a BeanIO configuration file and adds the configured streams to this factory.
     * If the given file name is not found in the file system, the file will be loaded
     * from the classpath.
     * @param filename the name of the BeanIO configuration file to load
     * @throws BeanIOException if an IOException or other fatal error is caught while
     *   loading the file
     * @throws BeanIOConfigurationException if the configuration is invalid
     */
    public void load(String filename) throws BeanIOException, BeanIOConfigurationException {
        load(new File(filename));
    }

    /**
     * Loads a BeanIO configuration file and adds the configured streams to this factory.
     * @param file the BeanIO configuration file to load
     * @throws BeanIOException if an IOException or other fatal error is caught while
     *   loading the file
     * @throws BeanIOConfigurationException if the configuration is invalid
     */
    public void load(File file) throws BeanIOException, BeanIOConfigurationException {
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            load(in);
        }
        catch (IOException ex) {
            throw new BeanIOException("Failed to load '" + file + "' from the file system", ex);
        }
        finally {
            IOUtil.closeQuietly(in);
        }
    }

    /**
     * Loads a BeanIO configuration and adds the configured streams to this factory.
     * @param in the input stream to read the configuration from
     * @throws BeanIOException if an IOException or other fatal error is caught while
     *   reading the input stream
     * @throws BeanIOConfigurationException if the configuration is invalid
     */
    public abstract void load(InputStream in) throws IOException, BeanIOConfigurationException;

    /**
     * Returns a new <tt>StreamFactory</tt> instance.  An implementation class is loaded
     * using the the BeanIO configuration setting <tt>org.beanio.streamFactory</tt>.
     * @return a new <tt>StreamFactory</tt>
     * @throws BeanIOException if a <tt>StreamFactory</tt> could not be created
     * @see Settings
     */
    public static StreamFactory newInstance() throws BeanIOException {
        return newInstance(null);
    }
    
    /**
     * Returns a new <tt>StreamFactory</tt> instance.  An implementation class is loaded
     * using the the BeanIO configuration setting <tt>org.beanio.streamFactory</tt>.
     * @param classLoader the {@link ClassLoader} to use to load the stream factory and
     *   all subcomponents.  If null, the current thread's context class loader is used.
     *   If there is no context class loader, the class loader that loaded this class
     *   is used.
     * @return a new <tt>StreamFactory</tt>
     * @throws BeanIOException if a <tt>StreamFactory</tt> could not be created
     * @see Settings
     * @since 2.0
     */
    public static StreamFactory newInstance(ClassLoader classLoader) throws BeanIOException {
        // find a default class loader
        if (classLoader == null) {
            try {
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            catch (Throwable t) { }
            
            if (classLoader == null) {
                classLoader = StreamFactory.class.getClassLoader();
            }
        }
        
        String className = Settings.getInstance(classLoader).getProperty(Settings.STREAM_FACTORY_CLASS);
        if (className == null) {
            throw new BeanIOException("Property '" + Settings.STREAM_FACTORY_CLASS + "' not set");
        }

        try {
            StreamFactory factory = (StreamFactory) classLoader.loadClass(className).newInstance();
            factory.setClassLoader(classLoader);
            factory.init();
            return factory;
        }
        catch (Exception ex) {
            throw new BeanIOException("Failed to load stream factory implementation class '" +
                className + "'", ex);
        }
    }
    
    /**
     * Test whether a mapping configuration exists for a named stream.
     * @param streamName the stream name to test for existence
     * @return <tt>true</tt> if a mapping configuration is found for the named stream
     * @since 1.2
     */
    public abstract boolean isMapped(String streamName);
    
    /**
     * This method is invoked after a StreamFactory is loaded and all attributes
     * have been set.
     * @since 2.0
     */
    protected void init() { }
    
    /**
     * Returns the class loader to use for resolving classpath resources and bean
     * objects declared in a mapping file.
     * @return the {@link ClassLoader} to use
     * @since 2.0
     */
    protected ClassLoader getClassLoader() {
        return classLoader;
    }
    
    /**
     * Sets the class loader to use for resolving classpath resources and bean objects
     * declared in a mapping files.
     * @param cl the {@link ClassLoader} to use
     * @since 2.0
     */
    protected void setClassLoader(ClassLoader cl) {
        this.classLoader = cl;
    }
}

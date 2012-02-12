/*
 * Copyright 2011 Kevin Seim
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
package org.beanio.spring;

import java.io.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

import org.beanio.*;
import org.beanio.util.*;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.*;
import org.springframework.batch.item.util.*;
import org.springframework.batch.support.transaction.TransactionAwareBufferedWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.*;

/**
 * A Spring Batch item writer that uses a BeanIO stream mapping file to write items 
 * to a flat file.  Restart capabilities are fully supported.
 * 
 * <p>This implementation requires Spring 2.5 or greater, and Spring Batch 2.1.x.</p>
 *  
 * @author Kevin Seim
 * @since 1.2
 * @param <T> Class type written to the file
 */
public class BeanIOFlatFileItemWriter<T> implements ItemStream, ItemWriter<T>, 
    ResourceAwareItemWriterItemStream<T>, InitializingBean {

    private static final String DEFAULT_CHARSET = Charset.defaultCharset().name();
    private static final String DEFAULT_LINE_SEPARATOR = System.getProperty("line.separator");
    
    private static final String RESTART_KEY = "current.count";
    private static final String WRITER_STATE_NAMESPACE = "bw";
    
    private StreamFactory streamFactory;
    private Resource streamMapping;
    private String streamName;
    
    private Resource resource;
    
    private boolean saveState = true;
    private boolean append = false;
    private boolean shouldDeleteIfExists = true;
    private boolean shouldDeleteIfEmpty = false;
    private boolean transactional = true;
    private String encoding = DEFAULT_CHARSET;
    private String lineSeparator = DEFAULT_LINE_SEPARATOR;
    private FlatFileHeaderCallback headerCallback;
    private ExecutionContextUserSupport ecSupport = new ExecutionContextUserSupport();
    
    private Stream stream;

    /**
     * Constructs a new <tt>BeanIOFlatFileItemWriter</tt>.
     */
    public BeanIOFlatFileItemWriter() {
        setName(ClassUtils.getShortName(BeanIOFlatFileItemWriter.class));
    }
    
    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        initializeStreamFactory();
        
        if (append) {
            shouldDeleteIfExists = false;
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.springframework.batch.item.ItemStream#open(org.springframework.batch.item.ExecutionContext)
     */
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (stream != null) {
            return;
        }
        
        Assert.notNull(resource, "The resource must be set");
        
        stream = new Stream();
        stream.open(executionContext);
    }
    
    /*
     * (non-Javadoc)
     * @see org.springframework.batch.item.ItemStream#update(org.springframework.batch.item.ExecutionContext)
     */
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        if (stream == null) {
            throw new ItemStreamException("ItemStream not open or already closed.");
        }
        
        if (saveState) {
            stream.update(executionContext);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.batch.item.ItemStream#close()
     */
    public void close() throws ItemStreamException {
        if (stream != null) {
            stream.close();
            stream = null;
        }
    }
   
    /*
     * (non-Javadoc)
     * @see org.springframework.batch.item.ItemWriter#write(java.util.List)
     */
    public void write(List<? extends T> items) throws Exception {
        if (stream == null) {
            throw new WriterNotOpenException("Writer must be open before it can be written to");
        }
        stream.write(items);
    }

    /**
     * Sets the {@link StreamFactory} for loading stream mapping configurations.  If not set,
     * a new default <tt>StreamFactory</tt> is created.
     * @param streamFactory the <tt>StreamFactory</tt> to use for loading stream
     *   mapping configurations
     */
    public void setStreamFactory(StreamFactory streamFactory) {
        this.streamFactory = streamFactory;
    }

    /**
     * Sets the mapping file resource.  A mapping file is required if a
     * stream factory is not set.
     * @param streamMapping the stream mapping resource
     */
    public void setStreamMapping(Resource streamMapping) {
        this.streamMapping = streamMapping;
    }

    /**
     * Sets the mapping configuration's stream name for writing this output
     * stream.
     * @param streamName the stream name
     */
    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }
    
    /*
     * (non-Javadoc)
     * @see org.springframework.batch.item.file.ResourceAwareItemWriterItemStream#setResource(org.springframework.core.io.Resource)
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }
    
    /**
     * Sets the output stream encoding.  If not set, the default system charset
     * is used to write the output stream.
     * @param encoding output stream encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    
    /**
     * Creates a {@link StreamFactory} if one was not set, loads the stream
     * mapping resource if set, and validates the <tt>StreamFactory</tt> has a
     * mapping for the configured stream name.
     * @throws IllegalStateException if the configured stream name is not found
     *   in the <tt>StreamFactory</tt> after loading the stream mapping
     */
    protected void initializeStreamFactory() throws Exception {
        if (streamFactory == null) {
            streamFactory = StreamFactory.newInstance();
        }
        
        // load the configured stream mapping if the stream name is not already mapped
        if (!streamFactory.isMapped(streamName) && streamMapping != null) {
            InputStream in = streamMapping.getInputStream();
            try {
                streamFactory.load(in);
            }
            finally {
                IOUtil.closeQuietly(in);
            }
        }
        
        if (!streamFactory.isMapped(streamName)) {
            throw new IllegalStateException("No mapping configuration for stream '" + streamName + "'");
        }
    }

    /**
     * Flag to indicate that the target file should be appended if it already
     * exists. If this flag is set then the flag
     * {@link #setShouldDeleteIfExists(boolean) shouldDeleteIfExists} is
     * automatically set to false, so that flag should not be set explicitly.
     * Defaults to false.
     * @param append the flag value to set
     */
    public void setAppendAllowed(boolean append) {
        this.append = append;
        this.shouldDeleteIfExists = false;
    }

    /**
     * Flag to indicate that the target file should be deleted if it already
     * exists, otherwise it will be created. Defaults to true, so no appending
     * except on restart. If set to false and {@link #setAppendAllowed(boolean)
     * appendAllowed} is also false then there will be an exception when the
     * stream is opened to prevent existing data being potentially corrupted.
     * @param shouldDeleteIfExists the flag value to set
     */
    public void setShouldDeleteIfExists(boolean shouldDeleteIfExists) {
        this.shouldDeleteIfExists = shouldDeleteIfExists;
    }
    
    /**
     * Flag to indicate that the target file should be deleted if no lines have
     * been written (other than header and footer) on close. Defaults to false.
     * @param shouldDeleteIfEmpty the flag value to set
     */
    public void setShouldDeleteIfEmpty(boolean shouldDeleteIfEmpty) {
        this.shouldDeleteIfEmpty = shouldDeleteIfEmpty;
    }

    /**
     * Set the flag indicating whether or not state should be saved in the
     * provided {@link ExecutionContext} during the {@link ItemStream} call to
     * update. Setting this to false means that it will always start at the
     * beginning on a restart.
     * @param saveState
     */
    public void setSaveState(boolean saveState) {
        this.saveState = saveState;
    }
    
    /**
     * Flag to indicate that writing to the buffer should be delayed if a
     * transaction is active. Defaults to true.
     * @param transactional set to <tt>false</tt> to disable buffering
     *   writes for transactions
     */
    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

    /**
     * Sets the name to use for prefixing keys added to the execution context.
     * Defaults to the class name.
     * @param name context key prefix
     */
    public void setName(String name) {
        ecSupport.setName(name);
    }
    
    /**
     * Sets the line separator used for the header and footer callback only. Other lines are
     * terminated based on the BeanIO stream mapping configuration.  Defaults to the System property
     * 'line.separator'.
     * @param lineSeparator the line separator to set
     */
    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }
    
    /**
     * The <tt>FlatFileHeaderCallback</tt> if called before writing the first item to the
     * file.  The configured line separator is written immediately following the header.
     * @param headerCallback the {@link FlatFileHeaderCallback} to invoke when writing
     *   to a new file
     */
    public void setHeaderCallback(FlatFileHeaderCallback headerCallback) {
        this.headerCallback = headerCallback;
    }

    /*
     * This inner class stores state information for the last opened stream.  Its implemented
     * as an inner class so that close() and open() can be called without error when a 
     * transaction is in flight.
     */
    private class Stream {
        
        private FileOutputStream outputStream;
        private FileChannel fileChannel;
        private Writer writer; // TransactionAwareBufferedWriter if transactional, BufferedWriter otherwise
        private BeanWriter beanWriter;
        private boolean restarted = false;
        private long lastMarkedByteOffsetPosition = 0L;
        private long itemsWritten = 0L;
        
        /*
         * Opens this stream and restores it state from the given execution context.
         */
        public void open(ExecutionContext executionContext) throws ItemStreamException {
            Assert.notNull(resource, "The resource must be set");
            
            File file;
            try {
                file = resource.getFile();
            }
            catch (IOException e) {
                throw new ItemStreamException("Could not convert resource to file: '" + resource + "'", e);
            }
            
            // determine if we are restarting using the stored execution context
            if (executionContext.containsKey(ecSupport.getKey(RESTART_KEY))) {
                lastMarkedByteOffsetPosition = executionContext.getLong(ecSupport.getKey(RESTART_KEY));
                restarted = true;
            }
            else {
                try {
                    if (!append) {
                        if (file.exists()) {
                            if (!shouldDeleteIfExists) {
                                throw new ItemStreamException("File already exists: " + file.getAbsolutePath());
                            }
                            if (!file.delete()) {
                                throw new IOException("Could not delete file: " + file.getAbsolutePath());
                            }
                        }
                        if (file.getParent() != null) {
                            new File(file.getParent()).mkdirs();
                        }
                        if (!createNewFile(file)) {
                            throw new ItemStreamException("Output file was not created: " + file.getAbsolutePath());
                        }
                    }
                    else {
                        if (!file.exists()) {
                            if (!createNewFile(file)) {
                                throw new ItemStreamException("Output file was not created: " + file.getAbsolutePath());
                            }
                        }
                    }
                }
                catch (IOException ioe) {
                    throw new ItemStreamException("Unable to create file: " + file.getAbsolutePath(), ioe);
                }
            }

            // validate the file is writable if it exists
            if (!file.canWrite()) {
                throw new ItemStreamException("File is not writable: " + file.getAbsolutePath());
            }
            
            boolean appending = false;
            try {
                // open a stream to the file
                outputStream = new FileOutputStream(file.getAbsolutePath(), true);
                fileChannel = outputStream.getChannel();
                writer = createBufferedWriter(fileChannel, encoding);
                writer.flush();
                
                long fileSize = fileChannel.size();
                
                if (append && fileSize > 0) {
                    appending = true;
                }
                
                // in case of restarting reset position to last committed point
                if (restarted) {
                    // validate the file size is at least as big as was previously committed
                    if (fileSize < lastMarkedByteOffsetPosition) {
                        throw new ItemStreamException("Current file size is smaller than size at last commit");
                    }
                    
                    // truncate the file to the last known good position
                    fileChannel.truncate(lastMarkedByteOffsetPosition);
                    fileChannel.position(lastMarkedByteOffsetPosition);
                }

                beanWriter = streamFactory.createWriter(streamName, stream.writer);
                if (restarted && beanWriter instanceof StatefulWriter) {
                    String namespace = ecSupport.getKey(WRITER_STATE_NAMESPACE);
                    
                    Map<String,Object> writerState = new HashMap<String,Object>();
                    for (Map.Entry<String,Object> entry : executionContext.entrySet()) {
                        if (entry.getKey().startsWith(namespace)) {
                            writerState.put(entry.getKey(), entry.getValue());
                        }
                    }
                    
                    ((StatefulWriter)beanWriter).restoreState(namespace, writerState);
                }
                
                itemsWritten = 0;
            }
            catch (IOException ioe) {
                throw new ItemStreamException("Failed to initialize writer", ioe);
            }
            
            if (lastMarkedByteOffsetPosition == 0 && !appending) {
                if (headerCallback != null) {
                    try {
                        headerCallback.writeHeader(writer);
                        writer.write(lineSeparator);
                    }
                    catch (IOException e) {
                        throw new ItemStreamException("Could not write headers.  The file may be corrupt.", e);
                    }
                }
            }
        }
        
        /*
         * Updates the given execution context.
         */
        public void update(ExecutionContext executionContext) throws ItemStreamException {
            Assert.notNull(executionContext, "ExecutionContext must not be null");
            try {
                long pos = 0;

                if (fileChannel != null) {
                    // flushing the bean writer will not flush the TransactionAwareBufferWRiter if
                    // a transaction is active
                    beanWriter.flush();
                    
                    // get the flushed file size
                    pos = fileChannel.position();
                    
                    // add in the bufferred transaction
                    if (transactional) {
                        pos += ((TransactionAwareBufferedWriter) writer).getBufferSize();
                    }
                }
                
                executionContext.putLong(ecSupport.getKey(RESTART_KEY), pos);
                
                // if the bean writer is stateful, gather its state information and add
                // it to the execution context
                if (beanWriter instanceof StatefulWriter) {
                    String namespace = ecSupport.getKey(WRITER_STATE_NAMESPACE);
                    
                    Map<String,Object> writerState = new HashMap<String,Object>();     
                    ((StatefulWriter)beanWriter).updateState(namespace, writerState);
                    
                    for (Map.Entry<String,Object> entry : writerState.entrySet()) {
                        executionContext.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            catch (IOException e) {
                throw new ItemStreamException("ItemStream does not return current position properly", e);
            }
        }
        
        /*
         * Writes a list of items to the stream.
         */
        public void write(List<? extends T> items) throws Exception {
            for (T item : items) {
                try {
                    beanWriter.write(item);
                    ++itemsWritten;
                }
                catch (BeanWriterException ex) {
                    throw new WriteFailedException("Writer failed: " + ex.toString(), ex);
                }
            }
            beanWriter.flush();
        }
        
        /*
         * Closes this stream.
         */
        public void close() throws ItemStreamException {
            try {
                beanWriter.close();
            }
            catch (BeanWriterIOException ex) {
                throw new ItemStreamException("Unable to close the ItemWriter", ex);
            }
            finally {
                if (!transactional) {
                    destroy();
                }
            }
        }
        
        /*
         * Releases all resources. 
         */
        private void destroy() {
            try {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
                catch (IOException ioe) {
                    throw new ItemStreamException("Unable to close the the ItemWriter", ioe);
                }
                
                if (!restarted && itemsWritten == 0 && shouldDeleteIfEmpty) {
                    try {
                        resource.getFile().delete();
                    }
                    catch (IOException ex) {
                        throw new ItemStreamException("Failed to delete empty file on close", ex);
                    }
                }
            }
            finally {
                beanWriter = null;
                writer = null;
                fileChannel = null;
                outputStream = null;
            }
        }
        
        /**
         * Creates a new empty file.
         * @return <tt>true</tt> if the file was successfully created
         */
        private boolean createNewFile(File file) throws IOException {
            try {
                return file.createNewFile() && file.exists();
            }
            catch (IOException e) {
                // Per spring-batch code, on some filesystems you can get an exception 
                // here even though the file was successfully created
                if (file.exists()) {
                    return true;
                }
                else {
                    throw e;
                }
            }
        }
        
        /**
         * Creates a buffered writer opened to the beginning of the file
         * for the given file channel.
         */
        private Writer createBufferedWriter(FileChannel fileChannel, String encoding) {
            try {
                Writer writer = Channels.newWriter(fileChannel, encoding);
                
                if (transactional) {
                    writer = new TransactionAwareBufferedWriter(writer, new Runnable() { 
                        public void run() { 
                            Stream.this.destroy();
                        }
                    });
                }
                else {
                    writer = new BufferedWriter(writer);
                }
                
                return writer;
            }
            catch (UnsupportedCharsetException ex) {
                throw new ItemStreamException("Bad encoding configuration for output file " + fileChannel, ex);
            }
        }
    }
}

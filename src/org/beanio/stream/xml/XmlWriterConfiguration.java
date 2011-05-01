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
package org.beanio.stream.xml;

/**
 * 
 * @author Kevin Seim
 * @since 1.1
 */
public class XmlWriterConfiguration implements Cloneable {

    private int indentation = -1;
    private String lineSeparator = null;
    
    private String xsiPrefix = "xsi";
    private boolean xsiDeclared = false;
    
    private boolean headerEnabled = false;
    private String version = "1.0";
    private String encoding = "utf-8";
    
    /**
     * Constructs a new <tt>XmlWriterConfiguration</tt>.
     */
    public XmlWriterConfiguration() { }

    /**
     * Returns the number of spaces to indent each level of XML, or <tt>-1</tt>
     * if indentation is disabled.
     * @return the number of spaces to indent each level of XML, 
     *   or <tt>-1</tt> to disable indentation
     */
    public int getIndentation() {
        return indentation;
    }

    /**
     * Enables and sets the indentation level in spaces.  If set to <tt>-1</tt>
     * (the default value), indentation is disabled.
     * @param indentation the number of spaces to indent each level of XML, 
     *   or <tt>-1</tt> to disable indentation
     */
    public void setIndentation(int indentation) {
        this.indentation = indentation;
    }
    
    /**
     * Returns whether XML output will be indented.
     * @return <tt>true</tt> if indentation is enabled
     */
    public boolean isIndentationEnabled() {
        return indentation >= 0;
    }

    /**
     * Returns the text used to terminate a line when indentation is enabled. 
     * When set to <tt>null</tt> (the default), the line separator is set to the 
     * value of the <tt>line.separator</tt> system property.
     * @return the line separation text
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * Sets the text used to terminate a line when indentation is enabled.  
     * When set to <tt>null</tt> (the default), the line separator is set to the 
     * value of the <tt>line.separator</tt> system property.
     * @param lineSeparator the line separation text
     */
    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    public String getXsiPrefix() {
        return xsiPrefix;
    }

    public void setXsiPrefix(String xsiPrefix) {
        this.xsiPrefix = xsiPrefix;
    }

    public boolean isXsiDeclared() {
        return xsiDeclared;
    }

    public void setXsiDeclared(boolean xsiDeclared) {
        this.xsiDeclared = xsiDeclared;
    }

    public boolean isHeaderEnabled() {
        return headerEnabled;
    }

    public void setHeaderEnabled(boolean headerEnabled) {
        this.headerEnabled = headerEnabled;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    
    @Override
    protected XmlWriterConfiguration clone() {
        try {
            return (XmlWriterConfiguration) super.clone();
        }
        catch (CloneNotSupportedException ex) {
            throw new IllegalStateException();
        }
    }
}

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
package org.beanio.util;

import java.io.*;

/**
 * Utility class for manipulating streams.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class IOUtil {

    private IOUtil() { }

    /**
     * Closes an input stream and quietly ignores any exception.
     * @param in the stream to close
     */
    public static void closeQuietly(Reader in) {
        try {
            if (in != null)
                in.close();
        }
        catch (IOException ex) { }
    }

    /**
     * Closes an output stream and quietly ignores any exception.
     * @param out the stream to close
     */
    public static void closeQuietly(Writer out) {
        try {
            if (out != null)
                out.close();
        }
        catch (IOException ex) { }
    }

    /**
     * Closes an input stream and quietly ignores any exception.
     * @param in the stream to close
     */
    public static void closeQuietly(InputStream in) {
        try {
            if (in != null)
                in.close();
        }
        catch (IOException ex) { }
    }

    /**
     * Closes an output stream and quietly ignores any exception.
     * @param out the stream to close
     */
    public static void closeQuietly(OutputStream out) {
        try {
            if (out != null)
                out.close();
        }
        catch (IOException ex) { }
    }
}

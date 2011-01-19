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

/**
 * Utility class for working with Java types.
 * @author Kevin Seim
 * @since 1.0
 */
public class TypeUtil {

    /**
     * Cannot instantiate.
     */
    private TypeUtil() { }
    
    /**
     * Returns <tt>true</tt> if <tt>to.isAssignableFrom(from)</tt> after converting
     * primitive values of <tt>to</tt> to its object counterpart.
     * @param to the class or primitive to test assignability to
     * @param from the class to test assignability from
     * @return <tt>true</tt> if <tt>to</tt> is assignable from <tt>from</tt>
     */
    public static boolean isAssignable(Class<?> to, Class<?> from) {
        Class<?> type = to;
        if (to.isPrimitive()) {
            if (int.class.equals(to))
                type = Integer.class;
            else if (double.class.equals(to))
                type = Double.class;
            else if (char.class.equals(to))
                type = Character.class;
            else if (boolean.class.equals(to))
                type = Boolean.class;
            else if (long.class.equals(to))
                type = Long.class;
            else if (float.class.equals(to))
                type = Float.class;
            else if (short.class.equals(to))
                type = Short.class;
            else if (byte.class.equals(to))
                type = Byte.class;
        }
        return type.isAssignableFrom(from);
    }
}

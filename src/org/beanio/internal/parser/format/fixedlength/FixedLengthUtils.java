/*
 * Copyright 2011-2013 Kevin Seim
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
package org.beanio.internal.parser.format.fixedlength;

import org.beanio.BeanIOException;
import org.beanio.internal.util.Settings;

import java.nio.charset.Charset;

import static org.beanio.internal.util.Settings.FIXED_LENGTH_CHARSET;
import static org.beanio.internal.util.Settings.FIXED_LENGTH_COUNT_MODE;

public class FixedLengthUtils {

    public static int calculateByteLength(String text) {
        String charset = Settings.getInstance().getProperty(FIXED_LENGTH_CHARSET);
        return text.getBytes(Charset.forName(charset)).length;
    }

    public static int calculateTextLength(String text) {
        String mode = Settings.getInstance().getProperty(FIXED_LENGTH_COUNT_MODE);
        switch (mode) {
            case "chars":
                return text.length();
            case "bytes":
                return calculateByteLength(text);
            default:
                throw new BeanIOException("Unsupported value: " + mode + " for countMode");
        }
    }

}
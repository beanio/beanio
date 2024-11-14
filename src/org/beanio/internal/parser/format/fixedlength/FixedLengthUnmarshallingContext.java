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
import org.beanio.internal.parser.TextLengthCounter;
import org.beanio.internal.parser.UnmarshallingContext;
import org.beanio.internal.util.Settings;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.beanio.internal.util.Settings.FIXED_LENGTH_CHARSET;
import static org.beanio.internal.util.Settings.FIXED_LENGTH_COUNT_MODE;

/**
 * The {@link UnmarshallingContext} implementation for a fixed length formatted stream.
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class FixedLengthUnmarshallingContext extends UnmarshallingContext {

    private String record;
    private int recordLength;

    @Override
    public void setRecordValue(Object value) {
        this.record = (String) value;
        this.recordLength = value == null ? 0 : FixedLengthUtils.calculateTextLength(record);
    }
    
    /**
     * Returns the length of the record being unmarshalled.
     * @return the record length
     */
    public int getRecordLength() {
        return recordLength;
    }

    @Override
    public TextLengthCounter getTextLengthCounter() {
        return new FixedLengthTextLengthCounter();
    }

    /**
     * Returns the field text at the given position in the record.
     * @param name the field name
     * @param position the position of the field in the record
     * @param length the field length, or -1 if the field is at the end of the
     *   record and unbounded
     * @param until the maximum position of the field as an offset
     *   of the field count, for example -2 to indicate the any position
     *   except the last two fields in the record
     * @return the field text, or null if the record length is less than
     *   the position of the field
     */
    public String getFieldText(String name, int position, int length, int until) {
        int max = recordLength + until;
        
        if (position < 0) {
            position = recordLength + position;
            
            position = getAdjustedFieldPosition(position);
            if (position < 0) {
                return null;
            }
        }
        else {
            position = getAdjustedFieldPosition(position);
            if (position >= max) {
                return null;
            }
        }
        
        String text;
        if (length < 0) {
            text = record.substring(position, max);
        }
        else {
            String mode = Settings.getInstance().getProperty(FIXED_LENGTH_COUNT_MODE);
            if (mode.equals("chars")) {
                text = record.substring(position, Math.min(max, position + length));
            }
            else if (mode.equals("bytes")) {
                try {
                    String charset = Settings.getInstance().getProperty(FIXED_LENGTH_CHARSET);
                    byte[] b = Arrays.copyOfRange(record.getBytes(charset), position, Math.min(max, position + length));
                    text = new String(b, Charset.forName(charset));
                }
                catch (UnsupportedEncodingException ex) {
                    throw new BeanIOException("Unsupported encoding: " + mode + " for countMode");
                }
            }
            else {
                throw new BeanIOException("Unsupported value: " + mode + " for countMode");
            }
        }
        setFieldText(name, text);
        return text;
    }
}

/*
 * Copyright 2011-2012 Kevin Seim
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
package org.beanio.parser.xml.validation;

import org.beanio.BeanIOConfigurationException;
import org.beanio.BeanIOConfigurationRepeatingNoneException;
import org.beanio.parser.ParserTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * JUnit test cases for collection type fields.
 * 
 * @author Cornelis Hoeflake
 * @since 3.0
 */
public class XmlRepeatingNoneValidationTest extends ParserTest {

    @Test
    public void testCollectionShouldNotRunInfinite() throws Exception{
        executeTest("segment");
    }


    @Test
    public void testCollectionShouldNotRunInfiniteField() throws Exception {
        executeTest("field");
    }

    @Test
    public void testCollectionShouldNotRunInfiniteGroup() throws Exception {
        executeTest("group");
    }

    private void executeTest(String elementName) {
        BeanIOConfigurationException beanIOConfigurationException = Assertions.assertThrows(BeanIOConfigurationException.class, () ->
                createStreamFactory(elementName)
        );
        beanIOConfigurationException.printStackTrace();
        Assert.assertTrue("A BeanIOConfigurationException was thrown, but we excpected a BeanIOConfigurationRepeatingNoneException as cause.", BeanIOConfigurationRepeatingNoneException.class.isAssignableFrom(beanIOConfigurationException.getCause().getClass()));
    }

    private void createStreamFactory(String part) {
        try {
            newStreamFactory("repeating-none-" + part + ".xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

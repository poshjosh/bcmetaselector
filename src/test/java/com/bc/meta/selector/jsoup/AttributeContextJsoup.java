/*
 * Copyright 2018 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bc.meta.selector.jsoup;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import org.jsoup.nodes.Element;
import com.bc.meta.selector.AttributeTestProvider;
/**
 * @author Chinomso Bassey Ikwuagwu on Jun 16, 2018 5:36:58 PM
 */
public class AttributeContextJsoup implements AttributeTestProvider<Element>, BiFunction<String, Element, String> {
    
    private final boolean useCache;
    
    private final Map<String, Predicate<Element>> testCache;

    public AttributeContextJsoup(boolean useCache) {
        this.useCache = useCache;
        this.testCache = !useCache ? Collections.EMPTY_MAP : 
                Collections.synchronizedMap(new WeakHashMap<>());
    }

    @Override
    public Predicate<Element> getAttributeTest(String attributeName, String attributeValue) {
        return this.getAttributeTest(attributeName, attributeValue, false);
    }

    @Override
    public Predicate<Element> getAttributeRegexTest(String attributeName, String attributeValue) {
        return this.getAttributeTest(attributeName, attributeValue, true);
    }

    public Predicate<Element> getAttributeTest(String attributeName, String attributeValue, boolean regex) {
        Predicate<Element> test;
        final String key = !useCache ? "" : this.buildKey(attributeName, attributeValue);
        synchronized(testCache) {
            test = testCache.get(key);
            if(test == null) {
                test = this.createAttributeTest(attributeName, attributeValue, regex);
                if(useCache) {
                    testCache.put(key, test);
                }
            }
        }
        return test;
    }

    public Predicate<Element> createAttributeTest(String attributeName, String attributeValue, boolean regex) {
        final Predicate<Element> test = !regex ? 
                (node) -> Objects.equals(node.attr(attributeName), attributeValue) :
                new com.bc.meta.selector.jsoup.HasAttributeRegexFilter(attributeName, attributeValue);
        return test;
    }

    @Override
    public String apply(String propertyName, Element node) {
        
        final String result = node == null ? null : node.attr("content");
        
        return result;
    }
    
    public String buildKey(String attributeName, String attributeValue) {
        return attributeName + '=' + attributeValue;
    }
}

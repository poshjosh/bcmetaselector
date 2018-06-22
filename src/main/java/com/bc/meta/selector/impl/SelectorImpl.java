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

package com.bc.meta.selector.impl;

import com.bc.meta.Metadata;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bc.meta.selector.Selector;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 18, 2018 8:46:31 PM
 */
public class SelectorImpl<NODE> implements Serializable, Selector<NODE> {

    private transient static final Logger LOG = Logger.getLogger(SelectorImpl.class.getName());
    
    public static class CollectIntoMetadata<NODE> extends CollectingConsumer<NODE, String, Metadata> {
        public CollectIntoMetadata(BiFunction<String, NODE, String> nodeConverter, Predicate<String> multipleValueTest) {
            super(nodeConverter, multipleValueTest);
        }
        @Override
        public Metadata flush(Map<String, Object> m) {
            final Map<String, Object> mCopy = new LinkedHashMap(m);
            return new Metadata() {
                @Override
                public String getValue(String name, String outputIfNone) {
                    return (String)mCopy.getOrDefault(name, outputIfNone);
                }
                @Override
                public Set<String> getValues(String name) {
                    return (Set<String>)mCopy.get(name);
                }
            };
        }
    }
    
    public static class CollectIntoMap<NODE> extends CollectingConsumer<NODE, Object, Map<String, Object>> {
        public CollectIntoMap(Predicate<String> multipleValueTest) {
            super((prop, node) -> node, multipleValueTest);
        }
        public CollectIntoMap(BiFunction<String, NODE, Object> nodeConverter, Predicate<String> multipleValueTest) {
            super(nodeConverter, multipleValueTest);
        }
        @Override
        public Map<String, Object> flush(Map<String, Object> m) {
            return Collections.unmodifiableMap(new LinkedHashMap(m));
        }
    }

    public static abstract class CollectingConsumer<NODE, NODEVALUE, RESULT> 
            extends LinkedHashMap<String, Object>
            implements Consumer<String, NODE, RESULT> {
        private final BiFunction<String, NODE, NODEVALUE> nodeConverter;
        private final Predicate<String> multipleValueTest;
        private int valueCount = 0;
        public CollectingConsumer(BiFunction<String, NODE, NODEVALUE> nodeConverter, Predicate<String> multipleValueTest) {
            this.nodeConverter = Objects.requireNonNull(nodeConverter);
            this.multipleValueTest = Objects.requireNonNull(multipleValueTest);
        }
        public abstract RESULT flush(Map<String, Object> m);
        @Override
        public boolean consume(String propertyName, NODE node) {
            final Object cached = this.get(propertyName);
            final NODEVALUE value = this.nodeConverter.apply(propertyName, node);
            if(cached == null) {
                if(this.isMultipleValues(propertyName)) {
                    final Set<NODEVALUE> valueSet = new LinkedHashSet<>();
                    valueSet.add(value);
                    this.put(propertyName, valueSet);
                    LOG.finer(() -> "Added to a new Set: " + propertyName + '=' + value);
                }else{
                    this.put(propertyName, value);
                    LOG.finer(() -> "Added: " + propertyName + '=' + value);
                }
                
                ++valueCount;
                
            }else{
                if(this.isMultipleValues(propertyName)) {
                    final Collection<NODEVALUE> valueSet = ((Collection<NODEVALUE>)cached);
                    valueSet.add(value);
                    LOG.finer(() -> "Added to a Set of " + (valueSet.size() - 1) + ": " + propertyName + '=' + value);
                    
                    ++valueCount;
                    
                }else{
                    LOG.warning(() -> "Found multiple values for a non multiple-value parameter: " + 
                            propertyName + ", while extracting meta-nodes data");
                    return false;
                }
            }
            return true;
        }
        @Override
        public RESULT flush() {
            final RESULT output = this.flush(this);
            this.clear();
            this.valueCount = 0;
            return output;
        }
        @Override
        public int getNameCount() {
            return this.size();
        }
        @Override
        public int getValueCount() {
            return this.valueCount;
        }
        @Override
        public boolean isConsumed(String name) {
            return this.containsKey(name);
        }
        @Override
        public boolean isMultipleValues(String name) {
            return this.multipleValueTest.test(name);
        }
    }
    
    private final Map<String, Predicate<NODE>> nodeTests;
    
    private final Consumer<String, NODE, Metadata> collectIntoMetadata;
    
    private final Consumer<String, NODE, Map> collectIntoMap;

    public SelectorImpl(
            Map<String, Predicate<NODE>> nodeTests, 
            BiFunction<String, NODE, Object> nodeConverter, 
            Predicate<String> multipleValueTest) {
        this.nodeTests = Objects.requireNonNull(nodeTests);
        this.collectIntoMetadata = new SelectorImpl.CollectIntoMetadata(nodeConverter, multipleValueTest);
        this.collectIntoMap = new SelectorImpl.CollectIntoMap(nodeConverter, multipleValueTest);
    }

    @Override
    public Metadata selectAsMetadata(Iterator<NODE> nodeIterator, Collection<String> names) {
        return this.select(nodeIterator, this.collectIntoMetadata, names);
    }
    
    @Override
    public Map selectAsMap(Iterator<NODE> nodeIterator, Collection<String> names) {
        return this.select(nodeIterator, this.collectIntoMap, names);
    }

    @Override
    public <RESULT> RESULT select(Iterator<NODE> nodeIterator, Consumer<String, NODE, RESULT> consumer, Collection<String> names) {
     
        while(nodeIterator.hasNext()) {

            final NODE node = nodeIterator.next();
            
            for(String name : names) {

                if(!consumer.isMultipleValues(name) && consumer.isConsumed(name)) {
                    continue;
                }
                
                final Predicate<NODE> predicate = nodeTests.get(name);
                
                if(predicate.test(node)) {

                    if(LOG.isLoggable(Level.FINER)) {
                        LOG.log(Level.FINER, "{0} = {1}", new Object[]{name, node});
                    }
                    
                    consumer.consume(name, node);
                    
                    break;
                }
            }
        }

        LOG.fine(() -> "Done extracting " + consumer.getNameCount() + ':' + consumer.getValueCount() + " values.");
        
        return this.flush(consumer);
    }
    
    public <RESULT> RESULT flush(Consumer<String, NODE, RESULT> consumer) {
        return consumer.flush();
    }
}

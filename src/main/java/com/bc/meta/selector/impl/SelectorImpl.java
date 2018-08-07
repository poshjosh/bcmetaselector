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

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bc.meta.selector.Selector;
import java.util.function.Function;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 18, 2018 8:46:31 PM
 */
public class SelectorImpl<NODE, NODEVALUE> implements Serializable, Selector<NODE> {

    private transient static final Logger LOG = Logger.getLogger(SelectorImpl.class.getName());
    
    private final Function<String, Predicate<NODE>> nodeTestProvider;
    
    private final BiFunction<String, NODE, NODEVALUE> nodeValueExtractor;

    private final Predicate<String> multiValueTest;

    public SelectorImpl(
            Function<String, Predicate<NODE>> nodeTestProvider, 
            BiFunction<String, NODE, NODEVALUE> nodeValueExtractor, 
            Predicate<String> multiValueTest) {
        this.nodeTestProvider = Objects.requireNonNull(nodeTestProvider);
        this.nodeValueExtractor = Objects.requireNonNull(nodeValueExtractor);
        this.multiValueTest = Objects.requireNonNull(multiValueTest);
    }

    @Override
    public <RESULT> RESULT select(Iterator<NODE> nodeIterator, Collection<String> names, Collector<RESULT> collector) {
     
        int consumed = 0;
        
        while(nodeIterator.hasNext()) {

            final NODE node = nodeIterator.next();
            
            for(String name : names) {

                if(!this.multiValueTest.test(name) && this.isSelected(collector, name)) {
                    continue;
                }
                
                final Predicate<NODE> predicate = nodeTestProvider.apply(name);
                
                if(predicate.test(node)) {

                    if(LOG.isLoggable(Level.FINER)) {
                        LOG.log(Level.FINER, "{0} = {1}", new Object[]{name, node});
                    }
                    
                    this.select(collector, name, node);
                    
                    ++consumed;
                    
                    break;
                }
            }
        }

        LOG.log(Level.FINER, "Done selecting {0} nodes.", consumed);
        
        return this.flush(collector);
    }

    public boolean isSelected(Collector collector, String propertyName) {
        final Object value = collector.getOrDefault(propertyName, null);
        if(value instanceof Collection) {
            return !((Collection)value).isEmpty();
        }else{
            return value != null;
        }
    }

    public boolean select(Collector collector, String propertyName, NODE node) {
        
        final Object cached = collector.getOrDefault(propertyName, null);
        
        final NODEVALUE value = this.nodeValueExtractor.apply(propertyName, node);
        
        if(cached == null) {
            
            if(this.multiValueTest.test(propertyName)) {
                final Set<NODEVALUE> valueSet = new LinkedHashSet<>();
                valueSet.add(value);
                collector.put(propertyName, valueSet);
                LOG.finer(() -> "Added to a new Set: " + propertyName + '=' + value);
            }else{
                collector.put(propertyName, value);
                LOG.finer(() -> "Added: " + propertyName + '=' + value);
            }
        }else{
            
            if(this.multiValueTest.test(propertyName)) {
                final Collection<NODEVALUE> valueSet = ((Collection<NODEVALUE>)cached);
                valueSet.add(value);
                LOG.finer(() -> "Added to a Set of " + (valueSet.size() - 1) + ": " + propertyName + '=' + value);
            }else{
                LOG.warning(() -> "Found multiple values for a non multi-value parameter: " + 
                        propertyName + ", while extracting meta-nodes data");
                return false;
            }
        }
        
        return true;
    }
    
    public <RESULT> RESULT flush(Collector<RESULT> collector) {
        final RESULT output = collector.buildResult();
        return output;
    }
}

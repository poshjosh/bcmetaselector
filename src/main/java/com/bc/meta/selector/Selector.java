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

package com.bc.meta.selector;

import com.bc.meta.Metadata;
import com.bc.meta.selector.impl.SelectorBuilderImpl;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 21, 2018 12:06:53 PM
 */
public interface Selector<NODE> {
    
    public static SelectorBuilder builder() {
        return new SelectorBuilderImpl<>();
    }
    
    public static interface Consumer<K, V, RESULT> {
        boolean consume(K name, V value);
        RESULT flush();
        int getNameCount();
        int getValueCount();
        boolean isConsumed(K name);
        boolean isMultipleValues(K name);
    }
    
    default <RESULT> RESULT select(Iterator<NODE> nodeIterator, Consumer<String, NODE, RESULT> consumer, String[] names) {
        return this.select(nodeIterator, consumer, Arrays.asList(names));
    }

    <RESULT> RESULT select(Iterator<NODE> nodeIterator, Consumer<String, NODE, RESULT> consumer, Collection<String> names);

    default Map selectAsMap(Iterator<NODE> nodeIterator, String[] names) {
        if(names.length == 0) {
            return Collections.EMPTY_MAP;
        }else{
            return this.selectAsMap(nodeIterator, Arrays.asList(names));
        }
    }
    
    Map selectAsMap(Iterator<NODE> nodeIterator, Collection<String> names);

    default Metadata selectAsMetadata(Iterator<NODE> nodeIterator, String[] names) {
        if(names.length == 0) {
            return Metadata.EMPTY;
        }else{
            return this.selectAsMetadata(nodeIterator, Arrays.asList(names));
        }
    }
    
    Metadata selectAsMetadata(Iterator<NODE> nodeIterator, Collection<String> names);
}

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

import com.bc.meta.selector.impl.SelectorBuilderImpl;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 21, 2018 12:06:53 PM
 */
public interface Selector<NODE> {
    
    public static SelectorBuilder builder() {
        return new SelectorBuilderImpl<>();
    }
    
    public static interface Collector<RESULT> {
        public RESULT buildResult();
        public Object getOrDefault(String propertyName, Object valueIfNone);
        public void put(String propertyName, Object value);
    }
    
    default <RESULT> RESULT select(Iterator<NODE> nodeIterator, String[] names, Collector<RESULT> collector) {
        return this.select(nodeIterator, Arrays.asList(names), collector);
    }

    <RESULT> RESULT select(Iterator<NODE> nodeIterator, Collection<String> names, Collector<RESULT> collector);
}

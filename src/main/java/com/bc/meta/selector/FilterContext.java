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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 15, 2018 9:33:21 PM
 */
public interface FilterContext<NODE> {
    
    FilterContext EMPTY = new FilterContext() {
        @Override
        public Predicate and(String name, Predicate outputIfNone) { return outputIfNone; }
        @Override
        public Predicate or(String name, Predicate outputIfNone) { return outputIfNone; }
        @Override
        public FilterContext and(FilterContext other) { return this; }
        @Override
        public FilterContext or(FilterContext other) { return other; }
        @Override
        public List getAll(String name) { return Collections.EMPTY_LIST; }
        @Override
        public Stream stream(String name, Predicate resultIfNone) { return Stream.empty(); }
        @Override
        public Iterator iterator(String name, Predicate resultIfNone) { return this.getAll(name).iterator(); }
        @Override
        public boolean anyMatch(String name, Predicate predicateIfNone, Object candidate) { return false; }
        @Override
        public boolean allMatch(String name, Predicate predicateIfNone, Object candidate) { return false; }
        @Override
        public List selectMatchingAllFilters(String name, Predicate predicateIfNone, Iterator candidates, int limit) { return Collections.EMPTY_LIST; }
        @Override
        public List selectMatchingAnyFilter(String name, Predicate predicateIfNone, Iterator candidates, int limit) { return Collections.EMPTY_LIST; }
        @Override
        public int getCount(String name) { return 0; }
        @Override
        public Predicate get(String name, int i, Predicate outputIfNone) { return outputIfNone; }
    };
    
    Predicate<NODE> and(String name, Predicate<NODE> outputIfNone);

    Predicate<NODE> or(String name, Predicate<NODE> outputIfNone);
            
    FilterContext<NODE> and(FilterContext<NODE> other);
    
    FilterContext<NODE> or(FilterContext<NODE> other);
    
    List<Predicate<NODE>> getAll(String name);
    
    Stream<Predicate<NODE>> stream(String name, Predicate<NODE> resultIfNone);
    
    Iterator<Predicate<NODE>> iterator(String name, Predicate<NODE> resultIfNone);
    
    boolean anyMatch(String name, Predicate<NODE> predicateIfNone, NODE candidate);

    boolean allMatch(String name, Predicate<NODE> predicateIfNone, NODE candidate);
    
    default NODE findFirstMatchingAllFilters(String name, Predicate<NODE> predicateIfNone, Iterator<NODE> candidates, NODE outputIfNone) {
        final List<NODE> found = this.selectMatchingAllFilters(name, predicateIfNone, candidates, 1);
        final NODE result = found.isEmpty() ? null : found.get(0);
        return result == null ? outputIfNone : result;
    }
    
    List<NODE> selectMatchingAllFilters(String name, Predicate<NODE> predicateIfNone, Iterator<NODE> candidates, int limit);

    default NODE findFirstMatchingAnyFilter(String name, Predicate<NODE> predicateIfNone, Iterator<NODE> candidates, NODE outputIfNone) {
        final List<NODE> found = this.selectMatchingAnyFilter(name, predicateIfNone, candidates, 1);
        final NODE result = found.isEmpty() ? null : found.get(0);
        return result == null ? outputIfNone : result;
    }

    List<NODE> selectMatchingAnyFilter(String name, Predicate<NODE> predicateIfNone, Iterator<NODE> candidates, int limit);
    
    int getCount(String name);
    
    Predicate<NODE> get(String name, int i, Predicate<NODE> outputIfNone);
}

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

import com.bc.meta.selector.FilterContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 17, 2018 2:00:34 AM
 */
public abstract class AbstractFilterContext<NODE> implements FilterContext<NODE> {

    private transient static final Logger LOG = Logger.getLogger(AbstractFilterContext.class.getName());

    @Override
    public Predicate<NODE> and(String name, Predicate<NODE> outputIfNone) {
        return this.compose(name, true, outputIfNone);
    }

    @Override
    public Predicate<NODE> or(String name, Predicate<NODE> outputIfNone) {
        return this.compose(name, false, outputIfNone);
    }
    
    @Override
    public FilterContext<NODE> and(FilterContext<NODE> other) {
        return new FilterContextPair(this, other, true);
    }
    
    @Override
    public FilterContext<NODE> or(FilterContext<NODE> other) {
        return new FilterContextPair(this, other, false);
    }
    
    public Predicate<NODE> compose(String name, boolean and, Predicate<NODE> outputIfNone) {
        final int count = this.getCount(name);
        Predicate<NODE> result = null;
        for(int i=0; i<count; i++) {
            final Predicate<NODE> test = this.get(name, i, null);
            if(result == null) {
                result = test;
            }else{
                result = and ? result.and(test) : result.or(test);
            }
        }
        return result == null ? outputIfNone : result;
    }
    
    @Override
    public List<Predicate<NODE>> getAll(String name) {
        final int count = this.getCount(name);
        List<Predicate<NODE>> result = null;
        for(int i=0; i<count; i++) {
            final Predicate<NODE> test = this.get(name, i, null);
            if(test != null) {
                if(result == null) {
                    result = new ArrayList(count);
                }
                result.add(test);
            }
        }
        return result == null || result.isEmpty() ? Collections.EMPTY_LIST : 
                result.size() ==  1 ? Collections.singletonList(result.get(0)) : 
                Collections.unmodifiableList(result);
    }
    
    @Override
    public Stream<Predicate<NODE>> stream(String name, Predicate<NODE> resultIfNone) {
        final Iterator<Predicate<NODE>> iter = this.iterator(name, resultIfNone);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED), false);        
    }
    
    @Override
    public Iterator<Predicate<NODE>> iterator(String name, Predicate<NODE> resultIfNone) {
        return new Iter<>(this, name, resultIfNone);
    }
    
    @Override
    public boolean anyMatch(String name, Predicate<NODE> predicateIfNone, NODE candidate) {
        final int size = this.getCount(name);
        boolean result = false;
        for(int i=0; i<size; i++) {
            final Predicate<NODE> test = this.get(name, i, predicateIfNone);
            if(test != null && test.test(candidate)) {
                result = true;
                break;
            }
        }
//        System.out.println("AbstractFilterContext-" + LocalDateTime.now() +  ". " + "Result: " + result + ", name: " + name + ", node: " + candidate);
        if(LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "Result: {0}, name: {1}, node: {2}", new Object[]{result, name, candidate});
        }
        return result;
    }

    @Override
    public boolean allMatch(String name, Predicate<NODE> predicateIfNone, NODE candidate) {
        final int size = this.getCount(name);
        for(int i=0; i<size; i++) {
            final Predicate<NODE> test = this.get(name, i, predicateIfNone);
            if(test == null || !test.test(candidate)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public List<NODE> selectMatchingAllFilters(String name, Predicate<NODE> predicateIfNone, Iterator<NODE> candidates, int limit) {
        return this.selectMatching(name, predicateIfNone, candidates, limit, true);
    }

    @Override
    public List<NODE> selectMatchingAnyFilter(String name, Predicate<NODE> predicateIfNone, Iterator<NODE> candidates, int limit) {
        return this.selectMatching(name, predicateIfNone, candidates, limit, false);
    }

    public List<NODE> selectMatching(String name, Predicate<NODE> predicateIfNone, Iterator<NODE> candidates, int limit, boolean all) {
        List<NODE> result = null;
        int count = 0;
        while(candidates.hasNext() && count < limit) {
            final NODE candidate = candidates.next();
            final boolean matches = all ? this.allMatch(name, predicateIfNone, candidate) :
                    this.anyMatch(name, predicateIfNone, candidate);
            if(matches) {
                if(result == null) {
                    result = new ArrayList(limit);
                }
                result.add(candidate);
                count = result.size();
            }
        }
        return result == null || result.isEmpty() ? Collections.EMPTY_LIST :
                result.size() == 1 ? Collections.singletonList(result.get(0)) :
                Collections.unmodifiableList(result);
    }
    
    private static final class FilterContextPair<E> extends AbstractFilterContext<E> {
        private final FilterContext<E> left;
        private final FilterContext<E> right;
        private final boolean and;
        public FilterContextPair(FilterContext<E> left, FilterContext<E> right, boolean and) {
            this.left = Objects.requireNonNull(left);
            this.right = Objects.requireNonNull(right);
            this.and = and;
        }
        @Override
        public int getCount(String name) {
            return Math.max(left.getCount(name), right.getCount(name));
        }
        @Override
        public Predicate<E> get(String name, int i, Predicate<E> outputIfNone) {
            final Predicate<E> lhs = i >= left.getCount(name) ? null : left.get(name, i, null);
            final Predicate<E> rhs = i >= right.getCount(name) ? null : right.get(name, i, null);
            final Predicate<E> result = 
                    lhs == null ? rhs : 
                    rhs == null ? lhs : 
                    and ? lhs.and(rhs) : lhs.or(rhs);
            return result == null ? outputIfNone : result;
        }
    }

    private static final class Iter<T> implements Iterator<Predicate<T>>, Serializable {
        private final FilterContext<T> ctx;
        private final String name;
        private final Predicate<T> defaultResult;
        private int pos;
        public Iter(FilterContext<T> ctx, String name, Predicate<T> defaultResult) {
            this.ctx = Objects.requireNonNull(ctx);
            this.name = Objects.requireNonNull(name);
            this.defaultResult = Objects.requireNonNull(defaultResult);
        }
        @Override
        public boolean hasNext() {
            return pos < ctx.getCount(name);
        }
        @Override
        public Predicate<T> next() {
            return ctx.get(name, pos++, defaultResult);
        }
    }
}

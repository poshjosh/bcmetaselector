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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import com.bc.meta.selector.FilterContext;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 15, 2018 11:36:02 PM
 */
public class FilterContextComposite<E> extends AbstractFilterContext<E> implements Serializable {

    private final boolean and;

    private final Collection<FilterContext<E>> delegates;
    
    public FilterContextComposite(boolean and, Collection<FilterContext<E>> delegates) {
        this.and = and;
        this.delegates = Collections.unmodifiableCollection(delegates);
    }
    
    public FilterContextComposite(boolean and, FilterContext<E>... delegates) {
        this.and = and;
        this.delegates = Arrays.asList(delegates);
    }

    @Override
    public int getCount(String name) {
        int max = 0;
        for(FilterContext filterCtx : delegates) {
            max = Math.max(max, filterCtx.getCount(name));
        }
        return max;
    }

    @Override
    public Predicate<E> get(String name, int i, Predicate<E> outputIfNone) {
        Predicate<E> result = null;
        for(FilterContext filterCtx : delegates) {
            if(i >= filterCtx.getCount(name)) {
                continue;
            }
            final Predicate<E> test = filterCtx.get(name, i, outputIfNone);
            result = result == null ? test : and ? result.and(test) : result.or(test);
        }
        return result == null ? outputIfNone : result;
    }
}

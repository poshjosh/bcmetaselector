/*
 * Copyright 2016 NUROX Ltd.
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.function.BiFunction;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 21, 2016 11:46:33 AM
 */
public class MetaContent<E> implements Metadata {
    
    private final List<E> nodes;
    
    private final FilterContext<E> filterContext;
    
    private final BiFunction<String, E, String> attributeValueExtractor;
    
    public MetaContent(
            List<E> nodes, 
            FilterContext<E> filterContext, 
            BiFunction<String, E, String> attributeValueExtractor) {
        this.nodes = Collections.unmodifiableList(nodes);
        this.filterContext = Objects.requireNonNull(filterContext);
        this.attributeValueExtractor = Objects.requireNonNull(attributeValueExtractor);
    }

    @Override
    public String getValue(String name, String outputIfNone) {
        final E node = this.filterContext.findFirstMatchingAnyFilter(name, null, nodes.iterator(), null);
        return this.attributeValueExtractor.apply(name, node);
    }

    @Override
    public Set<String> getValues(String name) {
        final List<E> found = this.filterContext.selectMatchingAnyFilter(name, null, nodes.iterator(), nodes.size());
        final Function<E, String> mapper = (node) -> this.attributeValueExtractor.apply(name, node);
        return found.stream().map(mapper).collect(Collectors.toSet());
    }
}

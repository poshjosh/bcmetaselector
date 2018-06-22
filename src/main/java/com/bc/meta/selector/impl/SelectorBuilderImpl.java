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

import com.bc.meta.selector.FilterBuilder;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.Objects;
import com.bc.meta.selector.Selector;
import com.bc.meta.selector.SelectorBuilder;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 21, 2018 11:51:22 AM
 */
public class SelectorBuilderImpl<NODE, NODEVALUE, PREVIOUS_BUILDER> implements SelectorBuilder<NODE, NODEVALUE, PREVIOUS_BUILDER> {

    private FilterBuilder<NODE, SelectorBuilder<NODE, NODEVALUE, PREVIOUS_BUILDER>> filterBuilder;

    private BiFunction<String, NODE, NODEVALUE> nodeConverter;
    
    private Predicate<String> multipleValueTest;
    
    private final PREVIOUS_BUILDER back;

    public SelectorBuilderImpl() {
        this(null);
    }
    
    public SelectorBuilderImpl(PREVIOUS_BUILDER back) {
        this.back = back;
        this.reset();
    }
    
    @Override
    public SelectorBuilder<NODE, NODEVALUE, PREVIOUS_BUILDER> reset() {
        if(this.filterBuilder != null) {
            this.filterBuilder.reset();
        }
        this.nodeConverter = null;
        this.multipleValueTest = null;
        return this;
    }

    @Override
    public PREVIOUS_BUILDER back() {
        return Objects.requireNonNull(back);
    }
    
    @Override
    public Selector<NODE> build() throws IOException, ParseException{
        Objects.requireNonNull(nodeConverter);
        Objects.requireNonNull(multipleValueTest);
        if(filterBuilder == null) {
            filterBuilder = new FilterBuilderImpl();
        }
        final Map<String, Predicate<NODE>> nodeTests = filterBuilder.build();
        return new SelectorImpl(nodeTests, nodeConverter, multipleValueTest);
    }

    @Override
    public FilterBuilder<NODE, SelectorBuilder<NODE, NODEVALUE, PREVIOUS_BUILDER>> filter() {
        if(filterBuilder == null) {
            this.filterBuilder(new FilterBuilderImpl(this));
        }
        return filterBuilder;
    }

    @Override
    public SelectorBuilder<NODE, NODEVALUE, PREVIOUS_BUILDER> filterBuilder(FilterBuilder<NODE, SelectorBuilder<NODE, NODEVALUE, PREVIOUS_BUILDER>> filterBuilder) {
        this.filterBuilder = filterBuilder;
        return this;
    }

    @Override
    public SelectorBuilder<NODE, NODEVALUE, PREVIOUS_BUILDER> nodeConverter(BiFunction<String, NODE, NODEVALUE> nodeConverter) {
        this.nodeConverter = nodeConverter;
        return this;
    }

    @Override
    public SelectorBuilder<NODE, NODEVALUE, PREVIOUS_BUILDER> multipleValueTest(Predicate<String> multipleValueTest) {
        this.multipleValueTest = multipleValueTest;
        return this;
    }
}

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
import com.bc.meta.selector.FilterContext;
import com.bc.meta.selector.util.JsonParser;
import com.bc.meta.selector.util.PropertiesParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import com.bc.meta.selector.AttributeTestProvider;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 21, 2018 8:58:24 AM
 * @param <E> The Node type that filters (predicates) will be built for
 * @param <B> The back handle type, i.e return type of the #back() method
 */
public class FilterBuilderImpl<E, B> implements FilterBuilder<E, B> {

    private Collection<String> configFilePaths;
    
    private Collection<String> propertyNames;
    
    private AttributeTestProvider<E> attributeContext;
    
    private JsonParser jsonParser;
    
    private BiFunction<AttributeTestProvider<E>, Map, FilterContext<E>> filterContextProvider;
    
    private Predicate<E> defaultTest;
    
    private String charset;
    
    private final B back;

    public FilterBuilderImpl() {
        this(null);
    }
    
    public FilterBuilderImpl(B back) {
        this.back = back;
        this.reset();
    }
        
    @Override
    public FilterBuilder<E, B> reset() {
        this.configFilePaths(Collections.EMPTY_LIST);
        this.propertyNames(Collections.EMPTY_LIST);
        this.attributeContext(null);
        this.jsonParser(null);
        this.filterContextProvider((ac, cfg) -> new FilterContextImpl(ac, cfg));
        this.defaultTest((node) -> false);
        this.charset(StandardCharsets.UTF_8);
        return this;
    }

    @Override
    public B back() {
        return Objects.requireNonNull(back, "No back handle specified");
    }
    
    @Override
    public Map<String, Predicate<E>> build() throws IOException, ParseException{
        
        Objects.requireNonNull(this.attributeContext);
        Objects.requireNonNull(this.jsonParser);
        
        Map<String, Predicate<E>> output = null;
        
        final PropertiesParser propertiesParser = new PropertiesParser(
                this.jsonParser, this.charset, true);
        
        for(String propertyName : this.propertyNames) {
            
            Predicate<E> result = null;
            
            for(String configFile : this.configFilePaths) {
                
                final Map filterContextProperties = propertiesParser.parse(configFile);
                
                final FilterContext filterContext = this.filterContextProvider.apply(
                        this.attributeContext, filterContextProperties);
                
                final Predicate<E> test = filterContext.or(propertyName, this.defaultTest);
                
                if(result == null) {
                    result = test;
                }else{
                    result = result.or(test);
                }
            }
            
            if(result != null) {
                
                if(output == null) {
                    output = new HashMap(this.propertyNames.size(), 1.0f);
                }
                
                output.put(propertyName, result);
            }
        }
        
        return output == null || output.isEmpty() ?
                Collections.EMPTY_MAP : Collections.unmodifiableMap(output);
    }

    @Override
    public FilterBuilder<E, B> configFilePaths(Collection<String> configFilePaths) {
        this.configFilePaths = configFilePaths;
        return this;
    }

    @Override
    public FilterBuilder<E, B> propertyNames(Collection<String> propertyNames) {
        this.propertyNames = propertyNames;
        return this;
    }

    @Override
    public FilterBuilder<E, B> attributeContext(AttributeTestProvider<E> attributeContext) {
        this.attributeContext = attributeContext;
        return this;
    }

    @Override
    public FilterBuilder<E, B> jsonParser(JsonParser jsonParser) {
        this.jsonParser = jsonParser;
        return this;
    }

    @Override
    public FilterBuilder<E, B> filterContextProvider(BiFunction<AttributeTestProvider<E>, Map, FilterContext<E>> filterContextProvider) {
        this.filterContextProvider = filterContextProvider;
        return this;
    }

    @Override
    public FilterBuilder<E, B> defaultTest(Predicate<E> defaultTest) {
        this.defaultTest = defaultTest;
        return this;
    }

    @Override
    public FilterBuilder<E, B> charset(String charset) {
        this.charset = charset;
        return this;
    }
}

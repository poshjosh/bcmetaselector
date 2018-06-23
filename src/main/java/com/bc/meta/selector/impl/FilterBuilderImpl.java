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
import java.io.InputStream;
import java.util.function.Function;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 21, 2018 8:58:24 AM
 * @param <NODE> The Node type that filters (predicates) will be built for
 * @param <PREVIOUS_BUILDER> The back handle type, i.e return type of the #back() method
 */
public class FilterBuilderImpl<NODE, PREVIOUS_BUILDER> implements FilterBuilder<NODE, PREVIOUS_BUILDER> {

    private Collection<String> configFilePaths;
    
    private Collection<String> propertyNames;
    
    private AttributeTestProvider<NODE> attributeContext;
    
    private JsonParser jsonParser;
    
    private Function<String, InputStream> streamProvider;
    
    private BiFunction<AttributeTestProvider<NODE>, Map, FilterContext<NODE>> filterContextProvider;
    
    private Predicate<NODE> defaultTest;
    
    private String charset;
    
    private final PREVIOUS_BUILDER back;

    public FilterBuilderImpl() {
        this(null);
    }
    
    public FilterBuilderImpl(PREVIOUS_BUILDER back) {
        this.back = back;
        this.reset();
    }
        
    @Override
    public FilterBuilder<NODE, PREVIOUS_BUILDER> reset() {
        this.configFilePaths(Collections.EMPTY_LIST);
        this.propertyNames(Collections.EMPTY_LIST);
        this.attributeContext(null);
        this.streamProvider(new PropertiesParser.DefaultStreamProvider());
        this.jsonParser(null);
        this.filterContextProvider((ac, cfg) -> new FilterContextImpl(ac, cfg));
        this.defaultTest((node) -> false);
        this.charset(StandardCharsets.UTF_8);
        return this;
    }

    @Override
    public PREVIOUS_BUILDER back() {
        return Objects.requireNonNull(back, "No back handle specified");
    }
    
    @Override
    public Function<String, Predicate<NODE>> build() throws IOException, ParseException{
        
        Objects.requireNonNull(this.attributeContext);
        Objects.requireNonNull(this.streamProvider);
        Objects.requireNonNull(this.jsonParser);
        
        Map<String, Predicate<NODE>> output = null;
        
        final PropertiesParser propertiesParser = new PropertiesParser(
                this.streamProvider, this.jsonParser, this.charset, true);
        
        for(String propertyName : this.propertyNames) {
            
            Predicate<NODE> result = this.buildPredicate(propertiesParser, propertyName, null);
            
            if(result != null) {
                
                if(output == null) {
                    output = new HashMap(this.propertyNames.size(), 1.0f);
                }
                
                output.put(propertyName, result);
            }
        }
        
        final Map<String, Predicate<NODE>> source = output == null || output.isEmpty() ?
                Collections.EMPTY_MAP : Collections.unmodifiableMap(output);
        
        return (name) -> source.get(name);
    }
    
    public Predicate<NODE> buildPredicate(
            PropertiesParser propertiesParser, String propertyName, Predicate<NODE> outputIfNone) 
            throws IOException, java.text.ParseException {
        
        Predicate<NODE> result = null;

        for(String configFile : this.configFilePaths) {

            final Map filterContextProperties = propertiesParser.parse(configFile);

            final FilterContext filterContext = this.filterContextProvider.apply(
                    this.attributeContext, filterContextProperties);

            final Predicate<NODE> test = filterContext.or(propertyName, this.defaultTest);

            if(result == null) {
                result = test;
            }else{
                result = result.or(test);
            }
        }

        return result == null ? outputIfNone : result;
    }

    @Override
    public FilterBuilder<NODE, PREVIOUS_BUILDER> configFilePaths(Collection<String> configFilePaths) {
        this.configFilePaths = configFilePaths;
        return this;
    }

    @Override
    public FilterBuilder<NODE, PREVIOUS_BUILDER> propertyNames(Collection<String> propertyNames) {
        this.propertyNames = propertyNames;
        return this;
    }

    @Override
    public FilterBuilder<NODE, PREVIOUS_BUILDER> attributeContext(AttributeTestProvider<NODE> attributeContext) {
        this.attributeContext = attributeContext;
        return this;
    }

    @Override
    public FilterBuilder<NODE, PREVIOUS_BUILDER> streamProvider(Function<String, InputStream> streamProvider) {
        this.streamProvider = streamProvider;
        return this;
    }

    @Override
    public FilterBuilder<NODE, PREVIOUS_BUILDER> jsonParser(JsonParser jsonParser) {
        this.jsonParser = jsonParser;
        return this;
    }

    @Override
    public FilterBuilder<NODE, PREVIOUS_BUILDER> filterContextProvider(BiFunction<AttributeTestProvider<NODE>, Map, FilterContext<NODE>> filterContextProvider) {
        this.filterContextProvider = filterContextProvider;
        return this;
    }

    @Override
    public FilterBuilder<NODE, PREVIOUS_BUILDER> defaultTest(Predicate<NODE> defaultTest) {
        this.defaultTest = defaultTest;
        return this;
    }

    @Override
    public FilterBuilder<NODE, PREVIOUS_BUILDER> charset(String charset) {
        this.charset = charset;
        return this;
    }
}

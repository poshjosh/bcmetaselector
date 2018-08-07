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

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bc.meta.selector.AttributeTestProvider;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 15, 2018 9:39:01 PM
 */
public class FilterContextImpl<NODE> extends AbstractFilterContext<NODE> implements Serializable {

    private transient static final Logger LOG = Logger.getLogger(FilterContextImpl.class.getName());

    private final AttributeTestProvider<NODE> attributeContext;
    
    private final Map config;
    
    public FilterContextImpl(AttributeTestProvider<NODE> attributeContext, Map config) {
        this.attributeContext = Objects.requireNonNull(attributeContext);
        this.config = Collections.unmodifiableMap(config);
//        System.out.println("FilterContextImpl-" + LocalDateTime.now() +  ". Config: " + config);
        LOG.log(Level.FINER, "Config: {0}", config);
    }
    
    @Override
    public int getCount(String name) {
        final List<Map> attrCfgs = this.getAttributeList(name);
        final int count = attrCfgs == null ? 0 : attrCfgs.size();
//        System.out.println("FilterContextImpl-" + LocalDateTime.now() +  ". " +name + '=' + count);
        LOG.finer(() -> name + '=' + count);
        return count;
    }
    
    @Override
    public Predicate<NODE> get(String name, int i, Predicate<NODE> outputIfNone) {
        final String prefix = "regex(";
        final String suffix = ")";
        final List<Map> attrCfgs = this.getAttributeList(name);
        final Map attrCfg = attrCfgs.get(i);
        final Set keys = attrCfg.keySet();
        Predicate<NODE> result = null;
        for(Object k : keys) {
            final Object v = attrCfg.get(k);
            String key = k.toString();
            String val = v.toString();
            final boolean regex = (val.startsWith(prefix) && val.endsWith(")"));
            final Predicate<NODE> test;
            if(regex) {
                val = val.substring(prefix.length(), val.length()-suffix.length());
                test = this.attributeContext.getAttributeRegexTest(key, val);
            }else{
                test = this.attributeContext.getAttributeTest(key, val);
            }
            if(result == null) {
                result = test;
            }else{
                result = result.and(test); 
            }
        }
        final Predicate<NODE> output = result == null ? outputIfNone : result;
//        System.out.println("FilterContextImpl-" + LocalDateTime.now() +  ". " + name + '(' + i + ") = " + attrCfg + ", filter: " + output);
        LOG.finer(() -> name + '(' + i + ") = " + attrCfg + ", filter: " + output);
        return output;
    }

    public List<Map> getAttributeList(String name) {
        final List<Map> attrCfgs = (List<Map>)this.config.get(name);
//        System.out.println("FilterContextImpl-" + LocalDateTime.now() +  ". " + name + '=' + attrCfgs);
        LOG.finer(() -> name + '=' + attrCfgs);
        return attrCfgs;
    }
}

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

package com.bc.meta.selector.jsoup;

import com.bc.meta.selector.impl.AbstractFilterContext;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Logger;
import org.jsoup.nodes.Element;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 18, 2018 12:23:07 AM
 */
public class FilterContextJsoup extends AbstractFilterContext<Element> implements Serializable {

    private transient static final Logger LOG = Logger.getLogger(FilterContextJsoup.class.getName());

    private final Map config;

    public FilterContextJsoup(Map config) {
        this.config = Collections.unmodifiableMap(config);
    }
    
    @Override
    public int getCount(String name) {
        final String attrCfgs = this.getAttributeConfigs(name);
        return attrCfgs == null ? 0 : 1;
    }

    @Override
    public Predicate<Element> get(String name, int i, Predicate<Element> outputIfNone) {
        final String attrCfgs = this.getAttributeConfigs(name);
        final Predicate<Element> result = (elem) -> elem.selectFirst(attrCfgs) != null;
        LOG.finer(() -> name + '(' + i + ") = " + attrCfgs + ", filter: " + result);
        return result;
    }

    public String getAttributeConfigs(String name) {
        final String attrCfgs = (String)this.config.get(name);
        LOG.finer(() -> name + '=' + attrCfgs);
        return attrCfgs;
    }
}

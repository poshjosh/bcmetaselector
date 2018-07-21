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

package com.bc.meta.impl;

import com.bc.meta.Metadata;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 22, 2016 3:00:21 PM
 */
public class MetadataComposite implements Metadata {
    
    private transient static final Logger logger = Logger.getLogger(MetadataComposite.class.getName());
    
    private final Collection<Metadata> delegates;

    public MetadataComposite(Collection<Metadata> delegates) {
        this.delegates = Collections.unmodifiableCollection(delegates);
    }
    
    public MetadataComposite(Metadata... delegates) {
        this.delegates = Arrays.asList(delegates);
    }

    @Override
    public Map<String, ?> toMap() {
        final Map map = new HashMap();
        for(Metadata delegate : delegates) {
            map.putAll(delegate.toMap());
        }
        return map.isEmpty() ? Collections.EMPTY_MAP : Collections.unmodifiableMap(map);
    }

    @Override
    public String getValue(String name, String outputIfNone) {
        String value = null;
        for(Metadata delegate : delegates) {
            value = delegate.getValue(name, null);
            if(value != null) {
                break;
            }
        }
        return value == null ? outputIfNone : value;
    }

    @Override
    public Set<String> getValues(String name) {
        Set<String> values = null;
        for(Metadata delegate : delegates) {
            values = delegate.getValues(name);
            if(values != null && !values.isEmpty()) {
                break;
            }
        }
        return values;
    }
}

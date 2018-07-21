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

package com.bc.meta.impl;

import com.bc.meta.Metadata;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 22, 2018 6:38:35 PM
 */
public class MetadataImpl implements Serializable, Metadata {
    
    private final Map<String, Object> values;

    public MetadataImpl(Map<String, Object> values) {
        this.values = Collections.unmodifiableMap(values);
    }

    @Override
    public Map<String, ?> toMap() {
        return values;
    }

    @Override
    public String getValue(String name, String outputIfNone) {
        return (String)values.getOrDefault(name, outputIfNone);
    }

    @Override
    public Set<String> getValues(String name) {
        return (Set<String>)values.getOrDefault(name, Collections.EMPTY_SET);
    }
}

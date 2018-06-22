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

import com.bc.meta.selector.Selector;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 22, 2018 6:04:37 PM
 */
public class Collectors {

    public static class CollectIntoBean<E> implements Selector.Collector<E>, Serializable {

        private final E bean;
        
        private final Class type;

        public CollectIntoBean(E bean) { 
            this.bean = Objects.requireNonNull(bean);
            this.type = bean.getClass();
        }
        @Override
        public E buildResult() {
            return bean;
        }
        @Override
        public Object getOrDefault(String propertyName, Object valueIfNone) {
            try{
                return type.getMethod(this.buildMethodName("get", propertyName)).invoke(bean);
            }catch(NoSuchMethodException | SecurityException | IllegalAccessException | 
                    IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void put(String propertyName, Object value) { 
            try{
                type.getMethod(this.buildMethodName("set", propertyName), value.getClass()).invoke(bean, value);
            }catch(NoSuchMethodException | SecurityException | IllegalAccessException | 
                    IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        public String buildMethodName(String prefix, String key) {
            return prefix + Character.toUpperCase(key.codePointAt(0)) + key.substring(1);
        }
        public E getBean() {
            return bean;
        }
    }
    
    public static class CollectIntoMap implements Selector.Collector<Map<String, Object>>, Serializable {

        private final Map<String, Object> buffer;

        public CollectIntoMap(Map<String, Object> buffer) { 
            this.buffer = Objects.requireNonNull(buffer);
        }
        @Override
        public Map<String, Object> buildResult() {
            return buffer;
        }
        @Override
        public Object getOrDefault(String propertyName, Object valueIfNone) {
            return buffer.getOrDefault(propertyName, valueIfNone);
        }
        @Override
        public void put(String propertyName, Object value) {
            buffer.put(propertyName, value);
        }
    }

    public static <E> Selector.Collector<E> toBean(E collectInto) {
        return new CollectIntoBean(collectInto);
    }
    
    public static Selector.Collector<Map<String, Object>> toMap() {
        return toMap(new LinkedHashMap<>());
    }

    public static Selector.Collector<Map<String, Object>> toMap(Map<String, Object> collectInto) {
        return new CollectIntoMap(collectInto);
    }
}

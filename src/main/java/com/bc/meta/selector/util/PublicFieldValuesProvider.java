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

package com.bc.meta.selector.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 21, 2018 12:32:24 PM
 */
public class PublicFieldValuesProvider implements BiFunction<Object, Predicate<Field>, Set> {

    public Set apply(Object ref, Predicate<Field> fieldTest) {
        
        final Function<Field, Object> getFieldValue = (field) -> {
            try{
                return field.get(ref instanceof Class ? null : ref);
            }catch(IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
        
        final Class type = ref instanceof Class ? (Class)ref : ref.getClass();
        
        return Arrays.asList(type.getFields())
                .stream()
                .filter(fieldTest)
                .map(getFieldValue)
                .filter((fieldValue) -> fieldValue != null)
                .collect(Collectors.toSet());
    }
}

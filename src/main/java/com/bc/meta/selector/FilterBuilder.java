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

package com.bc.meta.selector;

import com.bc.meta.selector.util.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 21, 2018 9:41:12 AM
 */
public interface FilterBuilder<NODE, PREVIOUS_BUILDER> extends Builder<Function<String, Predicate<NODE>>, PREVIOUS_BUILDER> {

    FilterBuilder<NODE, PREVIOUS_BUILDER> attributeContext(AttributeTestProvider<NODE> attributeContext);

    @Override
    Function<String, Predicate<NODE>> build() throws IOException, ParseException;
    
    default FilterBuilder<NODE, PREVIOUS_BUILDER> charset(Charset charset) {
        return this.charset(charset.name());
    }

    FilterBuilder<NODE, PREVIOUS_BUILDER> charset(String charset);
    
    default FilterBuilder<NODE, PREVIOUS_BUILDER> configFilePaths(String... configFilePaths) {
        return this.configFilePaths(Arrays.asList(configFilePaths));
    }

    FilterBuilder<NODE, PREVIOUS_BUILDER> configFilePaths(Collection<String> configFilePaths);

    FilterBuilder<NODE, PREVIOUS_BUILDER> defaultTest(Predicate<NODE> defaultTest);

    FilterBuilder<NODE, PREVIOUS_BUILDER> filterContextProvider(BiFunction<AttributeTestProvider<NODE>, Map, FilterContext<NODE>> filterContextProvider);

    FilterBuilder<NODE, PREVIOUS_BUILDER> jsonParser(JsonParser jsonParser);
    
    default FilterBuilder<NODE, PREVIOUS_BUILDER> propertyNames(String... propertyNames) {
        return this.propertyNames(Arrays.asList(propertyNames));
    }

    FilterBuilder<NODE, PREVIOUS_BUILDER> propertyNames(Collection<String> propertyNames);

    @Override
    FilterBuilder<NODE, PREVIOUS_BUILDER>  reset();

    FilterBuilder<NODE, PREVIOUS_BUILDER>  streamProvider(Function<String, InputStream> streamProvider);
}

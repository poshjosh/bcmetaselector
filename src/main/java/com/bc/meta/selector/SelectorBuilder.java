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

import java.io.IOException;
import java.text.ParseException;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 21, 2018 11:51:43 AM
 * @param <NODE> The type of the Node to select
 * @param <NODEVALUE> The type of the node value to select from the nod
 */
public interface SelectorBuilder<NODE, NODEVALUE, PREVIOUS_BUILDER> extends Builder<Selector<NODE>, PREVIOUS_BUILDER> {

    @Override
    SelectorBuilder<NODE, NODEVALUE, PREVIOUS_BUILDER> reset();
    
    @Override
    Selector<NODE> build() throws IOException, ParseException;

    FilterBuilder<NODE, SelectorBuilder<NODE, NODEVALUE, PREVIOUS_BUILDER>> filter();
    
    SelectorBuilder<NODE, NODEVALUE, PREVIOUS_BUILDER> filterBuilder(FilterBuilder<NODE, SelectorBuilder<NODE, NODEVALUE, PREVIOUS_BUILDER>> filterBuilder);

    SelectorBuilder<NODE, NODEVALUE, PREVIOUS_BUILDER> nodeConverter(BiFunction<String, NODE, NODEVALUE> nodeConverter);

    SelectorBuilder<NODE, NODEVALUE, PREVIOUS_BUILDER> multipleValueTest(Predicate<String> multipleValueTest);
}

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

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 21, 2018 1:35:01 PM
 * @param <RESULT> The type returned by the #build() method
 * @param <PREVIOUS_BUILDER> The type returned by the #back() method
 */
public interface Builder<RESULT, PREVIOUS_BUILDER> {

    Builder<RESULT, PREVIOUS_BUILDER> reset();
    
    RESULT build() throws Exception;
    
    PREVIOUS_BUILDER back();
}

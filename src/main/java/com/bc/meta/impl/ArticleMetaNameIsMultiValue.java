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

import com.bc.meta.ArticleMetaNames;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 21, 2018 3:54:30 PM
 */
public class ArticleMetaNameIsMultiValue implements ArticleMetaNames, Predicate<String> {

    @Override
    public boolean test(String prop) {
        return CATEGORY_SET.equals(prop) ||
                IMAGELINK_SET.equals(prop) ||
                TAG_SET.equals(prop);
    }
}

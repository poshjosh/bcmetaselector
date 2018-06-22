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

package com.bc.meta;

import com.bc.meta.selector.util.PublicFieldValuesProvider;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 5, 2016 8:17:21 PM
 */
public interface ArticleMetaNames {
//@todo add getGeo or getPlacename,getPosition,getRegion,getICBM
//<meta name="geo.placename" content="Lagos island, Lagos, Nigeria"/>
//<meta name="geo.position" content="6.4548790;3.4245980"/>
//<meta name="geo.region" content="NG-Lagos"/>
    //@notes=DO NOT EDIT ANY OF THE KEYS they are reserved and used within the metaselector api
    String TITLE = "title";
    String AUTHOR = "author";
    String PUBLISHER = "publisher";
    String TYPE = "type";
    String TAG_SET = "tagSet";
    String CATEGORY_SET = "categorySet";
    String KEYWORDS = "keywords";
    String DESCRIPTION = "description";
    String CONTENT = "content";
    String DATE_CREATED = "dateCreated";
    String DATE_PUBLISHED = "datePublished";
    String DATE_MODIFIED = "dateModified";
    String IMAGELINK_SET = "imageLinkSet";
    String LOCALE = "locale";
    
    Set<String> VALUES = (Set<String>)new PublicFieldValuesProvider()
                .apply(ArticleMetaNames.class, (field) -> true)
                .stream()
                .filter((fieldValue) -> fieldValue instanceof String)
                .map(Object::toString)
                .collect(Collectors.toSet());
    
    public static Set<String> values() {
        return VALUES;
    }
}

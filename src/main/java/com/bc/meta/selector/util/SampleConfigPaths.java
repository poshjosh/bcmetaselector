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

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 21, 2018 2:41:42 PM
 */
public interface SampleConfigPaths {

    String DIR = Paths.get("META-INF", "bcmetaselector", "configs").toString();

    String APP_ARTICLE = Paths.get(DIR, "app.article.json").toString();
    
    String BASIC = Paths.get(DIR, "basic.basic.json").toString();

    String OPENGRAPH_CUSTOM = Paths.get(DIR, "opengraph.custom.json").toString();

    String SCHEMA_ARTICLE = Paths.get(DIR, "schema.Article.json").toString();

    String SCHEMA_CREATIVEWORK = Paths.get(DIR, "schema.CreativeWork.json").toString();

    String SCHEMA_THING = Paths.get(DIR, "schema.Thing.json").toString();

    String TWITTERCARD_CUSTOM = Paths.get(DIR, "twittercard.custom.json").toString();

    List<String> APP_ARTICLE_LIST = Arrays.asList(SCHEMA_ARTICLE, OPENGRAPH_CUSTOM, TWITTERCARD_CUSTOM, BASIC);
}

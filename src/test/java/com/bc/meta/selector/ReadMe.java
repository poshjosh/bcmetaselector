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

import com.bc.meta.selector.htmlparser.AttributeContextHtmlparser;
import com.bc.meta.selector.util.SampleConfigPaths;
import com.bc.meta.ArticleMetaNames;
import com.bc.meta.impl.ArticleMetaNameIsMultiValue;
import java.util.Map;
import java.util.Iterator;
import java.util.function.BiFunction;
import org.json.simple.JSONValue;
import org.htmlparser.Parser;
import org.htmlparser.Node;
import org.htmlparser.Tag;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 20, 2018 11:01:24 PM
 */
public class ReadMe {

    public static void main(String... args) throws Exception {
        
        final BiFunction<String, Node, String> nodeContentExtractor =
                (prop, node) -> node instanceof Tag ? ((Tag)node).getAttributeValue("content") : null;
        
        final SelectorBuilder<Node, String, Object> builder = Selector.builder();

        final Selector<Node> selector = builder.filter()
                .attributeContext(new AttributeContextHtmlparser(false))
                .configFilePaths(SampleConfigPaths.APP_ARTICLE_LIST)
                .jsonParser((reader) -> (Map)JSONValue.parse(reader))
                .propertyNames(ArticleMetaNames.values())
                .back()
                .multipleValueTest(new ArticleMetaNameIsMultiValue())
                .nodeConverter(nodeContentExtractor)
                .build();
        
        final Parser parser = new Parser();
     
        final String url = "https://edition.cnn.com/2018/06/21/africa/noura-hussein-asequals-intl/index.html";
        
        parser.setURL(url);

        Iterator<Node> nodes = parser.elements().iterator();

        final Map map = selector.selectAsMap(nodes, ArticleMetaNames.values());
        
        System.out.println("Printing meta tags data for: " + url + "\n" + map);
    }
}

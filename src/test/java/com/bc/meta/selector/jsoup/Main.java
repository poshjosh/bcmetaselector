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

package com.bc.meta.selector.jsoup;

import java.net.URL;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Evaluator;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.QueryParser;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 22, 2018 12:28:03 PM
 */
public class Main {

    public static void main(String... args) {
        try{
            final int timeout = 0;
            final String urlStr = "";
            final Document doc = org.jsoup.Jsoup.parse(new URL(urlStr), timeout);
            
            final String cssQuery = "";
            Validate.notEmpty(cssQuery);

            final Evaluator evaluator = QueryParser.parse(cssQuery);
            Validate.notNull(evaluator);
            Validate.notNull(doc);
            
            BiConsumer<Element, Integer> consumer = (element, depth) -> {};
            NodeTraversor.traverse(new EvaluatorResultConsumer(doc, consumer, evaluator), doc);

            boolean proceed = true;
            BiPredicate<Element, Integer> predicate = (element, depth) -> proceed;
            NodeTraversor.filter(new EvaluatorResultFilter(doc, predicate, evaluator), doc);
        }catch(Exception e) {
            
        }
    }
}

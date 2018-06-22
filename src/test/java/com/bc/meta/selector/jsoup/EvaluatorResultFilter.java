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

import java.util.Objects;
import java.util.function.BiPredicate;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Evaluator;
import org.jsoup.select.NodeFilter;
import org.jsoup.select.NodeFilter.FilterResult;
import static org.jsoup.select.NodeFilter.FilterResult.CONTINUE;
import static org.jsoup.select.NodeFilter.FilterResult.STOP;
import org.jsoup.select.QueryParser;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 22, 2018 1:24:18 PM
 */
public class EvaluatorResultFilter implements NodeFilter {
    
    private final Element root;
    private final BiPredicate<Element, Integer> predicate;
    private final Evaluator eval;

    public EvaluatorResultFilter(Element root, BiPredicate<Element, Integer> predicate, String cssQuery) {
        this(root, predicate, QueryParser.parse(cssQuery));
    }

    public EvaluatorResultFilter(Element root, BiPredicate<Element, Integer> predicate, Evaluator eval) {
        this.root = Objects.requireNonNull(root);
        this.predicate = Objects.requireNonNull(predicate);
        this.eval = Objects.requireNonNull(eval);
    }

    @Override
    public FilterResult head(Node node, int depth) {
        if (node instanceof Element) {
            Element el = (Element) node;
            if (eval.matches(root, el)) {
                final boolean proceed = predicate.test(el, depth);
                if(!proceed) {
                    return STOP;
                }
            }
        }
        return CONTINUE;
    }

    @Override
    public FilterResult tail(Node node, int depth) {
        return CONTINUE;
    }
}

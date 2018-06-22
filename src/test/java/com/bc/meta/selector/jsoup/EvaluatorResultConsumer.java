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
import java.util.function.BiConsumer;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Evaluator;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.jsoup.select.QueryParser;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 22, 2018 1:13:34 PM
 */
public class EvaluatorResultConsumer implements NodeVisitor {
        
    private final Element root;
    private final BiConsumer<Element, Integer> consumer;
    private final Evaluator eval;

    public EvaluatorResultConsumer(Element root, BiConsumer<Element, Integer> consumer, String cssQuery) {
        this(root, consumer, QueryParser.parse(cssQuery));
    }
    
    public EvaluatorResultConsumer(Element root, BiConsumer<Element, Integer> consumer, Evaluator eval) {
        this.root = Objects.requireNonNull(root);
        this.consumer = Objects.requireNonNull(consumer);
        this.eval = Objects.requireNonNull(eval);
    }
    
    @Override
    public void head(Node node, int depth) {
        if (node instanceof Element) {
            Element el = (Element) node;
            if (eval.matches(root, el)) {
                consumer.accept(el, depth);
            }    
        }
    }

    @Override
    public void tail(Node node, int depth) {
        // void
    }
}

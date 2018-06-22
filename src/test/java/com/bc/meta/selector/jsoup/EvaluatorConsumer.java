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

import java.io.Serializable;
import java.util.Objects;
import java.util.function.BiConsumer;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 22, 2018 1:43:02 PM
 */
public class EvaluatorConsumer implements Serializable {
    
    private final BiConsumer<Element, Integer> consumer;
    
    public EvaluatorConsumer(BiConsumer<Element, Integer> consumer) {
        this.consumer = Objects.requireNonNull(consumer);
    }
    
    /**
     * Adapted from {@link org.jsoup.select.NodeTraversor#traverse(org.jsoup.select.NodeVisitor, org.jsoup.nodes.Node) NodeTraversor#traverse}
     * Start a depth-first traverse of the root and all of its descendants.
     * @param eval the Evaluator which decides nodes to accept
     * @param root the root node point to traverse.
     * @see org.jsoup.select.NodeTraversor#traverse(org.jsoup.select.NodeVisitor, org.jsoup.nodes.Node)
     */
    public void traverse(Evaluator eval, Element root) {
        Node node = root;
        int depth = 0;
        
        while (node != null) {
            this.head(eval, root, node, depth);
            if (node.childNodeSize() > 0) {
                node = node.childNode(0);
                depth++;
            } else {
                while (node.nextSibling() == null && depth > 0) {
                    this.tail(eval, root, node, depth);
                    node = node.parentNode();
                    depth--;
                }
                this.tail(eval, root, node, depth);
                if (node == root)
                    break;
                node = node.nextSibling();
            }
        }
    }

    /**
     * Adapted from {@link org.jsoup.select.NodeTraversor#traverse(org.jsoup.select.NodeVisitor, org.jsoup.select.Elements) NodeTraversor#traverse}
     * Start a depth-first traverse of all elements.
     * @param eval the Evaluator which decides nodes to accept
     * @param elements Elements to filter.
     * @see org.jsoup.select.NodeTraversor#traverse(org.jsoup.select.NodeVisitor, org.jsoup.select.Elements) 
     */
    public void traverse(Evaluator eval, Elements elements) {
        Validate.notNull(elements);
        for (Element el : elements) {
            traverse(eval, el);
        }    
    }
    
    /**
     * Adapted from {@link org.jsoup.select.NodeVisitor#head(org.jsoup.nodes.Node, int) NodeVisitor#head}
     * @param eval the Evaluator which decides nodes to accept
     * @param root
     * @param node
     * @param depth 
     * @see org.jsoup.select.NodeVisitor#head(org.jsoup.nodes.Node, int)
     */
    public void head(Evaluator eval, Element root, Node node, int depth) {
        if (node instanceof Element) {
            Element el = (Element) node;
            if (eval.matches(root, el)) {
                consumer.accept(el, depth);
            }    
        }
    }

    /**
     * Adapted from {@link org.jsoup.select.NodeVisitor#tail(org.jsoup.nodes.Node, int) NodeVisitor#tail}
     * @param eval the Evaluator which decides nodes to accept
     * @param root
     * @param node
     * @param depth 
     * @see org.jsoup.select.NodeVisitor#tail(org.jsoup.nodes.Node, int)
     */
    public void tail(Evaluator eval, Element root, Node node, int depth) {
        // void
    }
}

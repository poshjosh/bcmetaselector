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

import com.bc.meta.ArticleMetaNames;
import com.bc.meta.impl.ArticleMetaNameIsMultiValue;
import static com.bc.meta.selector.MetaSelectorTest.debug;
import com.bc.meta.selector.htmlparser.AttributeContextHtmlparser;
import com.bc.meta.selector.impl.Collectors;
import com.bc.meta.selector.impl.FilterContextImpl;
import com.bc.meta.selector.jsoup.AttributeContextJsoup;
import com.bc.meta.selector.jsoup.FilterContextJsoup;
import com.bc.meta.selector.util.SampleConfigPaths;
import com.bc.net.RequestBuilder;
import com.bc.net.impl.RequestBuilderImpl;
import java.io.IOException;
import java.net.MalformedURLException;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.htmlparser.Tag;
import org.htmlparser.dom.HtmlDocument;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 15, 2018 2:59:00 AM
 */
public class Main {
    
    private final Consumer<Node> nodePrinter = (node) -> System.out.println(
            (node instanceof Tag ? ((Tag)node).toTagHtml() : node )
    );
    
    private final Function<Node, String> nodeToString = (node) -> 
            node instanceof Tag ? ((Tag)node).toTagHtml() : node.toHtml();
    
    private final Set<String> names = ArticleMetaNames.values();
    
    private final Set<String> namesToSelect = names;

    public static void main(String... args) {
        
        String [] urls = {"https://www.bellanaija.com/2018/06/apple-deal-oprah-shows/",
                "https://www.naija.ng/1175319-portugal-force-spain-3-3-draw-group-b-opener-russia-2018.html",
                "https://www.lindaikejisblog.com/2018/6/worldcup-2018-cristiano-ronaldo-scores-brilliant-hat-trick-against-spain-becomes-the-first-player-to-score-at-eight-major-international-tournaments.html",
                "https://www.dailytrust.com.ng/why-i-cannot-refund-money-to-efcc-ladoja-256553.html",
                "http://www.pulse.ng/news/local/i-dont-know-if-my-son-is-dead-or-alive-shekaus-mother-id8499832.html",
                "https://www.bellanaija.com/2018/06/new-music-inspire-nsido/",
                "https://www.naija.ng/1175450-governor-yari-zamfara-resigns-role-chief-security-officer.html",
                "https://www.lindaikejisblog.com/2018/6/blac-chyna-shares-completely-naked-photos-and-her-teenage-boyfriend-cant-help-himself.html",
                "https://www.dailytrust.com.ng/why-buhari-deserves-second-term-kalu-256549.html",
                "http://www.pulse.ng/lifestyle/relationships-weddings/7-amazing-ways-to-live-your-best-life-while-single-id8500676.html"
        };
        
//        for(String url : urls) {
//            ref.test(url);
//        }

//        List<String> urlList = new ArrayList();
//        for(int i=0; i<4; i++) {
//            for(String url : urls) {
//                urlList.add(url);
//            }
//        }
        
//        urls = urlList.toArray(new String[0]);

        final Main examples = new Main();
        
        final int index = 3;
// 0, 2, 3, 8, 9      
        try{
        examples.test(true, true, urls);
//        examples.test(true, false, urls);
//        examples.test(false, true, urls[index);
//        examples.test(false, false, urls[index]);
        }catch(IOException | java.text.ParseException e) {
            e.printStackTrace();
        }
    }
    
    public void test(boolean htmlparser, boolean iterator, String... urls) 
            throws IOException, java.text.ParseException {
        final MetaSelectorTest test;
        if(htmlparser) {
            test = this.createHtmlparserInstance(
                new TagNameFilter("META"), iterator);
        }else{
            test = this.createJsoupInstance(
                    (element) -> "META".equalsIgnoreCase(element.tagName()));
        }
        if(iterator) {
            if(htmlparser) test.filterProviderWithIteratorTest(urls);
        }else{
            test.filterProviderTest(urls);
            test.metadataTest(urls);
        }
    }
    
    public MetaSelectorTest<Node> createHtmlparserInstance(NodeFilter nodeFilter, boolean iterator) 
            throws IOException, java.text.ParseException {
        final AttributeContextHtmlparser attributeContext = new AttributeContextHtmlparser(true);
        final Function<String, List<Node>> parser = this.htmlparserNodeExtractor(
                attributeContext, nodeFilter, iterator
        );
        final MetaSelectorTest<Node> result = new MetaSelectorTest<>(
                parser, 
                attributeContext, 
                (ac, cfg) -> new FilterContextImpl(ac, cfg), 
                attributeContext       
        );
        return result;
    }
    
    public Function<String, List<Node>> htmlparserNodeExtractor(
            AttributeContextHtmlparser attributeContext, NodeFilter nodeFilter, boolean iterator) 
            throws IOException, java.text.ParseException {
        final Function<String, List<Node>> parser;
        final Parser delegate = new Parser();
        final RequestBuilder req = this.getRequestBuilder();
        if(iterator) {
            final String ID = "SELECTOR WITH ITERATOR";
            final Selector<Node> selector = this.getSelector(ID, attributeContext, attributeContext);
            final Map<String, Object> reused = new HashMap<>();
            final Collectors.CollectIntoMap collector = new Collectors.CollectIntoMap(reused) {
                @Override
                public void put(String propertyName, Object value) {
                    if(debug) System.out.println(propertyName + " = " + value);
                    super.put(propertyName, value); 
                }
            };
            parser = (url) -> {
                try{
                    NodeIterator iter = Main.this.buildNodes(delegate, req, url);
                    if(debug) System.out.println("\n" + LocalDateTime.now() + ". " +ID+" extracting url: " + url);
                    
                    reused.clear();
                    
                    final Map copyThisElseWhere = selector.select(iter.iterator(), namesToSelect, collector);
                    
                    return Collections.EMPTY_LIST;
                    
                }catch(IOException | ParserException e) {
                    throw new RuntimeException(e);
                }
            };
        }else{
            parser = (url) -> {
                try{
                    final HtmlDocument allNodes = Main.this.buildDocument(delegate, req, url);
                    return allNodes;
                }catch(IOException | ParserException e) {
                    throw new RuntimeException(e);
                }
            };
        }
        return parser;
    }

    public MetaSelectorTest<Element> createJsoupInstance(Predicate<Element> nodeFilter) {
        final AttributeContextJsoup attributeContext = new AttributeContextJsoup(true);
        final Function<String, List<Element>> parser = this.jsoupNodeExtractor(nodeFilter);
        final MetaSelectorTest<Element> result = new MetaSelectorTest<>(
                parser, 
                attributeContext, 
                (ac, cfg) -> new FilterContextJsoup(cfg), 
                attributeContext
        );
        return result;
    }

    public Function<String, List<Element>> jsoupNodeExtractor(Predicate<Element> nodeFilter) {
        
        final Function<String, List<Element>> parser = (url) -> {
            try{

                final Document doc = Jsoup.parse(new URL(url), 60_000);

                return doc.getAllElements().stream().filter(nodeFilter).collect(java.util.stream.Collectors.toList());
                
            }catch(Exception e) {
                throw new RuntimeException(e);
            }
        };
        
        return parser;
    }

    public <E> Selector<E> getSelector(String ID, 
            AttributeTestProvider<E> attributeContext,
            BiFunction<String, E, String> attributeValueProvider) 
            throws IOException, java.text.ParseException {
        
        final SelectorBuilder<E, String, Object> builder = Selector.builder();

        return builder.filter()
                .attributeContext(attributeContext)
                .configFilePaths(SampleConfigPaths.APP_ARTICLE_LIST)
                .jsonParser(new JsonParserImpl())
                .propertyNames(names)
                .back()
                .multiValueTest(new ArticleMetaNameIsMultiValue())
                .nodeValueExtractor(attributeValueProvider)
                .build();
    }

    public HtmlDocument buildDocument(Parser parser, RequestBuilder builder, String url) 
            throws MalformedURLException, IOException, ParserException {
        parser.setConnection(builder.clearCookies().url(new URL(url)).build());
//        parser.setURL(url);
        final HtmlDocument allNodes = parser.parse(null);
        return allNodes;
    }

    public NodeIterator buildNodes(Parser parser, RequestBuilder builder, String url) 
            throws MalformedURLException, IOException, ParserException {
        parser.setConnection(builder.clearCookies().url(new URL(url)).build());
//        parser.setURL(url);
        return parser.elements();
    }

    public RequestBuilder getRequestBuilder() {
        final RequestBuilder builder = new RequestBuilderImpl();
        builder.randomUserAgent(true).charset(StandardCharsets.UTF_8.name());
        return builder;
    }
}

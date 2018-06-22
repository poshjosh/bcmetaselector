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

import com.bc.meta.Metadata;
import com.bc.meta.impl.MetadataComposite;
import com.bc.util.Util;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import com.bc.meta.selector.util.SampleConfigPaths;
import com.bc.meta.ArticleMetaNames;
import com.bc.meta.impl.ArticleMetaNameIsMultiValue;
import com.bc.meta.selector.impl.Collectors;
import com.bc.meta.selector.util.JsonParser;
import com.bc.meta.selector.util.PropertiesParser;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import static javax.swing.text.html.HTML.Attribute.ID;
import org.htmlparser.Tag;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 17, 2018 9:56:10 PM
 */
public class MetaSelectorTest<E> {
    
    private final Function<E, String> nodeToString = (node) -> 
            node instanceof Tag ? ((Tag)node).toTagHtml() : node.toString();
    
    public static final boolean debug = false;
    
    public static final long interval = 0;
    
    private final Function<String, List<E>> nodeExtractor;
    
    private final AttributeTestProvider<E> attributeContext;
    
    private final BiFunction<AttributeTestProvider<E>, Map, FilterContext<E>> filterContextProvider;
    
    private final BiFunction<String, E, String> nodeValueExtractor;
    
    private final JsonParser jsonParser = new JsonParserImpl();
    
    public MetaSelectorTest(
            Function<String, List<E>> parser,
            AttributeTestProvider<E> attributeContext, 
            BiFunction<AttributeTestProvider<E>, Map, FilterContext<E>> filterContextProvider,
            BiFunction<String, E, String> nodeConverter) {
        this.nodeExtractor = Objects.requireNonNull(parser);
        this.attributeContext = Objects.requireNonNull(attributeContext);
        this.filterContextProvider = Objects.requireNonNull(filterContextProvider);
        this.nodeValueExtractor = Objects.requireNonNull(nodeConverter);
    }

    public void filterProviderWithIteratorTest(String [] urls) {
        
        if(interval > 0) {
            Runtime.getRuntime().gc();
            try{ Thread.sleep(interval); }catch(InterruptedException e) { e.printStackTrace(); }
        }
        final long tb4 = System.currentTimeMillis();
        final long mb4 = Util.availableMemory();
        
        for(String url : urls) {
            try{
                this.nodeExtractor.apply(url);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("SELECTOR WITH ITERATOR COMPLETED. Consumed. time: " + (System.currentTimeMillis() - tb4) + 
                ", memory: " + Util.usedMemory(mb4) + "\n= x = x = x = x = x = x = x = x = x");
    }
    
    public void filterProviderTest(String [] urls) throws IOException, ParseException {
        
        if(interval > 0) {
            Runtime.getRuntime().gc();
            try{ Thread.sleep(interval); }catch(InterruptedException e) { e.printStackTrace(); }
        }

        final long tb4 = System.currentTimeMillis();
        final long mb4 = Util.availableMemory();
        
        final SelectorBuilder<E, String, Object> builder = Selector.builder();
        
        final Selector<E> selector = builder.filter()
                .attributeContext(attributeContext)
                .configFilePaths(SampleConfigPaths.APP_ARTICLE_LIST)
                .filterContextProvider(filterContextProvider)
                .jsonParser(jsonParser)
                .propertyNames(ArticleMetaNames.values())
                .back()
                .multiValueTest(new ArticleMetaNameIsMultiValue())
                .nodeValueExtractor(nodeValueExtractor)
                .build();
        
        final Map<String, Object> reused = new HashMap<>();
      
        final Collectors.CollectIntoMap consumer = new Collectors.CollectIntoMap(reused) {
            @Override
            public void put(String propertyName, Object value) {
                if(debug) System.out.println(propertyName + " = " + value);
                super.put(propertyName, value); 
            }
        };
        
        for(String url : urls) {
            try{
                final List<E> nodes = this.nodeExtractor.apply(url);
                if(debug) System.out.println("\n" + LocalDateTime.now() + ". " +ID+" extracting url: " + url);
                
                reused.clear();
                
                final Map copyThisElseWhere = selector.select(
                        nodes.iterator(), ArticleMetaNames.values(), consumer);
                
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("SELECTOR COMPLETED. Consumed. time: " + (System.currentTimeMillis() - tb4) + 
                ", memory: " + Util.usedMemory(mb4) + "\n= x = x = x = x = x = x = x = x = x");
    }

    public void metadataTest(String [] urls) {
        
        if(interval > 0) {
            Runtime.getRuntime().gc();
            try{ Thread.sleep(interval); }catch(InterruptedException e) { e.printStackTrace(); }
        }

        final long tb4 = System.currentTimeMillis();
        final long mb4 = Util.availableMemory();
        
        final String ID = "METANODES";
        
        for(String url : urls) {
            try{
                
                final List<E> nodes = this.nodeExtractor.apply(url);
                
                this.metadataTest(ID, url, nodes);
                
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(ID + " COMPLETED. Consumed. time: " + (System.currentTimeMillis() - tb4) + 
                ", memory: " + Util.usedMemory(mb4) + "\n= x = x = x = x = x = x = x = x = x");
    }

    public int metadataTest(String id, String url, List<E> nodes) {
        
        int count = 0;
        
        try{
            
            if(debug) System.out.println("\n" + LocalDateTime.now() + ". " +id+" extracting url: " + url);
            
            final String [] configPaths = {
                SampleConfigPaths.SCHEMA_ARTICLE,
                SampleConfigPaths.OPENGRAPH_CUSTOM,
                SampleConfigPaths.TWITTERCARD_CUSTOM,
                SampleConfigPaths.BASIC
            };
            
            final PropertiesParser parser = new PropertiesParser(jsonParser);
            
            final List<Metadata> delegates = new ArrayList();  
            
            for(String configPath : configPaths) {
                
                final Map config = parser.parse(configPath);
                
                final FilterContext<E> filterContext = this.filterContextProvider
                        .apply(attributeContext, config);
                
                delegates.add(new MetaContent(nodes, filterContext, nodeValueExtractor));
            }

            final Metadata metaData = new MetadataComposite(delegates);
            
            final Predicate<String> isMultiValue = new ArticleMetaNameIsMultiValue();
            
            for(String name : ArticleMetaNames.values()) {
                final Object value;
                if(isMultiValue.test(name)) {
                    final Set set = metaData.getValues(name);
                    value = set.isEmpty() ? null : set;
                }else{
                    value = metaData.getValue(name, null);
                }
                if(value != null) {
                    ++count;
                    if(debug) System.out.println(name + '=' + value);
                }
            }
            
            System.out.println(LocalDateTime.now() + ". " + id + " done extracting " + count + " values.");

        }catch(Exception e) {
            if(debug) {
                e.printStackTrace();
            }else{
                System.err.println(e.toString());
            }
        }
        
        return count;
    }

    public AttributeTestProvider<E> getAttributeContext() {
        return attributeContext;
    }

    public Function<String, List<E>> getNodeExtractor() {
        return nodeExtractor;
    }
}
/**
 * 

    public HtmlDocument buildDocument(Parser parser, RequestBuilder builder, String url) 
            throws MalformedURLException, IOException, ParserException {
        final NodeList allNodes = this.buildNodeList(parser, builder, url);
        final HtmlDocument doc = new HtmlDocumentImpl(url, allNodes);
        return doc;
    }
    
    public NodeList buildNodeList(Parser parser, RequestBuilder builder, String url) 
            throws MalformedURLException, IOException, ParserException {
//        parser.setConnection(builder.url(new URL(url)).build());
        parser.setURL(url);
        final NodeList allNodes = parser.parse(null);
        return allNodes;
    }

    public NodeIterator buildNodes(Parser parser, RequestBuilder builder, String url) 
            throws MalformedURLException, IOException, ParserException {
//        parser.setConnection(builder.url(new URL(url)).build());
        parser.setURL(url);
        return parser.elements();
    }

    public RequestBuilder getRequestBuilder() {
        final RequestBuilder builder = new RequestBuilderImpl();
        builder.charset(StandardCharsets.UTF_8.name());
        return builder;
    }
 * 
 */
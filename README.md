### Use case

```java

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

        final Map map = selector.select(nodes, ArticleMetaNames.values(), Collectors.toMap());
        
        System.out.println("Printing meta tags data for: " + url + "\n" + map);
    }
}
```

### Dependencies

* The api itself has no dependency
* It is designed to be used with any html parser etc library e.g 'jsoup', 'htmlparser' with minimal effort
* The example code above has the following dependencies 

```xml
<dependency>
    <groupId>org.htmlparser</groupId>
    <artifactId>htmlparser</artifactId>
    <version>2.1</version>
</dependency>

<dependency>
    <groupId>com.googlecode.json-simple</groupId>
    <artifactId>json-simple</artifactId>
    <version>1.1.1</version>
</dependency>
```

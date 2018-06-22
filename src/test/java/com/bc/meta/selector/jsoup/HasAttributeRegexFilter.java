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

import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 16, 2018 6:20:03 PM
 */
public class HasAttributeRegexFilter implements Predicate<Element> {

    private final int flags;
    private final String name;
    private final String value;
    private final Pattern namePattern;
    private final Pattern valuePattern;

    public HasAttributeRegexFilter (){
        this("", null);
    }

    public HasAttributeRegexFilter (String attributeNameRegex, String attributeValueRegex){
        this(attributeNameRegex, attributeValueRegex, Pattern.CASE_INSENSITIVE);
    }
    
    public HasAttributeRegexFilter (String attributeNameRegex, String attributeValueRegex, int flags){
        this.name = attributeNameRegex;
        this.value = attributeValueRegex;
        this.flags = flags;
        this.namePattern = name == null ? null : Pattern.compile(name, flags);
        this.valuePattern = value == null ? null : Pattern.compile(value, flags);
    }

    @Override
    public boolean test(Element node){
        
        Attributes attributes = node.attributes();
        
        boolean accept =  false;
        
        if(attributes != null && attributes.size() != 0) {
           
            for(Attribute attribute : attributes) {
                
                if(attribute == null) {
                    continue;
                }

                final String name = attribute.getKey();
                final String value = attribute.getValue();

                if(this.find(name, this.namePattern) && this.find(value, this.valuePattern)) {
                    accept = true;
                    break;
                }
            }
        }

        return accept;
    }
    
    private boolean find(String toFind, Pattern pattern) {
        return pattern == null || toFind == null || toFind.isEmpty() ? false : pattern.matcher(toFind).find();
    }
}

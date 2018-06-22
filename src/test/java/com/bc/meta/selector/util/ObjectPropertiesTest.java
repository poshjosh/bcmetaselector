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

import com.bc.meta.selector.JsonParserImpl;
import com.bc.util.JsonFormat;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * @author Josh
 */
public class ObjectPropertiesTest {

    private final JsonParser jsonParser = new JsonParserImpl();
    
    private final List<String> configFilePaths = Arrays.asList(
            SampleConfigPaths.APP_ARTICLE, SampleConfigPaths.BASIC, 
            SampleConfigPaths.OPENGRAPH_CUSTOM, SampleConfigPaths.SCHEMA_ARTICLE, 
            SampleConfigPaths.SCHEMA_CREATIVEWORK, SampleConfigPaths.SCHEMA_THING, 
            SampleConfigPaths.TWITTERCARD_CUSTOM);
    
    public ObjectPropertiesTest() { }

    /**
     * Test of apply method, of class PropertiesParser.
     */
    @Test
    public void testApply() throws IOException, ParseException {
        
        System.out.println("apply");
        
        final PropertiesParser instance = new PropertiesParser(jsonParser);
        
        for(String path : configFilePaths) {
            
            try{
                
                final File file = new File(path);
                
                final Map props = instance.parse(file.toString(), null);
                
                System.out.println("\n" + file + "\n" + 
                        new JsonFormat(true, true, "  ").toJSONString(props));
                
            }catch(IOException | ParseException e) {
                
                throw e;
            }
        }
    }
}

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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 20, 2018 8:42:57 PM
 */
public class PropertiesParser {

    private transient static final Logger LOG = Logger.getLogger(PropertiesParser.class.getName());

    private final JsonParser jsonParser;
    
    private final String charset;
    
    private final boolean useCache;
    
    private final Map<String, Map> propertiesCache;
    
    private final String propertiesKey = "properties";

    private final String objectHook = "#";
    
    private final String commentPrefix = "!--";

    public PropertiesParser(JsonParser jsonParser) {
        this(jsonParser, StandardCharsets.UTF_8.name(), false);
    }
    
    public PropertiesParser(JsonParser jsonParser, String charset, boolean useCache) {
        this.jsonParser = Objects.requireNonNull(jsonParser);
        this.charset = Objects.requireNonNull(charset);
        this.useCache = useCache;
        this.propertiesCache = useCache ? new HashMap() : Collections.EMPTY_MAP;
    }

    public Map parse(String location) throws IOException, ParseException {
        return this.parse(location, null);
    }
    
    public Map parse(String location, String objectName) throws IOException, ParseException {
        
        final String objKey = this.buildKey(location, objectName);
        
        final Map fromCache = this.propertiesCache.get(objKey);
        
        final Map result;
            
        if(fromCache != null) {
         
            result = fromCache;
            
            LOG.fine(() -> "For: " + objKey + ", FROM CACHE:\n" + fromCache);
            
        }else{
            
            try(InputStream in = this.getStream(location)) {

                final Map config = jsonParser.apply(in, charset);

    //            System.out.println(location + '=' + config.toString().replace(",", "\n"));
                LOG.fine(() -> location + ":\n" + config);

                final Map objectConfig;
                if(objectName != null) {
                    objectConfig = (Map)config.get(objectName);
                    Objects.requireNonNull(objectConfig, "No JSON Object named: " + 
                            objectName + " specified in: " + location);
                }else{
                    final Predicate<String> notComment = (key) -> !key.startsWith(commentPrefix);
                    final Set keys = config.keySet();
                    final Object key = keys.stream().filter(notComment).findFirst().orElse(null);
                    final String noObjectMsg = "No JSON Object specified in: " + location;
                    Objects.requireNonNull(key, noObjectMsg);
                    objectConfig = (Map)config.get(key);
                    Objects.requireNonNull(objectConfig, noObjectMsg);
                }

    //            System.out.println(objKey + ":\n" + objectConfig.toString().replace(",", "\n"));
                LOG.fine(() -> objKey + ":\n" + objectConfig);

                final Map objectProps = (Map)objectConfig.get(propertiesKey);
                
                LOG.fine(() -> objKey + " properties:\n" + objectProps);

                final String parentLocation = (String)objectConfig.get("extends");

                final List<String> siblingLocations = (List<String>)objectConfig.get("include");

                if(parentLocation == null && (siblingLocations == null || siblingLocations.isEmpty())) {
                    
                    Objects.requireNonNull(objectProps, propertiesKey + " not defined for: " + objectName + ", in: " + location);
                    result = objectProps;
                    
                }else{
                    
                    result = new LinkedHashMap();
                    
                    if(parentLocation != null) {
                        this.include(location, objectName, config, parentLocation, result);
                    }
                    
                    if(siblingLocations != null && !siblingLocations.isEmpty()) {
                        for(String siblingLoc : siblingLocations) {
                            this.include(location, objectName, config, siblingLoc, result);
                        }
                    }
                    
                    if(objectProps != null) {
                        result.putAll(objectProps);
                    }
                }

    //            System.out.println(buildKey(location, objectName) + '=' + result.toString().replace(",", "\n"));
                LOG.fine(() -> buildKey(location, objectName) + '=' + result);
            }
            
            if(this.useCache && result != null) {
                this.propertiesCache.put(objKey, result);
            }
        }

        return result == null || result.isEmpty() ?
                Collections.EMPTY_MAP :  
                Collections.unmodifiableMap(result);
    }
    
    public void include(String currentLocation, String objectName, 
            Map config, String locationToInclude, Map addTo) 
            throws IOException, ParseException {
        
        final Map toInclude = this.getPropertiesToInclude(config, locationToInclude);
        
        Objects.requireNonNull(toInclude, "Failed not locate: " + 
                locationToInclude + ", referenced from within: " + currentLocation);
        
        addTo.putAll(toInclude);
    }
    
    public Map getPropertiesToInclude(Map config, String locationToInclude) 
            throws IOException, ParseException {
        final Map toInclude;
        if(locationToInclude.startsWith(objectHook)) {
            toInclude = (Map)config.get(locationToInclude);
        }else if(!locationToInclude.contains(objectHook)) {    
            toInclude = this.parse(locationToInclude, null);
        }else{
            final int pivot = locationToInclude.lastIndexOf(objectHook);
            final String parentFileName = locationToInclude.substring(0, pivot);
            final String parentObjectName = locationToInclude.substring(pivot + 1);
            toInclude = this.parse(parentFileName, parentObjectName);
        }
        LOG.finer(() -> "Location to include: " + locationToInclude + ", properties:\n" + toInclude);
        return toInclude;
    }

    public InputStream getStream(String location) throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
        if(in == null) {
            try{
                in = new FileInputStream(location);
            }catch(IOException e) {
                try{
                    in = new URL(location).openStream();
                }catch(MalformedURLException mue) {
                    throw e;
                }
            }
        }
        return in;
    }
    
    public String buildKey(String location, String objectName) {
        Objects.requireNonNull(location);
        if(objectName == null) {
            return location;
        }else{
            return location + objectHook + objectName;
        } 
    }
}

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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 20, 2018 8:42:57 PM
 */
public class PropertiesParser {

    private transient static final Logger LOG = Logger.getLogger(PropertiesParser.class.getName());
    
    public static class DefaultStreamProvider implements Function<String, InputStream> {
        
        @Override
        public InputStream apply(String location) {
            
            try{
                
                final ClassLoader cl = Thread.currentThread().getContextClassLoader(); 
                InputStream in = cl.getResourceAsStream(location);
                
                if(LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "ClassLoader: {0}, location: {1}, input stream: {2}",
                            new Object[]{cl, location, in});
                }
                
                if(in == null) {
                    try{
                        in = new FileInputStream(location);
                        if(LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "Location: {0}, file input stream: {1}",
                                    new Object[]{location, in});
                        }
                    }catch(IOException e) {
                        try{
                            in = new URL(location).openStream();
                            if(LOG.isLoggable(Level.FINE)) {
                                LOG.log(Level.FINE, "Location: {0}, url input stream: {1}",
                                        new Object[]{location, in});
                            }
                        }catch(MalformedURLException mue) {
                            throw e;
                        }
                    }
                }

                return in;

            }catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final Function<String, InputStream> streamProvider;
    
    private final JsonParser jsonParser;
    
    private final String charset;
    
    private final boolean useCache;
    
    private final Map<String, Map> propertiesCache;
    
    private final String propertiesKey = "properties";

    private final String objectHook = "#";
    
    private final String commentPrefix = "!--";

    public PropertiesParser(JsonParser jsonParser) {
        this(new DefaultStreamProvider(), jsonParser, StandardCharsets.UTF_8.name(), false);
    }
    
    public PropertiesParser(Function<String, InputStream> streamProvider, 
            JsonParser jsonParser, String charset, boolean useCache) {
        this.streamProvider = Objects.requireNonNull(streamProvider);
        this.jsonParser = Objects.requireNonNull(jsonParser);
        this.charset = Objects.requireNonNull(charset);
        this.useCache = useCache;
        this.propertiesCache = useCache ? new HashMap() : Collections.EMPTY_MAP;
    }

    public Map parse(String location) throws IOException, ParseException {
        return this.parse(location, null);
    }
    
    public Map parse(final String location, final String objectName) throws IOException, ParseException {
        
        final String objKey = this.buildKey(location, objectName);
        
        final Map fromCache = this.propertiesCache.get(objKey);
        
        final Map result;
            
        if(fromCache != null) {
         
            result = fromCache;
            
            LOG.finer(() -> "For: " + objKey + ", FROM CACHE:\n" + fromCache);
            
        }else{
            
            try(InputStream in = this.streamProvider.apply(location)) {

                final Map config = jsonParser.apply(in, charset);

    //            System.out.println(location + '=' + config.toString().replace(",", "\n"));
                LOG.finer(() -> location + ":\n" + config);

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
                LOG.finer(() -> objKey + ":\n" + objectConfig);

                final Map objectProps = (Map)objectConfig.get(propertiesKey);
                
                LOG.finer(() -> objKey + " properties:\n" + objectProps);

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

    //            System.out.println(objKey + '=' + result.toString().replace(",", "\n"));
                LOG.fine(() -> objKey + '=' + result);
                
            }catch(RuntimeException e) {
                
                if(e.getCause() instanceof IOException) {
                    
                    LOG.warning(e.toString());
                    
                    throw (IOException)e.getCause();
                    
                }else{
                    
                    throw e;
                }
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
    
    public String buildKey(String location, String objectName) {
        Objects.requireNonNull(location);
        if(objectName == null) {
            return location;
        }else{
            return location + objectHook + objectName;
        } 
    }
}

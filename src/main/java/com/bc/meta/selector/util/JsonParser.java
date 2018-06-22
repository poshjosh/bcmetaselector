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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 20, 2018 9:43:59 PM
 */
public interface JsonParser extends BiFunction<InputStream, String, Map> {
    
    @Override
    default Map apply(InputStream in, String charset) {
        try{
            return this.parse(in, charset);
        }catch(IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    default Map parse(InputStream in, String charset) throws IOException, ParseException {
        try(Reader reader = new InputStreamReader(in, charset)){
            return this.parse(reader);
        }
    }
    
    Map parse(Reader reader) throws IOException, ParseException;
}

/*
 * Copyright 2016 NUROX Ltd.
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

package com.bc.meta;

import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 5, 2016 8:17:21 PM
 */
public interface Metadata {
//@todo add getGeo or getPlacename,getPosition,getRegion,getICBM
//<meta name="geo.placename" content="Lagos island, Lagos, Nigeria"/>
//<meta name="geo.position" content="6.4548790;3.4245980"/>
//<meta name="geo.region" content="NG-Lagos"/>
    Metadata EMPTY = new Metadata() {
        @Override
        public String getValue(String name, String outputIfNone) {
            return outputIfNone;
        }
        @Override
        public Set<String> getValues(String name) {
            return Collections.EMPTY_SET;
        }
    };
    
    default String getValue(String [] nameOptions, String outputIfNone) {
        String value = null;
        for(String name : nameOptions) {
            value = this.getValue(name, null);
            if(value != null) {
                break;
            }
        }
        return value == null ? outputIfNone : value;
    }
    
    String getValue(String name, String outputIfNone);
    
    default Set<String> getValues(String [] nameOptions) {
        Set<String> values = Collections.EMPTY_SET;
        for(String name : nameOptions) {
            values = this.getValues(name);
            if(values != null && !values.isEmpty()) {
                break;
            }
        }
        return values;
    }

    Set<String> getValues(String name);

    default String getAsSingle(String name, String outputIfNone) {
        return this.combineValues(name, outputIfNone);
    }
    
    default String combineValues(String name, String outputIfNone) {
        return this.toString(this.getValues(name), outputIfNone);
    }
    
    default String toString(Collection<String> c, String outputIfNone) {
        String output;
        if(c == null || c.isEmpty()) {
            output = outputIfNone;
        }else{
            StringBuilder builder = new StringBuilder();
            int i = 0;
            for(String s:c) {
                builder.append(s);
                if(i < c.size() -1) {
                    builder.append(',').append(' ');
                }
                ++i;
            }
            output = builder.toString();
        }
        return output == null ? outputIfNone : output;
    }

    default Locale getLocale(String [] nameOptions, Locale outputIfNone) throws ParseException {
        final String localeStr = this.getValue(nameOptions, null);
        return localeStr == null ? outputIfNone : this.toLocale(localeStr);
    }
    
    default Locale getLocale(String name, Locale outputIfNone) throws ParseException {
        final String localeStr = this.getValue(name, null);
        return localeStr == null ? outputIfNone : this.toLocale(localeStr);
    }

    /**
     * <p>Source: http://www.java2s.com/Code/Java/Data-Type/ConvertsaStringtoaLocale.htm</p>
     * 
     * <p>Converts a String to a Locale.</p>
     *
     * <p>This method takes the string format of a locale and creates the
     * locale object from it.</p>
     *
     * <pre>
     *   LocaleUtils.toLocale("en")         = new Locale("en", "")
     *   LocaleUtils.toLocale("en_GB")      = new Locale("en", "GB")
     *   LocaleUtils.toLocale("en_GB_xxx")  = new Locale("en", "GB", "xxx")   (#)
     * </pre>
     *
     * <p>(#) The behaviour of the JDK variant constructor changed between JDK1.3 and JDK1.4.
     * In JDK1.3, the constructor upper cases the variant, in JDK1.4, it doesn't.
     * Thus, the result from getVariant() may vary depending on your JDK.</p>
     *
     * <p>This method validates the input strictly.
     * The language code must be lowercase.
     * The country code must be uppercase.
     * The separator must be an underscore.
     * The length must be correct.
     * </p>
     *
     * @param str  the locale String to convert, null returns null
     * @return a Locale, null if null input
     * @throws ParseException if the string is an invalid format
     */
    default Locale toLocale(String str) throws ParseException {
        Objects.requireNonNull(str);
        int len = str.length();
        if (len != 2 && len != 5 && len < 7) {
            throw new ParseException("Invalid locale format: " + str, 0);
        }
        char ch0 = str.charAt(0);
        char ch1 = str.charAt(1);
        if (ch0 < 'a' || ch0 > 'z' || ch1 < 'a' || ch1 > 'z') {
            throw new ParseException("Invalid locale format: " + str, 0);
        }
        if (len == 2) {
            return new Locale(str, "");
        } else {
            if (str.charAt(2) != '_') {
                throw new ParseException("Invalid locale format: " + str, 2);
            }
            char ch3 = str.charAt(3);
            if (ch3 == '_') {
                return new Locale(str.substring(0, 2), "", str.substring(4));
            }
            char ch4 = str.charAt(4);
            if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
                throw new ParseException("Invalid locale format: " + str, 4);
            }
            if (len == 5) {
                return new Locale(str.substring(0, 2), str.substring(3, 5));
            } else {
                if (str.charAt(5) != '_') {
                    throw new ParseException("Invalid locale format: " + str, 5);
                }
                return new Locale(str.substring(0, 2), str.substring(3, 5), str.substring(6));
            }
        }
    }
}

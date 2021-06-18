package com.ibm.pa.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NOTE All HTTP GET PAIRS query parameters for WMS, WCS, ... must be upper
 * case. I decided to do case insensitive match on get(key) while I get things
 * going. But GET sematics are case preserving. and should follow the pairs
 * convention of being upper case
 * 
 * Note, adding new value to existing key adds the new value to the String[]. If
 * the intent is replace, then use the put() or delete()/put() of the super
 * class
 * 
 * 
 */
public class HttpRequestParameterMap extends HashMap<String, String[]> {
    private Boolean caseInsensitive = true;

    public HttpRequestParameterMap() {
    }

    public HttpRequestParameterMap(Map<String, String[]> map) {
        super(map);
    }

    public String get(String key) {
        String[] val = getArray(key);
        return val == null ? null : val[0];
    }

    public String[] getArray(String key) {
        if (caseInsensitive) {
            for (Map.Entry<String, String[]> entry : entrySet()) {
                String paramName = entry.getKey();
                if (key.equalsIgnoreCase(paramName))
                    return entry.getValue();
            }
        } else {
            return super.get(key);
        }

        return null;
    }

    /**
     * if key exists appends value to String[]. Otherwise, add new key, value
     * 
     * @param key
     * @param value
     */
    public void add(final String key, final String value) {
        String[] current = getArray(key);
        if (current != null) {
            List<String> tmp = new ArrayList<String>(Arrays.asList(current));
            tmp.add(value);
            super.put(key, tmp.toArray(new String[0]));
        } else
            super.put(key, new String[] { value });
    }

    public Boolean isCaseInsensitive() {
        return this.caseInsensitive;
    }

    public Boolean getCaseInsensitive() {
        return this.caseInsensitive;
    }

    public void setCaseInsensitive(Boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }



    public static void main(String[] args) {

    }
}

package com.ibm.pa.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
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
public class PairsHttpRequestParamMap extends HashMap<String, String[]> {
    private Boolean caseSensitive = false;

    public PairsHttpRequestParamMap() {
    }

    public PairsHttpRequestParamMap(Map<String, String[]> map) {
        super(map);
    }

    public PairsHttpRequestParamMap(HashMap<String, String> map) {
        for (Entry<String, String> e : map.entrySet())
            super.put(e.getKey(), new String[] { e.getValue() });
    }

    public String get(String key) {
        String[] val = getArray(key);
        return val == null ? null : val[0];
    }

    public String[] getArray(String key) {
        if (!caseSensitive) {
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

    public Boolean isCaseSensitive() {
        return this.caseSensitive;
    }

    public Boolean getCaseSensitive() {
        return this.caseSensitive;
    }

    public void setCaseSensitive(Boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public static void main(String[] args) {
        testMe();
    }

    private static void testMe() {
        Map<String, String[]> base = new HashMap<>();

        base.put("1a", new String[] { "1a", "1b" });
        base.put("2a", new String[] { "2a", "2b" });
        PairsHttpRequestParamMap map = new PairsHttpRequestParamMap(base);

        for (Entry<String, String[]> e : base.entrySet()) {
            System.out.println("base key: " + e.getKey() + ", value: " + Arrays.toString(e.getValue()));
        }

        for (Entry<String, String[]> e : map.entrySet()) {
            System.out.println("map key: " + e.getKey() + ", value: " + Arrays.toString(e.getValue()));
        }

        String[] sa = map.getArray("1A");
        String sv = map.get("1A");
        System.out.println("insensed key: 1A, str: " + sv + ", array: " + Arrays.toString(sa));
    }
}

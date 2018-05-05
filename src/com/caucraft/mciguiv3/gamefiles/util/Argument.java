package com.caucraft.mciguiv3.gamefiles.util;

import java.util.List;
import java.util.Map;

/**
 *
 * @author caucow
 */
public class Argument {
    
    String arg;
    List<Rule> rules;
    
    public Argument(String arg, List<Rule> rules) {
        this.arg = arg;
        this.rules = rules;
    }
    
    public boolean passRules(Map<String, String> properties) {
        if (rules == null || rules.isEmpty()) {
            return true;
        }
        boolean pass = false;
        for (Rule r : rules) {
            pass = r.test(properties, pass);
        }
        return pass;
    }
}

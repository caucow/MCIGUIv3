package com.caucraft.mciguiv3.gamefiles.util;

import java.util.Map;

/**
 *
 * @author caucow
 */
public interface Rule {
    public boolean test(Map<String, String> props, boolean passing);
}

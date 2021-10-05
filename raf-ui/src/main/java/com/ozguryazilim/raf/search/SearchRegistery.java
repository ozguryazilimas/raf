package com.ozguryazilim.raf.search;

import com.google.common.base.CaseFormat;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyasc34
 */
public class SearchRegistery {

    private static final Logger LOG = LoggerFactory.getLogger(SearchRegistery.class);

    private static List<String> searchPanels = new ArrayList<>();

    public static List<String> getSearchPanels() {
        return searchPanels;
    }

    public static void register(Class<? extends SearchPanelController> spc) {
        searchPanels.add(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, spc.getSimpleName()));
        LOG.info("SearchPanelController {} registered", spc.getSimpleName());
    }
}

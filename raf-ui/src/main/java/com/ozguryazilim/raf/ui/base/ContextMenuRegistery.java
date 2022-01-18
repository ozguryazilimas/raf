package com.ozguryazilim.raf.ui.base;

import com.google.common.base.CaseFormat;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextMenuRegistery {

    private static final Logger LOG = LoggerFactory.getLogger(ContextMenuRegistery.class);

    private static final Map<String, ContextMenu> contextMenus = new HashMap<>();

    public static void register(String name, ContextMenu cm) {
        name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
        contextMenus.put(name, cm);

        LOG.info("ContextMenu Registered : {}", name);
    }

    /**
     * Geriye kayıtlı tüm menü itemların CDI instance'larını döner.
     *
     * @return
     */
    public static List<AbstractContextMenuItem> getContextMenus() {
        List<AbstractContextMenuItem> result = new ArrayList<>();

        for (String pn : contextMenus.keySet()) {
            result.add((AbstractContextMenuItem) BeanProvider.getContextualReference(pn, true));
        }

        return result;
    }
}

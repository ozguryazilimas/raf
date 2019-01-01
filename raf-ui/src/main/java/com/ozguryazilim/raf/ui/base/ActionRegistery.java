package com.ozguryazilim.raf.ui.base;

import com.google.common.base.CaseFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
public class ActionRegistery {

    private static final Logger LOG = LoggerFactory.getLogger(ActionRegistery.class);

    private static final Map<String, Action> actions = new HashMap<>();

    public static void register(String name, Action a) {
        name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
        actions.put(name, a);

        LOG.info("Action Registered : {}", name);
    }

    /**
     * Geriye kayıtlı tüm action'ların CDI instance'larını döner.
     * 
     * @return 
     */
    public static List<AbstractAction> getActions() {
        List<AbstractAction> result = new ArrayList<>();

        for (String pn : actions.keySet()) {
            result.add((AbstractAction) BeanProvider.getContextualReference(pn, true));
        }
        
        return result;
    }
}

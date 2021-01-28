package com.ozguryazilim.raf.executors;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

@Singleton
public class BackgroundJobManager {

    private static final Logger LOG = LoggerFactory.getLogger(BackgroundJobManager.class);

    @Inject
    private RafService rafService;

    @Schedule(hour = "0", minute = "0", second = "0", persistent = false)
    public void dailyJob() {
        try {
            rafService.reGenerateObjectPreviews(rafService.getRafCollectionForAllNode().getItems());
        } catch (RafException ex) {
            LOG.error("daily raf preview generation error ", ex);
        }
    }

}

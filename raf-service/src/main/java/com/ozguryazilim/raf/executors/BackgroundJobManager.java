package com.ozguryazilim.raf.executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Schedule;
import javax.ejb.Singleton;

@Singleton
public class BackgroundJobManager {

    private static final Logger LOG = LoggerFactory.getLogger(BackgroundJobManager.class);

    @Schedule(hour = "0", minute = "0", second = "0", persistent = false)
    public void dailyJob() {

    }

}

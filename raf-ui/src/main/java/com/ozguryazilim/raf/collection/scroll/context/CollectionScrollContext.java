package com.ozguryazilim.raf.collection.scroll.context;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.collection.scroll.ScrollPosition;
import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.core.api.scope.WindowScoped;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@WindowScoped
public class CollectionScrollContext implements Serializable {

    private Map<String, ScrollPosition> scrollPositionByPath;

    @Inject
    private RafContext rafContext;

    @PostConstruct
    void init() {
        scrollPositionByPath = new HashMap<>();
    }

    public void scrollPositionChanged(@Observes ScrollPositionChangedEvent scrollPositionChangedEvent) {
        setScrollPositionOfPath(rafContext.getCollection().getPath(), scrollPositionChangedEvent.getScrollPosition());
    }

    public ScrollPosition getScrollPositionByPath(String path) {
        scrollPositionByPath.putIfAbsent(path, new ScrollPosition(0L, 0L));
        return scrollPositionByPath.get(path);
    }

    private void setScrollPositionOfPath(String path, ScrollPosition scrollPosition) {
        if (StringUtils.isBlank(path)) {
            return;
        }
        ScrollPosition currentScrollPosition = getScrollPositionByPath(path);

        if (scrollPosition.getxScroll() != null)  {
            currentScrollPosition.setxScroll(scrollPosition.getxScroll());
        }

        if (scrollPosition.getyScroll() != null)  {
            currentScrollPosition.setyScroll(scrollPosition.getyScroll());
        }

        scrollPositionByPath.put(path, currentScrollPosition);
    }

    public Long getXScroll() {
        return getScrollPositionByPath(rafContext.getCollection().getPath()).getxScroll();
    }

    public Long getYScroll() {
        return getScrollPositionByPath(rafContext.getCollection().getPath()).getyScroll();
    }

}

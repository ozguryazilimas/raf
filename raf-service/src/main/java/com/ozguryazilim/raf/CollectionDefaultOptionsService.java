package com.ozguryazilim.raf;

import com.ozguryazilim.mutfak.kahve.Kahve;
import com.ozguryazilim.mutfak.kahve.KahveKey;
import com.ozguryazilim.mutfak.kahve.annotations.UserAware;
import com.ozguryazilim.raf.enums.SortType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.Serializable;

@ApplicationScoped
public class CollectionDefaultOptionsService implements Serializable {

    @Inject
    @UserAware
    private Kahve kahve;

    public SortType getDefaultSortType() {
        return kahve.get(OptionKeys.DEFAULT_SORT_TYPE).getAsEnum(SortType.class);
    }

    public boolean getDefaultDescSort() {
        return kahve.get(OptionKeys.DEFAULT_DESC_SORT).getAsBoolean();
    }

    public void setDefaultSortType(SortType sortType) {
        kahve.put(OptionKeys.DEFAULT_SORT_TYPE, sortType);
    }

    public void setDefaultDescSort(boolean ascSort) {
        kahve.put(OptionKeys.DEFAULT_DESC_SORT, ascSort);
    }

    private enum OptionKeys implements KahveKey {
        DEFAULT_SORT_TYPE("raf.sortBy", SortType.DATE_DESC.toString()),
        DEFAULT_DESC_SORT("raf.descSort", "false");

        private final String key;
        private final String defaultValue;

        OptionKeys(String key, String defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultValue() {
            return defaultValue;
        }
    }

}

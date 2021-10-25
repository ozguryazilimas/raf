package com.ozguryazilim.raf.dashlets;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.entities.RafDefinition;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 *
 * @author evren.cengiz
 */
public class LazyRafDefinitionDataModel extends LazyDataModel<RafDefinition> {

    private MyRafsDashlet myRafsDashlet;

    public LazyRafDefinitionDataModel(MyRafsDashlet myRafsDashlet) {
        this.myRafsDashlet = myRafsDashlet;
    }

    @Override
    public List<RafDefinition> load(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        List<RafDefinition> result = myRafsDashlet.getRafs(first, pageSize);
        this.setRowCount(result.size());
        return result;
    }

    @Override
    public List<RafDefinition> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        List<RafDefinition> result = myRafsDashlet.getRafs(first, pageSize);
        this.setRowCount(result.size());
        return result;
    }

}

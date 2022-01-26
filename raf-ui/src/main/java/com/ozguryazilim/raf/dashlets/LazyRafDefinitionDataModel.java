package com.ozguryazilim.raf.dashlets;

import com.ozguryazilim.raf.entities.RafDefinition;
import java.util.List;
import java.util.Map;
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
        this.setRowCount(myRafsDashlet.getTotalRowCount());
        return result;
    }

    @Override
    public List<RafDefinition> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        List<RafDefinition> result = myRafsDashlet.getRafs(first, pageSize);
        this.setRowCount(myRafsDashlet.getTotalRowCount());
        return result;
    }

}

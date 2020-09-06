package com.ozguryazilim.raf.converters;

import com.ozguryazilim.raf.entities.RafDefinition;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import java.util.List;
import java.util.Optional;

public class RafDefinitionConverter implements Converter {

    private final List<RafDefinition> rafDefinitionList;

    public RafDefinitionConverter(List<RafDefinition> rafDefinitionList) {
        this.rafDefinitionList = rafDefinitionList;
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        RafDefinition rafDefinition = null;
            Optional<RafDefinition> optionalRafDefinition = rafDefinitionList.stream().filter(raf -> raf.getCode().equals(value)).findFirst();
            if (optionalRafDefinition.isPresent()) {
                rafDefinition = optionalRafDefinition.get();
            }
        return rafDefinition;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (!(value instanceof String)) {
            return ((RafDefinition) value).getName() + "";
        }
        return "";
    }

}

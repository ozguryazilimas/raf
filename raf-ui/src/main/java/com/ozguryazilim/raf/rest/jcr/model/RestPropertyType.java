package com.ozguryazilim.raf.rest.jcr.model;

import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.OnParentVersionAction;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.modeshape.common.util.StringUtil;

/**
 * A REST representation of a {@link PropertyDefinition}
 *
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
public final class RestPropertyType implements JSONAble {

    private final String declaringNodeTypeName;
    private final boolean isAutoCreated;
    private final boolean isMandatory;
    private final boolean isProtected;
    private final String onParentVersion;
    private final String name;
    private final boolean isMultiple;
    private final boolean isFullTextSearchable;
    private final String requiredType;

    /**
     * Creates a new property definition.
     *
     * @param definition a {@code non-null} {@link PropertyDefinition}
     */
    public RestPropertyType( PropertyDefinition definition ) {
        this.name = definition.getName();
        this.requiredType = org.modeshape.jcr.api.PropertyType.nameFromValue(definition.getRequiredType());
        NodeType declaringNodeType = definition.getDeclaringNodeType();
        this.declaringNodeTypeName = declaringNodeType == null ? null : declaringNodeType.getName();
        this.isAutoCreated = definition.isAutoCreated();
        this.isMandatory = definition.isMandatory();
        this.isProtected = definition.isProtected();
        this.isFullTextSearchable = definition.isFullTextSearchable();
        this.onParentVersion = OnParentVersionAction.nameFromValue(definition.getOnParentVersion());
        this.isMultiple = definition.isMultiple();
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject content = new JSONObject();
        content.put("requiredType", this.requiredType);
        if (!StringUtil.isBlank(declaringNodeTypeName)) {
            content.put("declaringNodeTypeName", this.declaringNodeTypeName);
        }
        content.put("mandatory", isMandatory);
        content.put("multiple", isMultiple);
        content.put("autocreated", isAutoCreated);
        content.put("protected", isProtected);
        content.put("fullTextSearchable", isFullTextSearchable);
        content.put("onParentVersion", onParentVersion);

        JSONObject object = new JSONObject();
        object.put(name, content);
        return object;
    }
}

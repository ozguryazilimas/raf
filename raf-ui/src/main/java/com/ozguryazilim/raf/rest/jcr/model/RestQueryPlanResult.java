package com.ozguryazilim.raf.rest.jcr.model;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * A REST representation of a query plan.
 * 
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
public final class RestQueryPlanResult implements JSONAble, Stringable {
    private final String plan;
    private final String language;
    private final String statement;
    private final String aqm;

    /**
     * Creates a result with the specified plan
     * 
     * @param plan the plan
     * @param statement the original query statement
     * @param language the query language
     * @param aqm the abstract query model for the query
     */
    public RestQueryPlanResult( String plan,
                                String statement,
                                String language,
                                String aqm ) {
        this.plan = plan;
        this.statement = statement;
        this.language = language;
        this.aqm = aqm;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("statement", statement);
        result.put("language", language);
        result.put("abstractQueryModel", aqm);
        String[] planParts = plan.split("\\n");
        JSONArray planArray = new JSONArray();
        for (String part : planParts) {
            planArray.put(part);
        }
        result.put("queryPlan", planArray);
        // result.put("queryPlan", plan);
        return result;
    }

    @Override
    public String asString() {
        return plan;
    }

}

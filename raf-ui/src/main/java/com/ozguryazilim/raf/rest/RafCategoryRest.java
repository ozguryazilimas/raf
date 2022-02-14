package com.ozguryazilim.raf.rest;

import com.ozguryazilim.raf.category.RafCategoryRepository;
import com.ozguryazilim.raf.entities.RafCategory;
import com.ozguryazilim.telve.rest.ext.Logged;
import com.ozguryazilim.telve.utils.TreeUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Logged
@RequiresPermissions("admin")
@Path("/api/category")
public class RafCategoryRest {

    private static final Logger LOG = LoggerFactory.getLogger(RafCategoryRest.class);

    @Inject
    private RafCategoryRepository rafCategoryRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, String>> getAllCategories() {
        List<RafCategory> categories = rafCategoryRepository.findAll();
        return getCategoryList(categories);
    }

    @POST()
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCategory(@FormParam("code") String code,
                                   @FormParam("name") String name,
                                   @FormParam("info") String info,
                                   @FormParam("parent") String parent) {

        try {
            RafCategory category = new RafCategory();
            category.setActive(Boolean.TRUE);
            category.setCode(code);
            category.setName(name);
            category.setInfo(info);
            if (parent != null && !parent.isEmpty()) {
                category.setParent(rafCategoryRepository.findByCode(parent).get(0));
            }
            category.setPath(TreeUtils.getNodeIdPath(category));
            rafCategoryRepository.save(category);
        } catch (Exception e) {
            LOG.error(String.format("Category Create Error - %s", code), e);
            return Response.status(Response.Status.CREATED).entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }

    private List<Map<String, String>> getCategoryList(List<RafCategory> categories) {
        List<Map<String, String>> groupList = new ArrayList<>();
        for (RafCategory rafCategory : categories)
            groupList.add(getCategoryInfo(rafCategory));
        return groupList;
    }

    private Map<String, String> getCategoryInfo(RafCategory category) {
        Map<String, String> categoryInfo = new HashMap<>();
        categoryInfo.put("code", category.getCode());
        categoryInfo.put("name", category.getName());
        categoryInfo.put("info", category.getInfo());
        RafCategory parent = category.getParent();
        categoryInfo.put("parent", parent != null ? parent.getCode() : "");
        return categoryInfo;
    }

}
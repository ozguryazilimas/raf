package com.ozguryazilim.raf.util;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractAction;

import java.util.Collections;
import java.util.List;

public class RafContextUtils {

    /**
     * Executes action with temporary selected items, then retains old selected items.
     * @param ctx RafContext
     * @param action Action
     */
    public static void executeActionWithTemporarySelectedItem(List<RafObject> objects, RafContext ctx, AbstractAction action) {
        List<RafObject> oldSelectedItems = ctx.getSeletedItems();

        ctx.setSeletedItems(objects);
        action.execute();
        ctx.setSeletedItems(oldSelectedItems);
    }

    /**
     * Executes action if it's selected, else executes with temporary selected item context
     * @param object Relevant rafObject
     * @param ctx RafContext
     * @param action Action
     */
    public static void executeActionWithTempSelectedItemsIfNotSelected(RafObject object, RafContext ctx, AbstractAction action) {
        if (ctx.getSeletedItems().contains(object)) {
            action.execute();
        } else {
            executeActionWithTemporarySelectedItem(Collections.singletonList(object), ctx, action);
        }
    }

    /**
     * Null safe isSelectedItem impl.
     * @param object Relevant rafObject
     * @param ctx RafContext
     */
    public static boolean isSelected(RafObject object, RafContext ctx) {
        return object != null && ctx.getSeletedItems().contains(object);
    }
}

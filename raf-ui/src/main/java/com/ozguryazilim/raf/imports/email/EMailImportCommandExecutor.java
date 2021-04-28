package com.ozguryazilim.raf.imports.email;

import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.category.RafCategoryService;
import com.ozguryazilim.raf.tag.TagSuggestionService;
import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import javax.inject.Inject;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@CommandExecutor(command = EMailImportCommand.class)
public class EMailImportCommandExecutor extends AbstractCommandExecuter<EMailImportCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(EMailImportCommandExecutor.class);

    @Inject
    RafCategoryService categoryService;

    @Inject
    RafService rafService;

    @Inject
    TagSuggestionService tagService;

    @Override
    public void execute(EMailImportCommand command) {

        LOG.debug("EMail RAF Import Start");
        callJEXL(command);
    }

    void callJEXL(EMailImportCommand command) {
        LOG.info("E-mail importer jexl command executing.");
        JexlEngine jexl = new JexlBuilder().create();
        JexlScript e = jexl.createScript(command.getJexlExp());
        JexlContext jc = new MapContext();
        RafEMailImporter importer = new RafEMailImporter(rafService, categoryService, tagService);
        jc.set("rafService", rafService);
        jc.set("message", importer.parseEmail(command.getEml()));
        jc.set("importer", importer);
        Object o = e.execute(jc);
        LOG.debug("E-mail importer jexl result. {}", o);
        LOG.info("E-mail importer jexl command executed.");
    }
}

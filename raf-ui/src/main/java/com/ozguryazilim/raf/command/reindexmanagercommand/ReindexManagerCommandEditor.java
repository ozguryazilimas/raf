package com.ozguryazilim.raf.command.reindexmanagercommand;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.imports.RafCommandPages;
import com.ozguryazilim.raf.jcr.ModeShapeRepositoryFactory;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditor;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditorBase;
import org.apache.commons.lang3.StringUtils;
import org.modeshape.jcr.RepositoryConfiguration;
import org.modeshape.jcr.index.elasticsearch.client.EsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@CommandEditor(command = ReindexManagerCommand.class, page = RafCommandPages.ReindexManagerCommandEditor.class)
public class ReindexManagerCommandEditor extends CommandEditorBase<ReindexManagerCommand> {

    Logger LOG = LoggerFactory.getLogger(ReindexManagerCommandEditor.class);

    @Inject
    RafService rafService;

    @Override
    public ReindexManagerCommand createNewCommand() {
        return new ReindexManagerCommand(StringUtils.EMPTY);
    }

    public List<EsIndexStatus> getIndexStatus() throws RafException {
            Optional<RepositoryConfiguration.Component> optionalComponent = ModeShapeRepositoryFactory.getRepositoryConfiguration().getIndexProviders().stream()
                    .filter(component -> "org.modeshape.jcr.index.elasticsearch.EsIndexProvider".equals(component.getClassname()))
                    .findFirst();

            if (optionalComponent.isPresent()) {
                RepositoryConfiguration.Component component = optionalComponent.get();
                EsClient esClient = new EsClient(
                        component.getDocument().getString(RepositoryConfiguration.FieldName.HOST, "localhost"),
                        component.getDocument().getInteger(RepositoryConfiguration.FieldName.PORT, 9200)
                );

                return rafService.getIndexDefinitions().entrySet().stream()
                        .map(entry -> {
                            try {
                                String workspaceName = ModeShapeRepositoryFactory.getRepositoryConfiguration().getDefaultWorkspaceName();
                                long dataCount = esClient.count((entry.getKey().toLowerCase(Locale.ROOT) + "-" + workspaceName), "");
                                return new EsIndexStatus(entry.getKey(), entry.getValue().isEnabled(), dataCount);
                            } catch (IOException e) {
                                LOG.warn("Could not get data count of index {}", entry.getKey());
                                return new EsIndexStatus(entry.getKey(), entry.getValue().isEnabled(), -1);
                            }
                        })
                        .collect(Collectors.toList());
            }

        return Collections.emptyList();

    }

}

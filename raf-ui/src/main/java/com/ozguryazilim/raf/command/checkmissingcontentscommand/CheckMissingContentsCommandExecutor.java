package com.ozguryazilim.raf.command.checkmissingcontentscommand;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.jcr.RafModeshapeRepository;
import com.ozguryazilim.telve.channel.email.EmailChannel;
import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import com.ozguryazilim.telve.messages.Messages;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.modeshape.jcr.value.binary.BinaryStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CommandExecutor(command = CheckMissingContentsCommand.class)
public class CheckMissingContentsCommandExecutor extends AbstractCommandExecuter<CheckMissingContentsCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(CheckMissingContentsCommandExecutor.class);
    private static final String NODE_FILE = "nt:file";
    private static final String CONTENT_NODE_NAME = "jcr:content";
    private static final String DATA_PROP_NAME = "jcr:data";

    @Inject
    private RafModeshapeRepository rafModeshapeRepository;

    @Inject
    private EmailChannel emailChannel;

    @Override
    public void execute(CheckMissingContentsCommand command) {
        List<String> nodePathsWithMissingContents = new ArrayList<>();

        LOG.info("Check Missing Contents Job started!");
        long startMs = System.currentTimeMillis();

        try {
            NodeIterator it = rafModeshapeRepository.getAllNodesWithNodeType(NODE_FILE);

            while (it.hasNext()) {
                Node currentNode = it.nextNode();
                if (!isNodeContainsContent(currentNode)) {
                    nodePathsWithMissingContents.add(currentNode.getPath() + ":" + currentNode.getIdentifier());
                }
            }

        } catch (RafException | RepositoryException ex) {
            LOG.error("Error while checking contents", ex);
        }

        nodePathsWithMissingContents = nodePathsWithMissingContents.stream()
                .filter(nodePath -> !nodePath.startsWith("/PROCESS"))
                .collect(Collectors.toList());

        if (!nodePathsWithMissingContents.isEmpty()) {
            sendEmailWithMissingContentsInformation(nodePathsWithMissingContents, command.getEmail());
            LOG.info("\nMissing contents:\n" + String.join("\n", nodePathsWithMissingContents));
        }

        LOG.info("Check Missing Contents Job end! Took {}s", DurationFormatUtils.formatDuration(System.currentTimeMillis() - startMs, "ss.SSS"));
    }

    public boolean isNodeContainsContent(Node node) {
        try {
            boolean hasContentNode = node.hasNode(CONTENT_NODE_NAME);
            if (hasContentNode) {
                Node contentNode = node.getNode(CONTENT_NODE_NAME);
                if (contentNode.hasProperty(DATA_PROP_NAME)) {
                    try {
                        contentNode.getProperty(DATA_PROP_NAME).getBinary().getStream();
                    } catch (BinaryStoreException ex) {
                        return false;
                    }
                }
            }
            return false;
        } catch (RepositoryException e) {
            LOG.error(e.getMessage());
            return false;
        }
    }

    private void sendEmailWithMissingContentsInformation(List<String> nodes, String email) {
        Map<String, Object> params = new HashMap<>();

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        params.put("date", date);
        params.put("messageClass", "COMMAND_CHECK_MISSING_CONTENTS");
        params.put("nodes", nodes);

        String subject = ConfigResolver.getPropertyValue("app.title") + " " + date + " " + Messages.getMessage("email.subject.CheckMissingContentsCommand.suffix");
        emailChannel.sendMessage(email, subject, "", params);
    }

}

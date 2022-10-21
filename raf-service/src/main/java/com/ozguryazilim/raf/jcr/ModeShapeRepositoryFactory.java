package com.ozguryazilim.raf.jcr;

import com.ozguryazilim.raf.MetadataConfig;
import com.ozguryazilim.raf.MetadataRegistery;
import com.ozguryazilim.raf.SequencerConfig;
import com.ozguryazilim.raf.SequencerRegistery;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.modeshape.common.collection.Problems;
import org.modeshape.jcr.ConfigurationException;
import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.RepositoryConfiguration;
import org.modeshape.jcr.value.binary.BinaryStore;
import org.modeshape.schematic.document.EditableArray;
import org.modeshape.schematic.document.EditableDocument;
import org.modeshape.schematic.document.Editor;
import org.modeshape.schematic.document.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
public class ModeShapeRepositoryFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModeShapeRepositoryFactory.class);

    private static ModeShapeEngine engine;

    private static Repository repository;

    private static BinaryStore binaryStore;

    private static RepositoryConfiguration repositoryConfiguration;

    public static ModeShapeEngine getEngine() {
        if (engine == null) {
            engine = new ModeShapeEngine();
            engine.start();
            LOGGER.info("ModeShape Engine Started");
        }

        return engine;
    }

    public static BinaryStore getBinaryStore() {
        return binaryStore;
    }

    public static RepositoryConfiguration getRepositoryConfiguration() {
        return repositoryConfiguration;
    }

    /**
     * Geriye ModeShape Repository döndürür.
     *
     * @return
     */
    public static Repository getRepository() {
        if (repository == null) {
            deployRepository();
        }
        return repository;
    }

    /**
     * Geriye ModeShape için JCR Session döndürür.
     *
     * @return
     * @throws RepositoryException
     */
    public static Session getSession() throws RepositoryException {
        return getRepository().login();
    }

    private static void deployRepository() {

        //Config sisteminden dosya adını alalım.
        String configFile = ConfigResolver.getProjectStageAwarePropertyValue("raf-modeshape-config", "modeshape-config.json");

        if ("modeshape-config.json".equals(configFile)) {
            LOGGER.error("!!! ModeShape Starter on Development Mode !!!");
        }

        // Load the configuration for a repository via the classloader (can also use path to a file)...
        try {
            InputStream is = ModeShapeRepositoryFactory.class.getClassLoader().getResourceAsStream(configFile);
            RepositoryConfiguration config = RepositoryConfiguration.read(is, configFile);

            //Configürasyon değişiklikleri.
            Editor editor = config.edit();

            EditableDocument sequencers = editor.getOrCreateDocument(RepositoryConfiguration.FieldName.SEQUENCING).getOrCreateDocument(RepositoryConfiguration.FieldName.SEQUENCERS);

            for (SequencerConfig sc : SequencerRegistery.getSequencers()) {
                EditableDocument sequencer = sequencers.getOrCreateDocument(sc.getName());
                sequencer.setString(RepositoryConfiguration.FieldName.CLASSNAME, sc.getClassname());
                sequencer.setString(RepositoryConfiguration.FieldName.PATH_EXPRESSION, sc.getExpression());
            }

            //Sistemde eklencek olan metadata bağımlılıkları için CND'ler
            EditableArray nodeTypes = editor.getOrCreateArray(RepositoryConfiguration.FieldName.NODE_TYPES);
            for (MetadataConfig c : MetadataRegistery.getConfigs()) {
                nodeTypes.addString(c.getCnd());
            }

            config = new RepositoryConfiguration(editor, "RafConfig");

            LOGGER.info("Node Types : {}", config.getNodeTypes());
            LOGGER.info("Sequencers : {}", config.getSequencing().getSequencers());

            // Verify the configuration for the repository ...
            Problems problems = config.validate();
            if (problems.hasErrors()) {
                LOGGER.error("Problems starting the engine.");
                LOGGER.error("{}", problems);
                return;
            }

            // Deploy the repository ...
            repository = getEngine().deploy(config);
            repositoryConfiguration = config;
            try {
                binaryStore = config.getBinaryStorage().getBinaryStore();
            } catch (Exception ex) {
                LOGGER.error("Problems getting binaryStore.", ex);
            }
        } catch (ParsingException | FileNotFoundException | ConfigurationException | RepositoryException e) {
            LOGGER.error("Problems starting the engine.", e);
        }
    }

    public static void shutdown() {
        if (engine == null) {
            return;
        }

        try {
            engine.shutdown().get();
            engine = null;
            LOGGER.info("ModeShape Engine Stopped");
        } catch (InterruptedException | ExecutionException ex) {
            LOGGER.error("Connot shutdown jcr engine", ex);
        }
    }

}

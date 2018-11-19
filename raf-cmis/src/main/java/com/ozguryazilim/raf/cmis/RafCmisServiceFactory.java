package com.ozguryazilim.raf.cmis;

import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.MutableCallContext;
import org.apache.chemistry.opencmis.server.support.wrapper.CallContextAwareCmisService;
import org.apache.chemistry.opencmis.server.support.wrapper.CmisServiceWrapperManager;
import org.apache.chemistry.opencmis.server.support.wrapper.ConformanceCmisServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RafCmisServiceFactory extends AbstractServiceFactory {

    private static final Logger LOG = LoggerFactory.getLogger(RafCmisServiceFactory.class);

    private static final String PREFIX_LOGIN = "login.";
    private static final String PREFIX_REPOSITORY = "repository.";
    private static final String SUFFIX_READWRITE = ".readwrite";
    private static final String SUFFIX_READONLY = ".readonly";

    private static final BigInteger DEFAULT_MAX_ITEMS_TYPES = BigInteger.valueOf(50);

    private static final BigInteger DEFAULT_DEPTH_TYPES = BigInteger.valueOf(-1);

    private static final BigInteger DEFAULT_MAX_ITEMS_OBJECTS = BigInteger.valueOf(200);

    private static final BigInteger DEFAULT_DEPTH_OBJECTS = BigInteger.valueOf(10);

    private ThreadLocal<CallContextAwareCmisService> threadLocalService = new ThreadLocal<CallContextAwareCmisService>();
    private CmisServiceWrapperManager wrapperManager;

    private RafCmisRepositoryManager repositoryManager;
    private RafCmisUserManager userManager;
    private RafCmisTypeManager typeManager;

    @Override
    public void init(Map<String, String> parameters) {

        wrapperManager = new CmisServiceWrapperManager();
        wrapperManager.addWrappersFromServiceFactoryParameters(parameters);
        wrapperManager.addOuterWrapper(ConformanceCmisServiceWrapper.class, DEFAULT_MAX_ITEMS_TYPES,
                DEFAULT_DEPTH_TYPES, DEFAULT_MAX_ITEMS_OBJECTS, DEFAULT_DEPTH_OBJECTS);

        repositoryManager = new RafCmisRepositoryManager();
        userManager = new RafCmisUserManager();
        typeManager = new RafCmisTypeManager();

        readConfiguration(parameters);
    }

    @Override
    public void destroy() {
        threadLocalService = null;
    }

    @Override
    public CmisService getService(CallContext context) {
        //userManager.authenticate(context);
        CallContextAwareCmisService service = threadLocalService.get();
        if (service == null) {
            RafCmisService fileShareService = new RafCmisService(repositoryManager);
            // wrap it with the chain of wrappers
            service = (CallContextAwareCmisService) wrapperManager.wrap(fileShareService);
            threadLocalService.set(service);
        }

        MutableCallContext mcc = (MutableCallContext)context;
        mcc.put("foo","bar");
        service.setCallContext(context);

        return service;
    }

    private void readConfiguration(Map<String, String> parameters) {
        List<String> keys = new ArrayList<String>(parameters.keySet());
        Collections.sort(keys);

        for (String key : keys) {
            if (key.startsWith(PREFIX_LOGIN)) {
                String usernameAndPassword = parameters.get(key);
                if (usernameAndPassword == null) {
                    continue;
                }

                String username = usernameAndPassword;
                String password = "";

                int x = usernameAndPassword.indexOf(':');
                if (x > -1) {
                    username = usernameAndPassword.substring(0, x);
                    password = usernameAndPassword.substring(x + 1);
                }

                LOG.info("Adding login '{}'.", username);

                //userManager.addLogin(username, password);
            } else if (key.startsWith(PREFIX_REPOSITORY)) {
                String repositoryId = key.substring(PREFIX_REPOSITORY.length()).trim();
                int x = repositoryId.lastIndexOf('.');
                if (x > 0) {
                    repositoryId = repositoryId.substring(0, x);
                }

                if (repositoryId.length() == 0) {
                    throw new IllegalArgumentException("No repository id!");
                }

                if (key.endsWith(SUFFIX_READWRITE)) {
                    RafCmisRepository fsr = repositoryManager.getRepository(repositoryId);
                    for (String user : split(parameters.get(key))) {
                        fsr.setUserReadWrite(user);
                    }
                } else if (key.endsWith(SUFFIX_READONLY)) {
                    RafCmisRepository fsr = repositoryManager.getRepository(repositoryId);
                    for (String user : split(parameters.get(key))) {
                        fsr.setUserReadOnly(user);
                    }
                } else {
                    String root = parameters.get(key);

                    LOG.info("Adding repository '{}': {}", repositoryId, root);

                    RafCmisRepository fsr = new RafCmisRepository(repositoryId, root, typeManager);
                    repositoryManager.addRepository(fsr);
                }
            }
        }
        
        LOG.info("Adding repository '{}': {}", "Raf", "/");

        RafCmisRepository fsr = new RafCmisRepository("Raf", "/", typeManager);
        repositoryManager.addRepository(fsr);
    }

    private List<String> split(String csl) {
        if (csl == null) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();
        for (String s : csl.split(",")) {
            result.add(s.trim());
        }

        return result;
    }
}

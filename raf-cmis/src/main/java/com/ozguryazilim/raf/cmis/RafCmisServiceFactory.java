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

        //Burada normalde config içerisinden bilgiler ile kullanıcı ve repository tanımları okunuyor
        //Ama raf için kullanıcı bilgileri Shiro/DeltaSpike/Telve üzerinden yönetiliyor
        //Keza rapository olarak da her bir raf tanımı alınıyor!
        //Üstelik bu bilgilerde repositoryManager tarafından lazy olarak toplanacak!
        //readConfiguration(parameters);
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

}

package com.ozguryazilim.raf.cmis;

import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RafCmisRepositoryManager {

    private final Map<String, RafCmisRepository> repositories;

    public RafCmisRepositoryManager() {
        repositories = new HashMap<String, RafCmisRepository>();
    }

    public void addRepository(RafCmisRepository fsr) {
        if (fsr == null || fsr.getRepositoryId() == null) {
            return;
        }

        repositories.put(fsr.getRepositoryId(), fsr);
    }

    public RafCmisRepository getRepository(String repositoryId) {
        RafCmisRepository result = repositories.get(repositoryId);
        if (result == null) {
            throw new CmisObjectNotFoundException("Unknown repository '" + repositoryId + "'!");
        }

        return result;
    }

    public Collection<RafCmisRepository> getRepositories() {
        return repositories.values();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (RafCmisRepository repository : repositories.values()) {
            sb.append('[');
            sb.append(repository.getRepositoryId());
            sb.append(']');
        }

        return sb.toString();
    }
}
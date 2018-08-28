package com.ozguryazilim.raf.cmis;

import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.server.CallContext;

import java.util.HashMap;
import java.util.Map;

public class RafCmisUserManager {

    private final Map<String, String> logins;

    public RafCmisUserManager() {
        logins = new HashMap<String, String>();
    }

    public synchronized void addLogin(String username, String password) {
        if (username == null || password == null) {
            return;
        }

        logins.put(username.trim(), password);
    }

    public synchronized String authenticate(CallContext context) {
        if (!authenticate(context.getUsername(), context.getPassword())) {
            throw new CmisPermissionDeniedException("Invalid username or password.");
        }

        return context.getUsername();
    }

    private synchronized boolean authenticate(String username, String password) {
        String pwd = logins.get(username);
        if (pwd == null) {
            return false;
        }

        return pwd.equals(password);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (String user : logins.keySet()) {
            sb.append('[');
            sb.append(user);
            sb.append(']');
        }

        return sb.toString();
    }
}

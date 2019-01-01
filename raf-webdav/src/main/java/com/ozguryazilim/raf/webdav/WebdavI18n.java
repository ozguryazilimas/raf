package com.ozguryazilim.raf.webdav;

import org.modeshape.common.annotation.Immutable;
import org.modeshape.common.i18n.I18n;

/**
 *
 * @author oyas
 */
@Immutable
public final class WebdavI18n {
    
    public static I18n noStoredRequest;
    public static I18n uriIsProperty;
    public static I18n warnMultiValuedProperty;
    public static I18n errorPropertyPath;

    // DefaultRequestResolver messages
    public static I18n requiredParameterMissing;
    public static I18n cannotCreateRepository;
    public static I18n cannotCreateWorkspaceInRepository;
    public static I18n cannotGetRepositorySession;

    private WebdavI18n() {
    }

    static {
        try {
            I18n.initialize(WebdavI18n.class);
        } catch (final Exception err) {
            // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
            System.err.println(err);
        }
    }
}

package com.ozguryazilim.raf.config;

import com.ozguryazilim.telve.auth.SecuredPage;
import com.ozguryazilim.telve.view.Pages;
import javax.enterprise.context.ApplicationScoped;
import org.apache.deltaspike.jsf.api.config.view.View;

/**
 *
 * @author oyas
 */
@ApplicationScoped
@View(name = "raf") @SecuredPage
public class RafPages implements Pages{
    

}

package com.ozguryazilim.raf.config;

import com.ozguryazilim.raf.tag.TagSuggestionService;
import com.ozguryazilim.telve.api.module.TelveModule;
import com.ozguryazilim.telve.suggestion.SuggestionGroupRegistery;

import javax.annotation.PostConstruct;

@TelveModule
public class RafServiceModule {

    @PostConstruct
    public void init(){
        SuggestionGroupRegistery.intance().addGroup(TagSuggestionService.SUGGESSTION_GROUP, Boolean.FALSE);
    }

}

package com.ozguryazilim.raf;

/**
 *
 * @author oyas
 */
public class MetadataConfigBuilder {
    
    private MetadataConfig config;
    
    public static MetadataConfigBuilder of( String name ){
        MetadataConfigBuilder result = new MetadataConfigBuilder();
        result.config = new MetadataConfig();
        result.config.setName(name);
        
        return result;
    }
    
    public MetadataConfigBuilder forType( String type){
        config.setType(type);
        return this;
    }
    
    public MetadataConfigBuilder forMimeType( String mimeType ){
        config.setMimeType(mimeType);
        return this;
    }
    
    public MetadataConfigBuilder canSelectable(){
        config.setSelectable(true);
        return this;
    }
    
    public MetadataConfigBuilder cnd( String cnd ){
        config.setCnd(cnd);
        return this;
    }
    
    public MetadataConfig build(){
        return config;
    }
    
}

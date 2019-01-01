package com.ozguryazilim.raf.web;

import com.ozguryazilim.mutfak.kahve.annotations.Kahve;
import java.sql.SQLException;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.sql.DataSource;

/**
 * Kahve için resource producer.
 * 
 * @author Hakan Uygun
 */
@ApplicationScoped
public class KahveProducer {
   
    @Resource(mappedName = "java:jboss/datasources/RafDS" )
    private DataSource dataSource;
    
    @Produces
    @Kahve
    public DataSource createDataSource() throws SQLException {
        return dataSource;
    }
}

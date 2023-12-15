package com.bezkoder.spring.datajpa.configuration;

import java.util.Properties;
import java.sql.SQLException;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import oracle.jdbc.pool.OracleDataSource;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceImpl;
import oracle.ucp.jdbc.ConnectionInitializationCallback;
import oracle.jdbc.OracleConnection;
    
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jdbc.support.ConnectionUsernameProvider;
import org.springframework.data.jdbc.support.oracle.ProxyDataSource;
import org.springframework.util.Assert;

import org.springframework.beans.factory.annotation.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class OracleConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(OracleConfiguration.class);

    static {
        logger.info("Initializing {}", OracleConfiguration.class.toString());
    }

    private @Value("${spring.datasource.schema}") String schema;

    @Bean(name = {"dataSourceProperties"})
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties getDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = {"dataSource"})
    @ConfigurationProperties(prefix = "spring.datasource.oracleucp")
    public DataSource getDataSource(@Qualifier("dataSourceProperties") DataSourceProperties properties) throws java.sql.SQLException {
        final DataSource dataSource = properties
            .initializeDataSourceBuilder()
            .type(PoolDataSourceImpl.class)
            .build();
        
        Assert.isInstanceOf(PoolDataSource.class, dataSource, "data source should be of type " + PoolDataSource.class.toString());

        ((PoolDataSource)dataSource).registerConnectionInitializationCallback(new ConnectionInitializationCallback() {
                @Override
                public void initialize(java.sql.Connection connection)  throws java.sql.SQLException {
                    final OracleConnection oracleConnection = (OracleConnection) connection;

                    if (!oracleConnection.isProxySession()) {
                        final Properties properties = new Properties();

                        properties.setProperty("PROXY_USER_NAME", schema);
                            
                        oracleConnection.openProxySession​(OracleConnection.PROXYTYPE_USER_NAME, properties);
                    }
                }
            });
            
        return dataSource;
    }
}

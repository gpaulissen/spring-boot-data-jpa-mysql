package com.bezkoder.spring.datajpa.configuration;

import java.util.Properties;
import java.sql.SQLException;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;
import oracle.ucp.jdbc.ConnectionInitializationCallback;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceImpl;
import oracle.ucp.jdbc.LabelableConnection;
    
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
        logger.info("getDataSource");
        
        final DataSource dataSource = properties
            .initializeDataSourceBuilder()
            //.type(PoolDataSourceImpl.class) // spring.datasource.type=oracle.ucp.jdbc.PoolDataSourceImpl
            .build();
        
        Assert.isInstanceOf(PoolDataSource.class, dataSource, "data source should be of type " + PoolDataSource.class.toString());

        // ((PoolDataSource)dataSource).registerConnectionInitializationCallback(new MyConnectionInitializationCallback());
        ((PoolDataSource)dataSource).registerConnectionLabelingCallback(new MyConnectionLabellingCallback());
            
        return dataSource;
    }

    private class MyConnectionInitializationCallback implements ConnectionInitializationCallback {
        @Override
        public void initialize(java.sql.Connection connection)  throws java.sql.SQLException {
            logger.info("MyConnectionInitializationCallback.initialize");
            
            final OracleConnection oracleConnection = (OracleConnection) connection;

            if (!oracleConnection.isProxySession()) {
                final Properties properties = new Properties();

                properties.setProperty(OracleConnection.PROXY_USER_NAME, schema);
                            
                oracleConnection.openProxySession​(OracleConnection.PROXYTYPE_USER_NAME, properties);
            }
        }
    }

    private class MyConnectionLabellingCallback
        implements oracle.ucp.ConnectionLabelingCallback,
                   oracle.ucp.jdbc.ConnectionLabelingCallback {

        private java.util.Properties connectionLabelingProperties = new java.util.Properties();

        public MyConnectionLabellingCallback() {
            logger.info("MyConnectionLabellingCallback");

            connectionLabelingProperties.put(OracleConnection.PROXY_USER_NAME, schema);
        }
        
        // oracle.ucp.jdbc.ConnectionInitializationCallback
        @Override
        public java.util.Properties getRequestedLabels() {
            logger.info("MyProxyConnectionLabelingCallback.getRequestedLabels");
            
            return connectionLabelingProperties;
        }

        // oracle.ucp.ConnectionInitializationCallback
        @Override
        public int cost(Properties reqLabels, Properties currentLabels) {
            int result = Integer.MAX_VALUE;

            if (reqLabels.equals(currentLabels)) {
                result = 0;
            } else if (currentLabels == null) {
                result = 1;
            }
            
            logger.info("MyProxyConnectionLabelingCallback.cost(reqLabels={}, currentLabels={}) = {}", reqLabels, currentLabels, result);

            return result;
        }

        // oracle.ucp.ConnectionInitializationCallback
        @Override
        public boolean configure(Properties reqLabels, Object conn) {
            logger.info("MyProxyConnectionLabelingCallback.configure(reqLabels={})", reqLabels);

            try {
                LabelableConnection lConn = (LabelableConnection) conn;
                final Properties connectionLabels = lConn.getConnectionLabels();

                logger.info("connectionLabels={}", connectionLabels);

                if (connectionLabels == null) {
                    setProxy((OracleConnection)conn, schema);
                    lConn.applyConnectionLabel(OracleConnection.PROXY_USER_NAME, schema);
                } else {
                    Object currentLabel = connectionLabels.get(OracleConnection.PROXY_USER_NAME);
                    
                    if (!schema.equals(currentLabel)) {
                        setProxy((OracleConnection) conn, schema);
                        lConn.applyConnectionLabel(OracleConnection.PROXY_USER_NAME, schema);
                    } else {
                        //Label match, good(strange anyway)
                    }
                }
                return true;
            } catch (SQLException| RuntimeException sExp) {
                return false;
            }
        }

        private void setProxy(OracleConnection oraCon, String proxyUserName) throws SQLException {
            logger.info("setProxy(proxyUserName={})", proxyUserName);

            if (oraCon.isProxySession()) {
                logger.info("closing proxy session");
                
                oraCon.close(OracleConnection.PROXY_SESSION);
            }
            
            Properties proxyProperties = new Properties();
            
            proxyProperties.setProperty(OracleConnection.PROXY_USER_NAME, proxyUserName);

            logger.info("opening proxy session");

            oraCon.openProxySession(OracleConnection.PROXYTYPE_USER_NAME, proxyProperties);
        }
    }
}

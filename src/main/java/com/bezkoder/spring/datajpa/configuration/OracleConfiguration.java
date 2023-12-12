package com.bezkoder.spring.datajpa.configuration;

import java.sql.SQLException;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import oracle.jdbc.pool.OracleDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jdbc.support.ConnectionUsernameProvider;
import org.springframework.data.jdbc.support.oracle.ProxyDataSource;
import org.springframework.util.Assert;

    
@Configuration
@ConfigurationProperties("oracle")
public class OracleConfiguration {
    @Autowired
    private ConnectionUsernameProvider contextProvider;

    @NotNull
    private String username;

    @NotNull
    private String password;

    @NotNull
    private String url;

    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    @Bean
    OracleDataSource oracleDataSource() throws SQLException {
        return OracleDataSourceFactory.getOracleDataSource(url, username, password);
    }
    
    @Bean
    @Primary
    DataSource dataSource() throws SQLException {
        Assert.notNull(contextProvider, "Context provider must NOT be null");
        
        return new ProxyDataSource(oracleDataSource(), contextProvider);
    }
}

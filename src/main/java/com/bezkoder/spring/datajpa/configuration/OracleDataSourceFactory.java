package com.bezkoder.spring.datajpa.configuration;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import oracle.jdbc.pool.OracleDataSource;
import org.springframework.util.Assert;
    
public class OracleDataSourceFactory {

    private static Map<String, OracleDataSource> oracleDataSources = new ConcurrentHashMap<>();

    private OracleDataSourceFactory() {
        // private constructor to prohibit instances being created
    }
        
    public static synchronized OracleDataSource getOracleDataSource(final String url, final String username, final String password) throws java.sql.SQLException {
        final String key = url + "|" + username + "|" + password;
        OracleDataSource oracleDataSource = oracleDataSources.get(key);
        
        if (oracleDataSource == null) {
            oracleDataSource = new OracleDataSource();

            Assert.notNull(username, "Username must NOT be null");
            Assert.notNull(password, "Password must NOT be null");
            Assert.notNull(url, "URL must NOT be null");

            oracleDataSource.setUser(username);
            oracleDataSource.setPassword(password);
            oracleDataSource.setURL(url);
            oracleDataSource.setImplicitCachingEnabled(true);

            oracleDataSources.put(key, oracleDataSource);
        }

        return oracleDataSource;
    }
}

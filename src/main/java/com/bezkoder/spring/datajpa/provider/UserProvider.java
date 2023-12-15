package com.bezkoder.spring.datajpa.provider;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.jdbc.support.ConnectionUsernameProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

@Component
public class UserProvider implements ConnectionUsernameProvider {

    @NotNull
    private @Value("${spring.datasource.schema}") String schema;

    @Override
    public String getUserName() {
        Assert.notNull(schema, "Schema name must NOT be null");

        return schema;
    }
}

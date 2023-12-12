package com.bezkoder.spring.datajpa.provider;

import javax.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.data.jdbc.support.ConnectionUsernameProvider;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

@Component
@ConfigurationProperties("app1")
public class UserProvider implements ConnectionUsernameProvider {

    @NotNull
    private String schema;

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String getUserName() {
        Assert.notNull(schema, "Schema name must NOT be null");

        return schema;
    }
}

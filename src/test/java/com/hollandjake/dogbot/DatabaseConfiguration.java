package com.hollandjake.dogbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
public class DatabaseConfiguration {
    @Value(value = "${spring.datasource.url}")
    private String url;

    @Value(value = "${spring.datasource.password}")
    private String password;

    @Value(value = "${spring.datasource.username}")
    private String username;

    @Value(value = "${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    public DataSource getDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(driverClassName);
        dataSourceBuilder.username(username);
        dataSourceBuilder.password(password);
        dataSourceBuilder.url(url + "?allowMultiQueries=true"
                + "&character_set_client=utf8mb4"
                + "&character_set_results=utf8mb4"
                + "&character_set_connection=utf8mb4"
                + "&autoReconnect=true"
                + "&useCompression=true"
                + "&allowMultiQueries=true"
                + "&rewriteBatchedStatements=true");
        return dataSourceBuilder.build();
    }
}

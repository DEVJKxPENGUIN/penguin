package com.devjk.penguin.framework.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(basePackages = ["com.devjk.penguin.db.repository"])
class JpaConfig(
    @Value("\${db-host}")
    private val dbHost: String,
    @Value("\${db-username}")
    private val dbUsername: String,
    @Value("\${db-password}")
    private val dbPassword: String,
) {

    @Bean
    fun dataSource(): DataSource {
        val config = HikariConfig().apply {
            driverClassName = "com.mysql.cj.jdbc.Driver"
            jdbcUrl =
                "jdbc:mysql://${dbHost}/penguin?useSSL=false&serverTimezone=UTC&connectTimeout=5000&autoReconnect=false&allowPublicKeyRetrieval=true"
            username = dbUsername
            password = dbPassword
            maximumPoolSize = 2
            minimumIdle = 2
        }

        return HikariDataSource(config)
    }

}
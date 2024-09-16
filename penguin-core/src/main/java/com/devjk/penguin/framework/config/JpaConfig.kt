package com.devjk.penguin.framework.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = ["com.devjk.penguin.db.repository"])
class JpaConfig {
}
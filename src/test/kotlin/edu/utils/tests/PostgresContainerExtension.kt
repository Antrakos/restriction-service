package edu.utils.tests

import org.testcontainers.containers.PostgreSQLContainer

class PostgresContainerExtension : AbstractContainerAwareExtension<PostgreSQLContainer<*>>() {
    class KPostgreSQLContainer : PostgreSQLContainer<KPostgreSQLContainer>()

    override fun start(): PostgreSQLContainer<*> {
        log.info("Starting container for Postgres with database: {}", POSTGRES_DB_NAME)
        val postgres = KPostgreSQLContainer()
                .withUsername(POSTGRES_USERNAME)
                .withPassword(POSTGRES_PASSWORD)
                .withDatabaseName(POSTGRES_DB_NAME)
        postgres.start()
        System.setProperty("spring.datasource.url", postgres.jdbcUrl)
        System.setProperty("spring.datasource.username", POSTGRES_USERNAME)
        System.setProperty("spring.datasource.password", POSTGRES_PASSWORD)
        return postgres
    }

    companion object {
        private const val POSTGRES_DB_NAME = "dev_db"
        private const val POSTGRES_USERNAME = "restriction-service"
        private const val POSTGRES_PASSWORD = "pwd"
    }
}

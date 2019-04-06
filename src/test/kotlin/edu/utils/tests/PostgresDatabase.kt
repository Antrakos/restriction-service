package edu.utils.tests

/**
 * Annotation to enable postgres database initialization for test
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class PostgresDatabase(
        val host: String = "localhost",
        val port: Int = 5433,
        val name: String = "dev_db",
        val username: String = "postgres",
        val password: String = "postgres"
)

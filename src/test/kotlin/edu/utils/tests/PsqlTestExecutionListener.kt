package edu.utils.tests

import edu.utils.tests.AnnotationUtils.findAnnotationIn
import edu.zoo.restriction.LoggerDelegate
import org.springframework.test.context.TestContext
import org.springframework.test.context.support.AbstractTestExecutionListener
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres
import ru.yandex.qatools.embed.postgresql.distribution.Version.Main.V9_6
import java.io.IOException
import java.io.UncheckedIOException
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Required to pick all `PostgresDatabase` annotations and initialize databases for them.
 * Takes care to deduplicate multiple same declaration of same database.
 */
class PsqlTestExecutionListener : AbstractTestExecutionListener() {
    override fun beforeTestClass(testContext: TestContext) {
        findDeclarationsAndStartDatabasesFor(testContext.testClass.kotlin)
    }

    companion object {
        private val log by LoggerDelegate()

        private val postgres = EmbeddedPostgres(V9_6)
        private val DATABASES = ConcurrentHashMap<PostgresDatabase, String>()

        init {
            Runtime.getRuntime().addShutdownHook(Thread(Runnable { stopPostgres() }))
        }

        fun findDeclarationsAndStartDatabasesFor(clazz: KClass<*>) {
            findAnnotationIn(clazz, PostgresDatabase::class)
                    .forEach { addDatabaseIfNeeded(it) }
        }

        private fun addDatabaseIfNeeded(database: PostgresDatabase) {
            DATABASES.computeIfAbsent(database) { addDatabase(it) }
        }

        private fun addDatabase(database: PostgresDatabase): String {
            try {
                log.info("Starting database {}", database)
                return postgres.start(database.host, database.port, database.name, database.username, database.password)
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            }

        }

        private fun stopPostgres() {
            log.info("Stopping all databases")
            postgres.stop()
        }
    }
}

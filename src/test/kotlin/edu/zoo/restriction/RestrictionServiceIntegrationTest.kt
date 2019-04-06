package edu.zoo.restriction

import edu.utils.tests.KafkaExtension
import edu.utils.tests.PostgresDatabase
import edu.utils.tests.PsqlTestExecutionListener
import org.flywaydb.test.annotation.FlywayTest
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@FlywayTest
@PostgresDatabase(name = "restrictions")
@SqlGroup(value = [
    Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = ["/cleanup.sql"]),
    Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = ["/cleanup.sql"])
])
@Extensions(value = [
    ExtendWith(SpringExtension::class),
    ExtendWith(KafkaExtension::class)
])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(listeners = [PsqlTestExecutionListener::class], mergeMode = MERGE_WITH_DEFAULTS)
@ActiveProfiles("test")
annotation class RestrictionServiceIntegrationTest
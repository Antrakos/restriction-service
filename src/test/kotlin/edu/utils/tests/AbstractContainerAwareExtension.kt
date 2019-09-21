package edu.utils.tests

import edu.utils.tests.JunitExtensionUtils.countTestsWithExtension
import edu.zoo.restriction.LoggerDelegate
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.testcontainers.containers.GenericContainer

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

abstract class AbstractContainerAwareExtension<T : GenericContainer<*>> : BeforeAllCallback, AfterAllCallback {
    val log by LoggerDelegate()

    private val name = this::class.simpleName!!

    @Throws(Exception::class)
    protected abstract fun start(): T

    private fun stop(container: T) {
        log.info("Stopping container for: {}", name)
        container.stop()
    }

    override fun beforeAll(context: ExtensionContext) {
        if (containers.containsKey(name)) {
            return
        }
        runningTests[name] = AtomicInteger(countTestsWithExtension(context, this::class))
        containers[name] = start()
    }

    override fun afterAll(context: ExtensionContext) {
        if (runningTests[javaClass.name]?.decrementAndGet() == 0) {
            stop(containers[name] as T)
        }
    }

    companion object {
        private val runningTests = ConcurrentHashMap<String, AtomicInteger>()
        private val containers = ConcurrentHashMap<String, Any>()
    }
}

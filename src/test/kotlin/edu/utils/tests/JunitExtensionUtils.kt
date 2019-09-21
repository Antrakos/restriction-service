package edu.utils.tests

import edu.utils.tests.AnnotationUtils.getFirstLevelAnnotations
import edu.utils.tests.AnnotationUtils.getSecondLevelAnnotations
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.Extensions
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.springframework.test.util.ReflectionTestUtils
import kotlin.reflect.KClass

object JunitExtensionUtils {
    fun countTestsWithExtension(context: ExtensionContext, extension: KClass<out Extension>): Int {
        val engineDescriptor = ReflectionTestUtils.getField(context.root, "testDescriptor") as EngineDescriptor
        return findTestClasses(engineDescriptor)
                .filter { testClass -> hasExtension(testClass, extension) }
                .filter { notDisabled(it) }
                .count()
    }

    private fun findTestClasses(descriptor: EngineDescriptor): Sequence<KClass<*>> {
        return descriptor.children
                .asSequence()
                .filter { it is ClassTestDescriptor }
                .map { it as ClassTestDescriptor }
                .map { it.testClass.kotlin }
    }

    private fun notDisabled(testClass: KClass<*>): Boolean {
        return getFirstLevelAnnotations(testClass)
                .none { it is Disabled }
    }

    private fun hasExtension(testClass: KClass<*>, extension: KClass<out Extension>): Boolean {
        return (getExtendWithAnnotations(testClass) + getExtensionsAnnotations(testClass))
                .flatMap { it.value.asSequence() }
                .contains(extension)
    }

    private fun getExtendWithAnnotations(testClass: KClass<*>): Sequence<ExtendWith> {
        return (getFirstLevelAnnotations(testClass) + getSecondLevelAnnotations(testClass))
                .filter { it is ExtendWith }
                .map { it as ExtendWith }
    }

    private fun getExtensionsAnnotations(testClass: KClass<*>): Sequence<ExtendWith> {
        return (getFirstLevelAnnotations(testClass) + getSecondLevelAnnotations(testClass))
                .filter { it is Extensions }
                .map { it as Extensions }
                .flatMap { it.value.asSequence() }
    }
}

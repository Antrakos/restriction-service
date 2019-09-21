package edu.utils.tests

import kotlin.reflect.KClass

object AnnotationUtils {
    fun getFirstLevelAnnotations(testClass: KClass<*>) = testClass.annotations.asSequence()

    fun getSecondLevelAnnotations(testClass: KClass<*>): Sequence<Annotation> {
        return getFirstLevelAnnotations(testClass)
                .map { it.annotationClass }
                .map { it.annotations }
                .flatten()
    }
}

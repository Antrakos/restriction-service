package edu.utils.tests

import kotlin.reflect.KClass
import kotlin.reflect.full.cast

object AnnotationUtils {
    fun getFirstLevelAnnotations(testClass: KClass<*>) = testClass.annotations.asSequence()

    fun getSecondLevelAnnotations(testClass: KClass<*>): Sequence<Annotation> {
        return getFirstLevelAnnotations(testClass)
                .map { it.annotationClass }
                .map { it.annotations }
                .flatten()
    }

    fun <T : Annotation> findAnnotationIn(clazz: KClass<*>, annotation: KClass<T>) = (getFirstLevelAnnotations(clazz) + getSecondLevelAnnotations(clazz))
            .filter { annotation.isInstance(it) }
            .map { annotation.cast(it) }
}

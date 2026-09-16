package io.cstories.processor

import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals

class ComponentDescriptorTest {
    @Test
    fun `refKey keeps historical shape without namespace`() {
        assertEquals(
            "Button.Primary",
            componentDescriptor(namespace = "", enclosingObjectName = "Button", functionName = "Primary").refKey,
        )
    }

    @Test
    fun `refKey prepends namespace when provided`() {
        assertEquals(
            "v2.Button.Primary",
            componentDescriptor(namespace = "v2", enclosingObjectName = "Button", functionName = "Primary").refKey,
        )
    }

    @Test
    fun `top level namespaced component includes namespace in path segments`() {
        assertEquals(
            listOf("v2", "Primary"),
            componentDescriptor(namespace = "v2", enclosingObjectName = null, functionName = "Primary").refPathSegments,
        )
    }

    private fun componentDescriptor(
        namespace: String,
        enclosingObjectName: String?,
        functionName: String,
    ): ComponentDescriptor {
        return ComponentDescriptor(
            namespace = namespace,
            enclosingObjectName = enclosingObjectName,
            functionName = functionName,
            fqn = "sample.$functionName",
            function = fakeKsFunction(),
            originatingFile = null as KSFile?,
            documentation = null,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun fakeKsFunction(): KSFunctionDeclaration {
        return Proxy.newProxyInstance(
            KSFunctionDeclaration::class.java.classLoader,
            arrayOf(KSFunctionDeclaration::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "toString" -> "KSFunctionDeclarationProxy"
                "hashCode" -> 0
                "equals" -> false
                else -> throw UnsupportedOperationException("Unexpected call to ${method.name}")
            }
        } as KSFunctionDeclaration
    }
}

package io.cstories.processor

import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import java.lang.reflect.Proxy
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ComponentRefsGeneratorTest {
    @Test
    fun `object name is fixed regardless of module name`() {
        assertEquals("CStoryComponentRefs", ComponentRefsGenerator.OBJECT_NAME)
    }

    @Test
    fun `qualified name includes the generated package`() {
        assertEquals(
            "io.cstories.generated.CStoryComponentRefs",
            ComponentRefsGenerator.QUALIFIED_NAME,
        )
    }

    @Test
    fun `metadata decoding preserves empty namespace fields`() {
        val metadata = ComponentRefsGenerator.decodeMetadata(
            listOf(
                "",
                encoded("LumenSidebar"),
                encoded("Info"),
                encoded("com.example.LumenSidebar.Companion.Info"),
                encoded("docs"),
            ),
        )

        assertEquals("", metadata.single().namespace)
        assertEquals("LumenSidebar", metadata.single().enclosingObjectName)
        assertEquals("Info", metadata.single().functionName)
        assertEquals("com.example.LumenSidebar.Companion.Info", metadata.single().fqn)
    }

    private fun encoded(value: String): String = Base64.getEncoder().encodeToString(value.toByteArray())

    @Test
    fun `buildFileSpec nests namespaced component refs under namespace object`() {
        val file = ComponentRefsGenerator.buildFileSpec(
            listOf(
                component(namespace = "", enclosingObjectName = "Button", functionName = "Primary", fqn = "sample.Button.Companion.Primary"),
                component(namespace = "v2", enclosingObjectName = "Button", functionName = "Primary", fqn = "sample.v2.Button.Companion.Primary"),
                component(namespace = "v2", enclosingObjectName = "Button", functionName = "Warning", fqn = "sample.v2.Button.Companion.Warning"),
                component(namespace = "v2", enclosingObjectName = null, functionName = "Standalone", fqn = "sample.Standalone"),
            ),
        )

        val generated = file.toString()
        assertContains(generated, "public object CStoryComponentRefs")
        assertContains(generated, "public object Button")
        assertContains(generated, "const val Primary")
        assertContains(generated, "\"sample.Button.Companion.Primary\"")
        assertContains(generated, "public object v2")
        assertContains(generated, "public object Button")
        assertContains(generated, "\"sample.v2.Button.Companion.Primary\"")
        assertContains(generated, "const val Warning")
        assertContains(generated, "\"sample.v2.Button.Companion.Warning\"")
        assertContains(generated, "const val Standalone")
        assertContains(generated, "\"sample.Standalone\"")
        assertContains(generated, "componentFqn = \"sample.v2.Button.Companion.Primary\"")
    }

    @Test
    fun `buildFileSpec keeps non namespaced and namespaced refs side by side`() {
        val file = ComponentRefsGenerator.buildFileSpec(
            listOf(
                component(namespace = "", enclosingObjectName = "LumenIconOnBackground", functionName = "Brand", fqn = "sample.v1.LumenIconOnBackground.Brand"),
                component(namespace = "v2", enclosingObjectName = "LumenIconOnBackground", functionName = "Brand", fqn = "sample.v2.LumenIconOnBackground.Brand"),
            ),
        )

        val generated = file.toString()
        assertContains(generated, "public object LumenIconOnBackground")
        assertContains(generated, "public object v2")
        assertContains(generated, "\"sample.v1.LumenIconOnBackground.Brand\"")
        assertContains(generated, "\"sample.v2.LumenIconOnBackground.Brand\"")
    }

    @Test
    fun `validateStructure rejects namespace colliding with root property`() {
        val errors = ComponentRefsGenerator.validateStructure(
            listOf(
                component(namespace = "", enclosingObjectName = null, functionName = "v2", fqn = "sample.v2"),
                component(namespace = "v2", enclosingObjectName = "Button", functionName = "Primary", fqn = "sample.Button.Primary"),
            ),
        )

        assertEquals(
            listOf("@CStoryComponent reference collision for 'v2': a property already exists at that path"),
            errors,
        )
    }

    @Test
    fun `validateStructure rejects duplicate complete keys`() {
        val errors = ComponentRefsGenerator.validateStructure(
            listOf(
                component(namespace = "v2", enclosingObjectName = "Button", functionName = "Primary", fqn = "sample.Button.Primary"),
                component(namespace = "v2", enclosingObjectName = "Button", functionName = "Primary", fqn = "sample.Button.Primary2"),
            ),
        )

        assertEquals(
            listOf("@CStoryComponent reference collision for 'v2.Button.Primary': another component already uses this reference name"),
            errors,
        )
    }

    @Test
    fun `validateStructure rejects root object colliding with top level property`() {
        val errors = ComponentRefsGenerator.validateStructure(
            listOf(
                component(namespace = "", enclosingObjectName = null, functionName = "Button", fqn = "sample.Button"),
                component(namespace = "", enclosingObjectName = "Button", functionName = "Primary", fqn = "sample.Button.Primary"),
            ),
        )

        assertEquals(
            listOf("@CStoryComponent reference collision for 'Button': a property already exists at that path"),
            errors,
        )
    }

    private fun component(
        namespace: String,
        enclosingObjectName: String?,
        functionName: String,
        fqn: String,
    ): ComponentDescriptor {
        return ComponentDescriptor(
            namespace = namespace,
            enclosingObjectName = enclosingObjectName,
            functionName = functionName,
            fqn = fqn,
            function = fakeKsFunction(),
            originatingFile = null as KSFile?,
            documentation = "docs for $functionName",
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

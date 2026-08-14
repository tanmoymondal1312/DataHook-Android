package com.mediaghor.datahook

import com.mediaghor.datahook.data.remote.dto.AppUpdateConfig
import com.mediaghor.datahook.update.decideUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateDecisionTest {

    private fun config(enabled: Boolean = true, mode: Int = 1) = AppUpdateConfig(
        enabled = enabled,
        mode = mode,
        currentVersion = "2.4",
        newVersion = "3.89",
    )

    // --- Disabled / mode 0: never prompt ------------------------------------//

    @Test
    fun `disabled config never prompts`() {
        assertNull(decideUpdate(config(enabled = false, mode = 1)))
        assertNull(decideUpdate(config(enabled = false, mode = 2)))
    }

    @Test
    fun `mode 0 never prompts`() {
        assertNull(decideUpdate(config(mode = 0)))
    }

    @Test
    fun `unknown mode never prompts`() {
        assertNull(decideUpdate(config(mode = 99)))
    }

    // --- Mode 1: optional ------------------------------------------------//

    @Test
    fun `mode 1 prompts optionally`() {
        val prompt = decideUpdate(config(mode = 1))
        assertTrue(prompt!!.mandatory.not())
        assertEquals("Update Available", prompt.title)
    }

    // --- Mode 2: mandatory ------------------------------------------------//

    @Test
    fun `mode 2 prompts mandatorily`() {
        val prompt = decideUpdate(config(mode = 2))
        assertTrue(prompt!!.mandatory)
        assertEquals("Update Required", prompt.title)
    }

    // --- Version strings are display-only ----------------------------------//

    @Test
    fun `version strings never gate the decision`() {
        // Any version text must not change the outcome.
        for (mode in listOf(1, 2)) {
            val plain = decideUpdate(config(mode = mode, enabled = false)) // off
            assertNull(plain)
            val prompt = decideUpdate(config(mode = mode))
            assertEquals(mode == 2, prompt!!.mandatory)
        }
    }

    // --- Message fallbacks ------------------------------------------------//

    @Test
    fun `blank message falls back to a sensible default`() {
        val prompt = decideUpdate(config())
        assertTrue(prompt!!.message.isNotBlank())
    }

    @Test
    fun `backend message is used verbatim when present`() {
        val prompt = decideUpdate(config().copy(message = "Update now!"))
        assertEquals("Update now!", prompt!!.message)
    }
}

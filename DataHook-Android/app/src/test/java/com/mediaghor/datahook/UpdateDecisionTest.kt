package com.mediaghor.datahook

import com.mediaghor.datahook.data.remote.dto.AppUpdateConfig
import com.mediaghor.datahook.update.decideUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateDecisionTest {

    private fun config(
        enabled: Boolean = true,
        mode: Int = 1,
        latest: Int = 12,
        minimum: Int = 10,
    ) = AppUpdateConfig(
        enabled = enabled,
        mode = mode,
        latestVersion = latest,
        minimumVersion = minimum,
    )

    // --- Mode 0 / disabled: never prompt ------------------------------------//

    @Test
    fun `disabled config never prompts`() {
        assertNull(decideUpdate(config(enabled = false, mode = 1), 2))
        assertNull(decideUpdate(config(enabled = false, mode = 2), 2))
    }

    @Test
    fun `mode 0 never prompts`() {
        assertNull(decideUpdate(config(mode = 0), 2))
    }

    @Test
    fun `unknown mode never prompts`() {
        assertNull(decideUpdate(config(mode = 99), 2))
    }

    // --- Mode 1: optional ------------------------------------------------//

    @Test
    fun `mode 1 prompts when installed version is below latest`() {
        val prompt = decideUpdate(config(mode = 1), 11)
        assertTrue(prompt!!.mandatory.not())
        assertEquals("Update Available", prompt.title)
    }

    @Test
    fun `mode 1 does not prompt when installed version equals latest`() {
        assertNull(decideUpdate(config(mode = 1), 12))
    }

    @Test
    fun `mode 1 does not prompt when installed version is above latest`() {
        assertNull(decideUpdate(config(mode = 1), 13))
    }

    // --- Mode 2: mandatory ------------------------------------------------//

    @Test
    fun `mode 2 prompts when installed version is below minimum`() {
        val prompt = decideUpdate(config(mode = 2, minimum = 10), 9)
        assertTrue(prompt!!.mandatory)
        assertEquals("Update Required", prompt.title)
    }

    @Test
    fun `mode 2 does not prompt at or above minimum even if below latest`() {
        assertNull(decideUpdate(config(mode = 2, minimum = 10), 10))
        assertNull(decideUpdate(config(mode = 2, minimum = 10), 11))
    }

    // --- Message fallbacks ------------------------------------------------//

    @Test
    fun `blank message falls back to a sensible default`() {
        val prompt = decideUpdate(config(), 11)
        assertTrue(prompt!!.message.isNotBlank())
    }

    @Test
    fun `backend message is used verbatim when present`() {
        val prompt = decideUpdate(config().copy(message = "Update now!"), 11)
        assertEquals("Update now!", prompt!!.message)
    }
}

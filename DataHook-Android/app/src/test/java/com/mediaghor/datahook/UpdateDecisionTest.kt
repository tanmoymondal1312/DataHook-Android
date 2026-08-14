package com.mediaghor.datahook

import com.mediaghor.datahook.data.remote.dto.AppUpdateConfig
import com.mediaghor.datahook.update.decideUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateDecisionTest {

    // Installed versionCode of a hypothetical user on release 3 (versionCode 3).
    private val installed = 3

    private fun config(
        enabled: Boolean = true,
        mode: Int = 1,
        newVersionCode: Int = 4,
    ) = AppUpdateConfig(
        enabled = enabled,
        mode = mode,
        currentVersion = "2.4",
        newVersion = "3.89",
        newVersionCode = newVersionCode,
    )

    // --- Version gating: updated users are never prompted ------------------//

    @Test
    fun `user on the target version is never prompted`() {
        // installed == new_version_code → no prompt (they just updated)
        assertNull(decideUpdate(config(mode = 1, newVersionCode = installed), installed))
        assertNull(decideUpdate(config(mode = 2, newVersionCode = installed), installed))
    }

    @Test
    fun `user past the target version is never prompted`() {
        // installed > new_version_code → no prompt (future-proofing)
        assertNull(decideUpdate(config(newVersionCode = installed - 1), installed))
        assertNull(decideUpdate(config(mode = 2, newVersionCode = 1), installed))
    }

    @Test
    fun `zero new_version_code never prompts`() {
        // Safe default: admin hasn't configured a target release yet.
        assertNull(decideUpdate(config(newVersionCode = 0), installed))
    }

    @Test
    fun `outdated user is prompted`() {
        val prompt = decideUpdate(config(newVersionCode = 5), installed)
        assertTrue(prompt!!.mandatory.not())
    }

    // --- Disabled / mode 0: never prompt ------------------------------------//

    @Test
    fun `disabled config never prompts`() {
        assertNull(decideUpdate(config(enabled = false, mode = 1), installed))
        assertNull(decideUpdate(config(enabled = false, mode = 2), installed))
    }

    @Test
    fun `mode 0 never prompts`() {
        assertNull(decideUpdate(config(mode = 0), installed))
    }

    @Test
    fun `unknown mode never prompts`() {
        assertNull(decideUpdate(config(mode = 99), installed))
    }

    // --- Mode 1: optional ------------------------------------------------//

    @Test
    fun `mode 1 prompts optionally`() {
        val prompt = decideUpdate(config(mode = 1), installed)
        assertTrue(prompt!!.mandatory.not())
        assertEquals("Update Available", prompt.title)
    }

    // --- Mode 2: mandatory ------------------------------------------------//

    @Test
    fun `mode 2 prompts mandatorily`() {
        val prompt = decideUpdate(config(mode = 2), installed)
        assertTrue(prompt!!.mandatory)
        assertEquals("Update Required", prompt.title)
    }

    // --- Version strings are display-only ----------------------------------//

    @Test
    fun `version strings never gate the decision`() {
        for (mode in listOf(1, 2)) {
            val prompt = decideUpdate(config(mode = mode), installed)
            assertEquals(mode == 2, prompt!!.mandatory)
        }
    }

    // --- Message fallbacks ------------------------------------------------//

    @Test
    fun `blank message falls back to a sensible default`() {
        val prompt = decideUpdate(config(), installed)
        assertTrue(prompt!!.message.isNotBlank())
    }

    @Test
    fun `backend message is used verbatim when present`() {
        val prompt = decideUpdate(config().copy(message = "Update now!"), installed)
        assertEquals("Update now!", prompt!!.message)
    }
}
package de.servicehealth.poppmodule.sdk.egk

/**
 * In-memory eGK. Returns [responsesByCommand] for known command APDUs, else [default]. If
 * [throwOnCommand] matches a command, it throws (simulating card removal). Records every command.
 */
internal class FakeCard(
    private val responsesByCommand: Map<String, String> = emptyMap(),
    private val default: String = "9000",
    private val throwOnCommand: String? = null,
) : EgkApduChannel {
    val transceived = mutableListOf<String>()

    override suspend fun transceive(commandApduHex: String): String {
        transceived += commandApduHex
        if (commandApduHex == throwOnCommand) throw IllegalStateException("card removed")
        return responsesByCommand[commandApduHex] ?: default
    }
}

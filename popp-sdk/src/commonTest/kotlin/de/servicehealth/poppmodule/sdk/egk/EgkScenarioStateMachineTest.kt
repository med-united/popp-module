package de.servicehealth.poppmodule.sdk.egk

import de.servicehealth.poppmodule.sdk.PoppSdkError
import de.servicehealth.poppmodule.sdk.egk.protocol.ErrorMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.ScenarioResponseMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.ScenarioStep
import de.servicehealth.poppmodule.sdk.egk.protocol.StandardScenarioMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.StartMessage
import de.servicehealth.poppmodule.sdk.egk.protocol.TokenMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EgkScenarioStateMachineTest {

    private val sessionId = "sess-1"
    private val machine = EgkScenarioStateMachine

    private fun initial() = EgkScenarioState.initial(sessionId)

    private fun scenario(seq: Int, steps: List<ScenarioStep>, timeSpan: Int = 1000) =
        StandardScenarioMessage("1.0.0", sessionId, seq, timeSpan, steps)

    @Test
    fun begin_sends_start_and_awaits_scenario() {
        val (state, actions) = machine.next(initial(), EgkEvent.Begin)
        assertEquals(EgkScenarioState.Phase.AwaitingScenario, state.phase)
        assertEquals(listOf(EgkAction.Send(StartMessage(clientSessionId = sessionId))), actions)
    }

    @Test
    fun scenario_with_steps_triggers_transceive() {
        var state = machine.next(initial(), EgkEvent.Begin).first
        val steps = listOf(ScenarioStep("00A4040C", listOf("9000")))
        val (next, actions) = machine.next(state, EgkEvent.ServerMessage(scenario(seq = 0, steps = steps)))
        assertEquals(EgkScenarioState.Phase.AwaitingApdus, next.phase)
        assertEquals(listOf(EgkAction.Transceive(steps, scenarioIndex = 0)), actions)
    }

    @Test
    fun empty_scenario_answers_immediately_without_transceive() {
        var state = machine.next(initial(), EgkEvent.Begin).first
        val (next, actions) = machine.next(state, EgkEvent.ServerMessage(scenario(seq = 0, steps = emptyList())))
        assertEquals(EgkScenarioState.Phase.AwaitingScenario, next.phase)
        assertEquals(listOf(EgkAction.Send(ScenarioResponseMessage(emptyList()))), actions)
    }

    @Test
    fun apdus_transceived_sends_scenario_response() {
        var state = machine.next(initial(), EgkEvent.Begin).first
        state = machine.next(state, EgkEvent.ServerMessage(scenario(0, listOf(ScenarioStep("00", listOf("9000")))))).first
        val (next, actions) = machine.next(state, EgkEvent.ApdusTransceived(listOf("9000")))
        assertEquals(EgkScenarioState.Phase.AwaitingScenario, next.phase)
        assertEquals(listOf(EgkAction.Send(ScenarioResponseMessage(listOf("9000")))), actions)
    }

    @Test
    fun second_scenario_increments_scenario_index_and_accepts_next_sequence() {
        var state = machine.next(initial(), EgkEvent.Begin).first
        state = machine.next(state, EgkEvent.ServerMessage(scenario(0, listOf(ScenarioStep("00", listOf("9000")))))).first
        state = machine.next(state, EgkEvent.ApdusTransceived(listOf("9000"))).first
        val (_, actions) = machine.next(state, EgkEvent.ServerMessage(scenario(1, listOf(ScenarioStep("01", listOf("9000"))))))
        assertEquals(listOf(EgkAction.Transceive(listOf(ScenarioStep("01", listOf("9000"))), scenarioIndex = 1)), actions)
    }

    @Test
    fun token_completes_with_success() {
        var state = machine.next(initial(), EgkEvent.Begin).first
        val (next, actions) = machine.next(state, EgkEvent.ServerMessage(TokenMessage("jwt", "pn")))
        assertEquals(EgkScenarioState.Phase.Terminal, next.phase)
        assertEquals(listOf(EgkAction.Complete(EgkCheckInResult.Success("jwt", "pn"))), actions)
    }

    @Test
    fun error_completes_with_failed() {
        var state = machine.next(initial(), EgkEvent.Begin).first
        val (next, actions) = machine.next(state, EgkEvent.ServerMessage(ErrorMessage("egk_failed", "bad card")))
        assertEquals(EgkScenarioState.Phase.Terminal, next.phase)
        assertEquals(listOf(EgkAction.Complete(EgkCheckInResult.Failed("egk_failed", "bad card"))), actions)
    }

    @Test
    fun mismatched_client_session_id_fails() {
        var state = machine.next(initial(), EgkEvent.Begin).first
        val foreign = StandardScenarioMessage("1.0.0", "other-session", 0, 1000, emptyList())
        val (next, actions) = machine.next(state, EgkEvent.ServerMessage(foreign))
        assertEquals(EgkScenarioState.Phase.Terminal, next.phase)
        assertIs<EgkAction.Fail>(actions.single())
        assertIs<PoppSdkError.Protocol>((actions.single() as EgkAction.Fail).error)
    }

    @Test
    fun sequence_counter_gap_fails() {
        var state = machine.next(initial(), EgkEvent.Begin).first
        state = machine.next(state, EgkEvent.ServerMessage(scenario(0, listOf(ScenarioStep("00", listOf("9000")))))).first
        state = machine.next(state, EgkEvent.ApdusTransceived(listOf("9000"))).first
        val (next, actions) = machine.next(state, EgkEvent.ServerMessage(scenario(5, emptyList())))
        assertEquals(EgkScenarioState.Phase.Terminal, next.phase)
        assertIs<PoppSdkError.Protocol>((actions.single() as EgkAction.Fail).error)
    }

    @Test
    fun unexpected_message_for_phase_fails() {
        var state = machine.next(initial(), EgkEvent.Begin).first
        state = machine.next(state, EgkEvent.ServerMessage(scenario(0, listOf(ScenarioStep("00", listOf("9000")))))).first
        // Now AwaitingApdus; a server StandardScenario here is illegal.
        val (next, actions) = machine.next(state, EgkEvent.ServerMessage(scenario(1, emptyList())))
        assertEquals(EgkScenarioState.Phase.Terminal, next.phase)
        assertIs<PoppSdkError.Protocol>((actions.single() as EgkAction.Fail).error)
    }

    @Test
    fun client_only_message_arriving_as_server_message_fails() {
        val state = machine.next(initial(), EgkEvent.Begin).first
        val (next, actions) = machine.next(state, EgkEvent.ServerMessage(StartMessage(clientSessionId = sessionId)))
        assertEquals(EgkScenarioState.Phase.Terminal, next.phase)
        assertIs<PoppSdkError.Protocol>((actions.single() as EgkAction.Fail).error)
    }

    @Test
    fun begin_in_wrong_phase_fails() {
        val state = machine.next(initial(), EgkEvent.Begin).first // now AwaitingScenario
        val (next, actions) = machine.next(state, EgkEvent.Begin)
        assertEquals(EgkScenarioState.Phase.Terminal, next.phase)
        assertIs<PoppSdkError.Protocol>((actions.single() as EgkAction.Fail).error)
    }

    @Test
    fun apdus_transceived_in_wrong_phase_fails() {
        val state = machine.next(initial(), EgkEvent.Begin).first // AwaitingScenario, not AwaitingApdus
        val (next, actions) = machine.next(state, EgkEvent.ApdusTransceived(listOf("9000")))
        assertEquals(EgkScenarioState.Phase.Terminal, next.phase)
        assertIs<PoppSdkError.Protocol>((actions.single() as EgkAction.Fail).error)
    }
}

package com.salah.callblocker.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.salah.callblocker.CallBlockerApp
import com.salah.callblocker.data.BlockedCall
import com.salah.callblocker.data.PatternType
import com.salah.callblocker.data.RuleAction
import com.salah.callblocker.domain.RuleMatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class CallBlockerScreeningService : CallScreeningService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val isIncoming = callDetails.callDirection == Call.Details.DIRECTION_INCOMING
        val raw = callDetails.handle?.schemeSpecificPart
        if (!isIncoming) {
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }
        if (raw.isNullOrBlank()) {
            // No caller ID (private / withheld number).
            val blockUnknown = CallBlockerApp.settings(applicationContext).blockUnknownNow()
            if (!blockUnknown) {
                respondToCall(callDetails, CallResponse.Builder().build())
                return
            }
            scope.launch { rejectUnknown(callDetails) }
            return
        }
        scope.launch {
            val ctx = applicationContext
            val settings = CallBlockerApp.settings(ctx)

            if (settings.allowContactsNow() && ContactsChecker.isKnownContact(ctx, raw)) {
                respondToCall(callDetails, CallResponse.Builder().build())
                return@launch
            }

            val match = RuleMatcher.firstMatch(raw, CallBlockerApp.repository(ctx).enabledRules())
            if (match == null) {
                respondToCall(callDetails, CallResponse.Builder().build())
                return@launch
            }

            val response = when (match.action) {
                RuleAction.REJECT ->
                    CallResponse.Builder()
                        .setDisallowCall(true)
                        .setRejectCall(true)
                        .setSkipCallLog(false)
                        .setSkipNotification(false)
                        .build()

                RuleAction.VOICEMAIL ->
                    CallResponse.Builder()
                        .setDisallowCall(true)
                        .setRejectCall(false)
                        .setSkipCallLog(false)
                        .setSkipNotification(false)
                        .build()

                RuleAction.SILENCE ->
                    CallResponse.Builder()
                        .setSilenceCall(true)
                        .build()
            }

            respondToCall(callDetails, response)

            CallBlockerApp.repository(ctx).logBlocked(
                BlockedCall(
                    number = raw,
                    matchedRuleId = match.id,
                    matchedPattern = match.pattern,
                    matchedType = match.type,
                    action = match.action,
                ),
            )

            if (settings.notifyOnBlockNow()) {
                Notifier.notifyBlocked(ctx, raw, match.pattern, match.action)
            }
        }
    }

    private suspend fun rejectUnknown(callDetails: Call.Details) {
        val ctx = applicationContext
        respondToCall(
            callDetails,
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build(),
        )

        CallBlockerApp.repository(ctx).logBlocked(
            BlockedCall(
                number = UNKNOWN_NUMBER_LABEL,
                matchedRuleId = null,
                matchedPattern = UNKNOWN_NUMBER_LABEL,
                matchedType = PatternType.EXACT,
                action = RuleAction.REJECT,
            ),
        )

        if (CallBlockerApp.settings(ctx).notifyOnBlockNow()) {
            Notifier.notifyBlocked(ctx, UNKNOWN_NUMBER_LABEL, UNKNOWN_NUMBER_LABEL, RuleAction.REJECT)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private companion object {
        const val UNKNOWN_NUMBER_LABEL = "Unknown"
    }
}

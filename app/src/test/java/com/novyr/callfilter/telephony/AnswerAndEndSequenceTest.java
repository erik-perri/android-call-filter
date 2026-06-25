package com.novyr.callfilter.telephony;

import android.media.AudioManager;
import android.telephony.TelephonyManager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.InOrder;

public class AnswerAndEndSequenceTest {
    /** A call that is already connected and ends cleanly. */
    private static class ImmediateConnectTelephony implements AnswerAndEndSequence.Telephony {
        @Override
        public void attemptAnswer() {
        }

        @Override
        public boolean endCall() {
            return true;
        }

        @Override
        public int getCallState() {
            return TelephonyManager.CALL_STATE_OFFHOOK;
        }
    }

    @Test
    public void run_idlePhone_mutesThenRestoresMicrophone() {
        AudioManager audioManager = mock(AudioManager.class);
        when(audioManager.getMode()).thenReturn(AudioManager.MODE_RINGTONE);
        when(audioManager.isMicrophoneMute()).thenReturn(false);

        AnswerAndEndResult result = AnswerAndEndSequence.run(
                audioManager,
                new ImmediateConnectTelephony(),
                AnswerAndEndSequence.MuteGuard.NO_OP
        );

        assertEquals(AnswerAndEndResult.ANSWERED_AND_ENDED, result);
        verify(audioManager).setMicrophoneMute(true);
        verify(audioManager).setMicrophoneMute(false);
    }

    @Test
    public void run_callAlreadyActive_doesNotTouchMicrophone() {
        AudioManager audioManager = mock(AudioManager.class);
        when(audioManager.getMode()).thenReturn(AudioManager.MODE_IN_CALL);

        AnswerAndEndSequence.run(
                audioManager,
                new ImmediateConnectTelephony(),
                AnswerAndEndSequence.MuteGuard.NO_OP
        );

        verify(audioManager, never()).setMicrophoneMute(anyBoolean());
    }

    @Test
    public void run_alreadyMuted_leavesMicrophoneAlone() {
        AudioManager audioManager = mock(AudioManager.class);
        when(audioManager.getMode()).thenReturn(AudioManager.MODE_RINGTONE);
        when(audioManager.isMicrophoneMute()).thenReturn(true);

        AnswerAndEndSequence.run(
                audioManager,
                new ImmediateConnectTelephony(),
                AnswerAndEndSequence.MuteGuard.NO_OP
        );

        verify(audioManager, never()).setMicrophoneMute(anyBoolean());
    }

    @Test
    public void run_noAudioManager_stillCompletes() {
        assertEquals(
                AnswerAndEndResult.ANSWERED_AND_ENDED,
                AnswerAndEndSequence.run(
                        null,
                        new ImmediateConnectTelephony(),
                        AnswerAndEndSequence.MuteGuard.NO_OP
                )
        );
    }

    @Test
    public void run_endCallFails_restoresMicrophoneAndReportsFailure() {
        AudioManager audioManager = mock(AudioManager.class);
        when(audioManager.getMode()).thenReturn(AudioManager.MODE_RINGTONE);
        when(audioManager.isMicrophoneMute()).thenReturn(false);

        AnswerAndEndResult result = AnswerAndEndSequence.run(
                audioManager,
                new ImmediateConnectTelephony() {
                    @Override
                    public boolean endCall() {
                        return false;
                    }
                },
                AnswerAndEndSequence.MuteGuard.NO_OP
        );

        assertEquals(AnswerAndEndResult.FAILED, result);
        verify(audioManager).setMicrophoneMute(false);
    }

    @Test
    public void run_idlePhone_armsGuardBeforeMutingAndDisarmsAfterRestoring() {
        AudioManager audioManager = mock(AudioManager.class);
        when(audioManager.getMode()).thenReturn(AudioManager.MODE_RINGTONE);
        when(audioManager.isMicrophoneMute()).thenReturn(false);
        AnswerAndEndSequence.MuteGuard guard = mock(AnswerAndEndSequence.MuteGuard.class);

        AnswerAndEndSequence.run(audioManager, new ImmediateConnectTelephony(), guard);

        // The marker must be persisted before the mute and only cleared once it is restored.
        InOrder order = inOrder(guard, audioManager);
        order.verify(guard).arm();
        order.verify(audioManager).setMicrophoneMute(true);
        order.verify(audioManager).setMicrophoneMute(false);
        order.verify(guard).disarm();
    }

    @Test
    public void run_muteThrows_disarmsGuardSoNoMarkerIsLeftBehind() {
        AudioManager audioManager = mock(AudioManager.class);
        when(audioManager.getMode()).thenReturn(AudioManager.MODE_RINGTONE);
        when(audioManager.isMicrophoneMute()).thenReturn(false);
        doThrow(new SecurityException()).when(audioManager).setMicrophoneMute(true);
        AnswerAndEndSequence.MuteGuard guard = mock(AnswerAndEndSequence.MuteGuard.class);

        AnswerAndEndSequence.run(audioManager, new ImmediateConnectTelephony(), guard);

        // Mute never took effect, so the marker armed beforehand must be cleared.
        verify(guard).arm();
        verify(guard).disarm();
    }

    @Test
    public void run_callAlreadyActive_doesNotTouchGuard() {
        AudioManager audioManager = mock(AudioManager.class);
        when(audioManager.getMode()).thenReturn(AudioManager.MODE_IN_CALL);
        AnswerAndEndSequence.MuteGuard guard = mock(AnswerAndEndSequence.MuteGuard.class);

        AnswerAndEndSequence.run(audioManager, new ImmediateConnectTelephony(), guard);

        verify(guard, never()).arm();
        verify(guard, never()).disarm();
    }
}

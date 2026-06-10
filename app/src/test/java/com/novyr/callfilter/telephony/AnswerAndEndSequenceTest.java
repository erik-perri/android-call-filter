package com.novyr.callfilter.telephony;

import android.media.AudioManager;
import android.telephony.TelephonyManager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
                new ImmediateConnectTelephony()
        );

        assertEquals(AnswerAndEndResult.ANSWERED_AND_ENDED, result);
        verify(audioManager).setMicrophoneMute(true);
        verify(audioManager).setMicrophoneMute(false);
    }

    @Test
    public void run_callAlreadyActive_doesNotTouchMicrophone() {
        AudioManager audioManager = mock(AudioManager.class);
        when(audioManager.getMode()).thenReturn(AudioManager.MODE_IN_CALL);

        AnswerAndEndSequence.run(audioManager, new ImmediateConnectTelephony());

        verify(audioManager, never()).setMicrophoneMute(anyBoolean());
    }

    @Test
    public void run_alreadyMuted_leavesMicrophoneAlone() {
        AudioManager audioManager = mock(AudioManager.class);
        when(audioManager.getMode()).thenReturn(AudioManager.MODE_RINGTONE);
        when(audioManager.isMicrophoneMute()).thenReturn(true);

        AnswerAndEndSequence.run(audioManager, new ImmediateConnectTelephony());

        verify(audioManager, never()).setMicrophoneMute(anyBoolean());
    }

    @Test
    public void run_noAudioManager_stillCompletes() {
        assertEquals(
                AnswerAndEndResult.ANSWERED_AND_ENDED,
                AnswerAndEndSequence.run(null, new ImmediateConnectTelephony())
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
                }
        );

        assertEquals(AnswerAndEndResult.FAILED, result);
        verify(audioManager).setMicrophoneMute(false);
    }
}

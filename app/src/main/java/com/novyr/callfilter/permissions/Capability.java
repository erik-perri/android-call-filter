package com.novyr.callfilter.permissions;

/**
 * A user-facing optional feature, decoupled from the raw permissions/mechanism behind it. There is
 * deliberately no persisted "user enabled X" flag for these since the rules themselves are the
 * opt-in record, so a capability is in use when an enabled rule references it.
 */
public enum Capability {
    /** Treating known contacts differently from everyone else; needs the contacts permission. */
    CONTACTS_MATCHING,

    /** The answer-then-hang-up rule action; needs answer-calls permissions on newer API levels. */
    HANG_UP,
}

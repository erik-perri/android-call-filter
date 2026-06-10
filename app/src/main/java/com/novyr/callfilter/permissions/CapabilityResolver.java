package com.novyr.callfilter.permissions;

/**
 * Answers whether a capability can currently be used: available on this device and with all of its
 * permissions granted right now. Permissions can be revoked in system settings at any time, so
 * consumers (the rule decision layer, the rule editor) must re-ask rather than cache the answer.
 */
public interface CapabilityResolver {
    boolean isActive(Capability capability);
}

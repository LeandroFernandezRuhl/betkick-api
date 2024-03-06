package com.leandroruhl.betkickapi.model.enums;

/**
 * The Status enumeration represents the various states a football match can be in.
 * It includes options such as scheduled, in progress, finished, etc.
 */
public enum Status {
    SCHEDULED,
    TIMED,
    IN_PLAY,
    PAUSED,
    EXTRA_TIME,
    PENALTY_SHOOTOUT,
    FINISHED,
    SUSPENDED,
    POSTPONED,
    CANCELLED,
    AWARDED
}

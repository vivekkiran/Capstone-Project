package com.vivek.codemozo.events;

import com.vivek.codemozo.model.Contest;


public final class ContestClickEvent {
    Contest contest;

    public ContestClickEvent(Contest contest) {
        this.contest = contest;
    }

    public Contest getContest() {
        return contest;
    }
}

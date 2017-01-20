package com.vivek.codemozo.events;


public class SnackBarMessageDetailFragmentEvent {
    String msg;

    public SnackBarMessageDetailFragmentEvent(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}

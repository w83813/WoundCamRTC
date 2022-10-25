package org.appspot.apprtc;


public interface AppRTCCallback {
    public void onConnected();

    public void onDisconnected(final String description);

    public void onError(final String description);

}

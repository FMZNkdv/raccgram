package org.telegram.messenger;

public class RaccgramIdHelper {

    public static boolean isEnabled() {
        return RaccgramConfig.getInstance().showIdEnabled;
    }

    public static void setEnabled(boolean value) {
        RaccgramConfig.getInstance().setShowIdEnabled(value);
    }
}
package it.unipi.mywearapp;

import android.app.Application;

/**
 * This class is global at all the activities, and it is the first to be instantiated. It is used for store & load information useful for all the activities
 */
public class GlobalInfoContainer extends Application {
    public static final String USER_PREFERENCES_FILE = "preferences";

    public static final String SELECTED_PERIOD_KEY = "selectedPeriod";
    public static final String IS_CLEAR_BUTTON_KEY = "clearButton";
    public static final String IS_CLEAR_ALL_SWINGS_KEY = "clearAllSwings";
    public static final String ITEM_ID_KEY = "itemID";
    public static final String DATA_KEY = "it.unipi.mywearapp.data";
    public static final String PERIOD_KEY = "it.unipi.mywearapp.period";

    private static Swing defaultSwing;
    private static Swing bestSwing;
    private static boolean showBestSwing;
    private static float favouriteBat;
    private static String ALERT_DIALOG_TITLE;

    public static String getAlertDialogTitle() {
        return ALERT_DIALOG_TITLE;
    }
    public static void setAlertDialogTitle(String alertDialogTitle) {
        ALERT_DIALOG_TITLE = alertDialogTitle;
    }
    public static Swing getDefaultSwing() {
        return defaultSwing;
    }
    public static void setDefaultSwing(Swing defaultSwing) {
        GlobalInfoContainer.defaultSwing = defaultSwing;
    }
    public static Swing getBestSwing() {
        return bestSwing;
    }
    public static void setBestSwing(Swing bestSwing) {
        GlobalInfoContainer.bestSwing = bestSwing;
    }
    public static boolean isShowBestSwing() {
        return showBestSwing;
    }
    public static void setShowBestSwing(boolean showBestSwing) {
        GlobalInfoContainer.showBestSwing = showBestSwing;
    }
    public static float getFavouriteBat() {
        return favouriteBat;
    }
    public static void setFavouriteBat(float favouriteBat) {
        GlobalInfoContainer.favouriteBat = favouriteBat;
    }
}

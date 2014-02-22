package com.paradoxplaza.eu4.replayer.localization;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Class intended for localizing. Uses default locale!
 * Implements singleton pattern.
 */
public class Localizator {

    /** Name of the resource bundle for localization. */
    static private final String RESOURCE_BUNDLE = "l10n";

    /** Key for missing localization. */
    static private final String KEY_NOT_FOUND = "locale.key.notfound";

    /** Holds the singleton's intance. */
    static private volatile Localizator instance = null;

    /**
     * Returns singleton's instance.
     * @return singleton's instance
     */
    public static Localizator getInstance() {
        if (instance == null) { //double checking
            synchronized (Localizator.class) {
                if (instance == null) {
                    instance = new Localizator();
                }
            }
        }
        return instance;
    }

    /**
     * Equal to getInstance().localize(key).
     * @param key string to localize
     * @return localization of the key
     * @see Localizator#localize(String)
     */
    public static String l10n(final String key) {
        return getInstance().localize(key);
    }

    /** Bundle containing the localization. */
    ResourceBundle resourceBundle = null;

    /**
     * Singleton, hence private constructor.
     */
    private Localizator() {

    }

    /**
     * Returns current localization resource bundle.
     * @return current localization resource bundle
     */
    public synchronized ResourceBundle getResourceBundle() {
        if (resourceBundle == null) {
            reloadResourceBundle();
        }
        return resourceBundle;
    }

    /**
     * Localizes the key. If the localization is not found, returns key instead.
     * @param key string to localize
     * @return localization of the key
     */
    public synchronized String localize(final String key) {
        try {
            return getResourceBundle().getString(key);
        } catch(NullPointerException | MissingResourceException e) {
            if (KEY_NOT_FOUND.equals(key)) { //no infinite recursion
                System.err.printf("Localization string \"%1$s\" not found!\n", key);
            } else {
                System.err.printf(localize(KEY_NOT_FOUND), key);
            }
            return "${" + key + "}";
        }
    }

    /**
     * Reloads {@link #resourceBundle}. Uses default locale.
     */
    public synchronized void reloadResourceBundle() {
        try {
            resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
        } catch (MissingResourceException e) {
            if (resourceBundle == null) {
                System.err.printf("Localization resource \"%1$s\" not found!\n", RESOURCE_BUNDLE);
                resourceBundle = new EmptyResourceBundle(); //create empty resource bundle
            } else {
                //keep old resource bundle
                System.err.printf(localize("locale.bundle.notfound"), RESOURCE_BUNDLE);
            }
            e.printStackTrace();
        }
    }
}

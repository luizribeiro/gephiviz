package org.luizribeiro.gephiviz;

public class Settings {

    public static String getApiKey() {
        return System.getenv("GEPHIVIZ_API_KEY");
    }

    public static String getAppSecret() {
        return System.getenv("GEPHIVIZ_APP_SECRET");
    }
}

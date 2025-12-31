package fm.mrc.gymlog;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import java.util.Locale;

public class LocaleHelper {

    public static Context onAttach(Context context) {
        String lang = getPersistedData(context, "de"); // Default Deutsch
        return setLocale(context, lang);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, "de");
    }

    public static Context setLocale(Context context, String language) {
        return updateResources(context, language);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences prefs = context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(SettingsActivity.KEY_LANGUAGE, defaultLanguage);
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        
        config.setLocale(locale);
        context = context.createConfigurationContext(config);
        
        return context;
    }
}

package com.gerefi.app;

import android.app.Application;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.config.ToastConfigurationBuilder;

public class gerefiApplication extends Application {
    static {
        /**
         * We better not touch real classes in order not to trigger class loading
         *
         * Logging#LOG_CLASS_NAME
         * @see ConsoleLogger
         */
        System.setProperty("log.className", "com.devexperts.logging.ConsoleLogger");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this);
        builder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder.class)
                .setMailTo("gerefillc@gmail.com")
                .setSubject("Android Crash")
                .setEnabled(true);

        builder.getPluginConfigurationBuilder(ToastConfigurationBuilder.class);
        ACRA.init(this, builder);
    }
}

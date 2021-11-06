package com.swivel.cc.auth.configuration;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.swivel.cc.auth.exception.AuthServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Class to handle Google Analytics API's
 */

@Configuration
public class GoogleAnalyticsReportConfiguration {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final String appName;

    @Autowired
    public GoogleAnalyticsReportConfiguration(@Value("${googleAnalytics.applicationName}") String appName) {
        this.appName = appName;
    }

    /**
     * Initializes an Analytics Reporting API V4 service object.
     *
     * @return An authorized Analytics Reporting API V4 service object.
     */
    @Bean
    public AnalyticsReporting initializeAnalyticsReporting() {
        try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleCredentials credential = GoogleCredentials
                    .getApplicationDefault()
                    .createScoped(AnalyticsReportingScopes.all());

            return new AnalyticsReporting.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credential))
                    .setApplicationName(appName).build();
        } catch (GeneralSecurityException | IOException e) {
            throw new AuthServiceException("Initializing google analytics failed", e);
        }
    }

}


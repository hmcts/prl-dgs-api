package uk.gov.hmcts.reform.prl.documentgenerator.config.launchdarkly;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class LaunchDarklyClient {
    public static final LDContext PRL_DGS_USER = LDContext.builder("prl-dgs-api")
        .anonymous(true)
        .build();

    private final LDClientInterface internalClient;

    @Autowired
    public LaunchDarklyClient(
        LDClientFactory ldClientFactory,
        @Value("${launchdarkly.sdk-key}") String sdkKey,
        @Value("${launchdarkly.offline-mode:false}") Boolean offlineMode
    ) {
        this.internalClient = ldClientFactory.create(sdkKey, offlineMode);
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    public boolean isFeatureEnabled(String feature) {
        return internalClient.boolVariation(feature, LaunchDarklyClient.PRL_DGS_USER, false);
    }

    public boolean isFeatureEnabled(String feature, LDContext user) {
        return internalClient.boolVariation(feature, user, false);
    }

    private void close() {
        try {
            internalClient.close();
        } catch (IOException e) {
            log.warn("Failed to close LaunchDarkly client", e);
        }
    }
}

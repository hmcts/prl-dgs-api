package uk.gov.hmcts.reform.prl.documentgenerator.service.launchdarkly;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.documentgenerator.config.launchdarkly.LaunchDarklyClient;

@Service
public class LaunchdarklyIntValidationService {

    LaunchDarklyClient launchDarklyClient;

    @Value("${launchdarkly.sdk-key}")
    private String testSecret;

    @Autowired
    public LaunchdarklyIntValidationService(LaunchDarklyClient launchDarklyClient) {
        this.launchDarklyClient = launchDarklyClient;
    }

    public String checkFeatureFlag(String flag) {
        if (launchDarklyClient.isFeatureEnabled(flag)) {
            return "Game on";
        } else {
            return "Keep trying";
        }
    }

    public String getSecretValue() {
        return "hello "+ testSecret;
    }

}

package uk.gov.hmcts.reform.prl.documentgenerator.config.launchdarkly;

import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.documentgenerator.config.launchdarkly.LDClientFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LDClientFactoryTest {
    private LDClientFactory factory;

    @Before
    public void setUp() {
        factory = new LDClientFactory();
    }

    @Test
    public void testCreate() {
        LDClientInterface client = factory.create("test key", true);
        assertNotNull(client);
    }
}

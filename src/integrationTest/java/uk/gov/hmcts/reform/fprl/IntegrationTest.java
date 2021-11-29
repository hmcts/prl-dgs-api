package uk.gov.hmcts.reform.fprl;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import org.apache.http.entity.ContentType;
import org.assertj.core.util.Strings;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import javax.annotation.PostConstruct;

@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
public abstract class IntegrationTest {

    @Value("${prl.document.generator.uri}")
    protected String prlDocumentGeneratorURI;

    @Value("${prl.document.generateDraft.uri}")
    protected String prlDocumentGenerateDraftURI;

    @Value("${document.management.store.baseUrl}")
    protected String documentManagementURL;

    @Value("${http.proxy:#{null}}")
    protected String httpProxy;

    @Value("${idam.user.genericpassword}")
    protected String aatPassword;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamUtils idamTestSupportUtil;

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration;

    private static String userToken = null;
    private String username;

    public IntegrationTest() {
        this.springMethodIntegration = new SpringIntegrationMethodRule();
    }

    @PostConstruct
    public void init() {
        if (!Strings.isNullOrEmpty(httpProxy)) {
            configProxyHost();
        }
    }

    public Response callPrlDocumentGenerator(String requestBody) {
        return DocumentGeneratorUtil.generatePDF(requestBody,
            prlDocumentGeneratorURI,
            getUserToken());
    }

    public Response callInvalidPrlDocumentGenerator(String requestBody) {
        return DocumentGeneratorUtil.generatePDF(requestBody,
            prlDocumentGeneratorURI,
            getToken());
    }

    private String getToken() {
        String authHeaderToken = null;
        String userLoginDetails = String.join(":", username, aatPassword);
        authHeaderToken = "Bearer " + new String(Base64.getEncoder().encode((userLoginDetails).getBytes()));

        return authHeaderToken;
    }

    private synchronized String getUserToken() {
        username = "simulate-delivered" + UUID.randomUUID() + "@testhmcts.net";

        if (userToken == null) {
            idamTestSupportUtil.createCaseworkerUserInIdam(username, aatPassword);

            userToken = idamTestSupportUtil.generateUserTokenWithNoRoles(username, aatPassword);
        }

        return userToken;
    }

    public String getAuthorizationToken() {
        String authToken = getUserToken();
        return authToken;
    }

    public String getStoredUserToken() {
        return userToken;
    }

    public String constructHealthUrl(String apiName) {
        String url = null;
        url = idamTestSupportUtil.generateUrlForValidMicroservice(apiName);
        return url;
    }

    private void configProxyHost() {
        try {
            URL proxy = new URL(httpProxy);
            if (InetAddress.getByName(proxy.getHost()).isReachable(2000)) {
                System.setProperty("http.proxyHost", proxy.getHost());
                System.setProperty("http.proxyPort", Integer.toString(proxy.getPort()));
                System.setProperty("https.proxyHost", proxy.getHost());
                System.setProperty("https.proxyPort", Integer.toString(proxy.getPort()));
            } else {
                throw new IOException();
            }
        } catch (IOException e) {
            log.error("Error setting up proxy - are you connected to the VPN?", e);
            throw new RuntimeException(e);
        }
    }
}

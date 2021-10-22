package uk.gov.hmcts.reform.prl.documentgenerator.management.monitoring.health;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.jayway.jsonpath.JsonPath;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.WireMockSpring;
import org.springframework.cloud.netflix.ribbon.StaticServerList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.documentgenerator.DocumentGeneratorApplication;
import uk.gov.hmcts.reform.prl.documentgenerator.functionaltest.ConnectionCloseExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Ignore("As there is an issue with healthcheck")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(
        classes = {DocumentGeneratorApplication.class, HealthCheckITest.LocalRibbonClientConfiguration.class})
@PropertySource(value = "classpath:application.properties")
@TestPropertySource(properties = {
        "management.endpoint.health.cache.time-to-live=0",
        "feign.hystrix.enabled=true",
        "eureka.client.enabled=false",
    })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class HealthCheckITest {

    private static final String HEALTH_UP_RESPONSE = "{ \"status\": \"UP\"}";
    private static final String HEALTH_DOWN_RESPONSE = "{ \"status\": \"DOWN\"}";

    @LocalServerPort
    private int port;

    @ClassRule
    public static WireMockClassRule serviceAuthServer = new WireMockClassRule(buildWireMockConfig(4502));

    @ClassRule
    public static WireMockClassRule docmosisService = new WireMockClassRule(buildWireMockConfig(5501));

    @ClassRule
    public static WireMockClassRule caseDocService = new WireMockClassRule(buildWireMockConfig(5170));

    private String healthUrl;
    private final HttpClient httpClient = HttpClients.createMinimal();

    private HttpResponse getHealth() throws Exception {
        final HttpGet request = new HttpGet(healthUrl);
        request.addHeader("Accept", "application/json;charset=UTF-8");

        return httpClient.execute(request);
    }

    @Before
    public void setUp() {
        healthUrl = "http://localhost:" + String.valueOf(port) + "/health";
    }

    @After
    public void tearDown() {
        resetAllMockServices();
    }

    private static WireMockConfiguration buildWireMockConfig(int port) {
        return WireMockSpring
            .options()
            .port(port)
            .jettyStopTimeout(20L)
            .extensions(new ConnectionCloseExtension());
    }

    @Test
    public void givenAllDependenciesAreUp_whenCheckHealth_thenReturnStatusUp() throws Exception {
        stubEndpointAndResponse(serviceAuthServer, true);
        stubEndpointAndResponse(docmosisService, true);
        stubEndpointAndResponse(caseDocService, true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.OK.value()));
        assertThat(JsonPath.read(body, "$.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.docmosisHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.caseDocumentManagement.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.diskSpace.status").toString(),
            equalTo("UP"));
    }

    @Test
    public void givenAllDependenciesAreDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        stubEndpointAndResponse(serviceAuthServer, false);
        stubEndpointAndResponse(docmosisService, false);
        stubEndpointAndResponse(caseDocService, false);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SERVICE_UNAVAILABLE.value()));
        assertThat(JsonPath.read(body, "$.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.docmosisHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.caseDocumentManagement.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.diskSpace.status").toString(),
            equalTo("UP"));
    }

    @Test
    public void givenDocmosisServiceIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        stubEndpointAndResponse(serviceAuthServer, true);
        stubEndpointAndResponse(docmosisService, false);
        stubEndpointAndResponse(caseDocService, true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SERVICE_UNAVAILABLE.value()));
        assertThat(JsonPath.read(body, "$.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.docmosisHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.caseDocumentManagement.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.diskSpace.status").toString(),
            equalTo("UP"));
    }

    @Test
    public void givenCaseDocumentsClientIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        stubEndpointAndResponse(serviceAuthServer, true);
        stubEndpointAndResponse(docmosisService, true);
        stubEndpointAndResponse(caseDocService, false);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SERVICE_UNAVAILABLE.value()));
        assertThat(JsonPath.read(body, "$.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.docmosisHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.caseDocumentManagement.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.diskSpace.status").toString(),
            equalTo("UP"));
    }

    @Test
    public void givenAuthServiceIsDown_whenCheckHealth_thenReturnStatusDown() throws Exception {
        stubEndpointAndResponse(serviceAuthServer, false);
        stubEndpointAndResponse(docmosisService, true);
        stubEndpointAndResponse(caseDocService, true);

        HttpResponse response = getHealth();
        String body = EntityUtils.toString(response.getEntity());

        assertThat(response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SERVICE_UNAVAILABLE.value()));
        assertThat(JsonPath.read(body, "$.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.serviceAuthProviderHealthCheck.status").toString(),
            equalTo("DOWN"));
        assertThat(JsonPath.read(body, "$.components.docmosisHealthCheck.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.caseDocumentManagement.status").toString(),
            equalTo("UP"));
        assertThat(JsonPath.read(body, "$.components.diskSpace.status").toString(),
            equalTo("UP"));
    }

    private void stubEndpointAndResponse(WireMockClassRule mockServer, boolean serviceUp) {
        mockServer.stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(serviceUp ? HttpStatus.OK.value() : HttpStatus.SERVICE_UNAVAILABLE.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(serviceUp ? HEALTH_UP_RESPONSE : HEALTH_DOWN_RESPONSE)));
    }

    protected final void resetAllMockServices() {
        serviceAuthServer.resetAll();
        docmosisService.resetAll();
    }

    @TestConfiguration
    public static class LocalRibbonClientConfiguration {
        @Bean
        public ServerList<Server> ribbonServerList(@Value("${auth.provider.service.client.port}") int serverPort) {
            return new StaticServerList<>(new Server("localhost", serverPort));
        }
    }
}

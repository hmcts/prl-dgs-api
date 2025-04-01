package uk.gov.hmcts.reform.prl.documentgenerator.management.monitoring.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebServiceHealthCheckUTest {
    private static final String URI = "https://example.com";
    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final HttpEntityFactory httpEntityFactory = mock(HttpEntityFactory.class);

    private final TestWebServiceHealthCheck healthCheck = new TestWebServiceHealthCheck(httpEntityFactory, restTemplate,
            URI);

    @Test
    void givenServiceReturnsOkWhenHealthThenReturnUp() {
        final HttpEntity<Object> httpEntity = new HttpEntity<>(null);
        final ResponseEntity<Object> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        when(httpEntityFactory.createRequestEntityForHealthCheck()).thenReturn(httpEntity);

        when(restTemplate.exchange(URI, HttpMethod.GET, httpEntity, Object.class, new HashMap<>()))
                .thenReturn(responseEntity);

        assertThat(healthCheck.health()).isEqualTo(Health.up().build());

        verify(httpEntityFactory).createRequestEntityForHealthCheck();
        verify(restTemplate).exchange(URI, HttpMethod.GET, httpEntity, Object.class,
                new HashMap<>());

        verifyNoMoreInteractions(httpEntityFactory, restTemplate);
    }

    @Test
    void givenServiceReturnsServiceUnavailableWhenHealthThenReturnDown() {
        final HttpEntity<Object> httpEntity = new HttpEntity<>(null);

        when(httpEntityFactory.createRequestEntityForHealthCheck()).thenReturn(httpEntity);

        HttpServerErrorException exception = new HttpServerErrorException(
            HttpStatus.SERVICE_UNAVAILABLE,"unknown error"
        );

        doThrow(exception).when(restTemplate)
                .exchange(URI, HttpMethod.GET, httpEntity, Object.class, new HashMap<>());

        assertThat(healthCheck.health()).isEqualTo(Health.down().build());

        verify(httpEntityFactory).createRequestEntityForHealthCheck();
        verify(restTemplate).exchange(URI, HttpMethod.GET, httpEntity, Object.class,
                new HashMap<>());

        verifyNoMoreInteractions(httpEntityFactory, restTemplate);
    }

    @Test
    void whenResourceAccessExceptionIsThrownWhenHealthThenReturnDown() {
        final HttpEntity<Object> httpEntity = new HttpEntity<>(null);

        when(httpEntityFactory.createRequestEntityForHealthCheck()).thenReturn(httpEntity);

        ResourceAccessException exception = new ResourceAccessException("no resource found");

        doThrow(exception).when(restTemplate)
                .exchange(URI, HttpMethod.GET, httpEntity, Object.class, new HashMap<>());

        assertThat(healthCheck.health()).isEqualTo(Health.down().build());

        verify(httpEntityFactory).createRequestEntityForHealthCheck();
        verify(restTemplate).exchange(URI, HttpMethod.GET, httpEntity, Object.class,
                new HashMap<>());

        verifyNoMoreInteractions(httpEntityFactory, restTemplate);
    }

    @Test
    void givenExceptionIsThrownWhenHealthThenReturnUnknown() {
        final HttpEntity<Object> httpEntity = new HttpEntity<>(null);

        when(httpEntityFactory.createRequestEntityForHealthCheck()).thenReturn(httpEntity);

        RuntimeException exception = new RuntimeException("internal server error");

        doThrow(exception).when(restTemplate)
                .exchange(URI, HttpMethod.GET, httpEntity, Object.class, new HashMap<>());

        assertThat(healthCheck.health()).isEqualTo(Health.unknown().build());

        verify(httpEntityFactory).createRequestEntityForHealthCheck();
        verify(restTemplate).exchange(URI, HttpMethod.GET, httpEntity, Object.class,
                new HashMap<>());

        verifyNoMoreInteractions(httpEntityFactory, restTemplate);
    }

    @Test
    void givenUpstreamStatusIsNot200or503WhenHealthThenReturnUnknownStatus() {
        final HttpEntity<Object> httpEntity = new HttpEntity<>(null);
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(HttpStatus.MOVED_PERMANENTLY);

        when(httpEntityFactory.createRequestEntityForHealthCheck()).thenReturn(httpEntity);

        when(restTemplate.exchange(URI, HttpMethod.GET, httpEntity, Object.class, new HashMap<>()))
                .thenReturn(responseEntity);

        assertThat(healthCheck.health()).isEqualTo(Health.unknown().build());

        verify(httpEntityFactory).createRequestEntityForHealthCheck();
        verify(restTemplate).exchange(URI, HttpMethod.GET, httpEntity, Object.class,
                new HashMap<>());

        verifyNoMoreInteractions(httpEntityFactory, restTemplate);
    }

    private static class TestWebServiceHealthCheck extends WebServiceHealthCheck {
        TestWebServiceHealthCheck(HttpEntityFactory httpEntityFactory, RestTemplate restTemplate, String uri) {
            super(httpEntityFactory, restTemplate, uri);
        }
    }
}

package uk.gov.hmcts.reform.prl.documentgenerator.management.monitoring.health;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
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
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebServiceHealthCheckUTest {
    private static final String URI = "http://example.com";
    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final HttpEntityFactory httpEntityFactory = mock(HttpEntityFactory.class);

    private final TestWebServiceHealthCheck healthCheck = new TestWebServiceHealthCheck(httpEntityFactory, restTemplate,
            URI);

    @Test
    public void givenServiceReturnsOk_whenHealth_thenReturnUp() {
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
    public void givenServiceReturnsServiceUnavailable_whenHealth_thenReturnDown() {
        final HttpEntity<Object> httpEntity = new HttpEntity<>(null);

        when(httpEntityFactory.createRequestEntityForHealthCheck()).thenReturn(httpEntity);

        HttpServerErrorException exception = new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE,"unknown error");

        doThrow(exception).when(restTemplate)
                .exchange(URI, HttpMethod.GET, httpEntity, Object.class, new HashMap<>());

        assertThat(healthCheck.health()).isEqualTo(Health.down().build());

        verify(httpEntityFactory).createRequestEntityForHealthCheck();
        verify(restTemplate).exchange(URI, HttpMethod.GET, httpEntity, Object.class,
                new HashMap<>());

        verifyNoMoreInteractions(httpEntityFactory, restTemplate);
    }

    @Test
    public void whenResourceAccessExceptionIsThrown_whenHealth_thenReturnDown() {
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
    public void givenExceptionIsThrown_whenHealth_thenReturnUnknown() {
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
    public void givenUpstreamStatusIsNot200or503_whenHealth_thenReturnUnknownStatus() {
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

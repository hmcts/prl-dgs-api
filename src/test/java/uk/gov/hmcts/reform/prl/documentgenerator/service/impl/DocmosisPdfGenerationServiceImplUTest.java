package uk.gov.hmcts.reform.prl.documentgenerator.service.impl;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.prl.documentgenerator.exception.PDFGenerationException;
import uk.gov.hmcts.reform.prl.documentgenerator.mapper.TemplateDataMapper;
import uk.gov.hmcts.reform.prl.documentgenerator.util.NullOrEmptyValidator;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class DocmosisPdfGenerationServiceImplUTest {

    private static final String PDF_SERVICE_ENDPOINT = "pdf_service_endpoint";
    private static final String PDF_SERVICE_KEY = "pdf_service_key";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TemplateDataMapper templateDataMapper;


    @InjectMocks
    @Spy
    private final DocmosisPDFGenerationServiceImpl classUnderTest = new DocmosisPDFGenerationServiceImpl();

    @Before
    public void before() throws IllegalAccessException {
        FieldUtils.writeField(classUnderTest, "docmosisPdfServiceEndpoint", "test", true);
    }

    @Test
    public void givenHttpClientErrorExceptionThrown_whenGenerateCalled_thenThrowPdfGenerationException()
        throws Exception {
        final String template = "1";
        final Map<String, Object> placeholders = Collections.emptyMap();
        final HttpClientErrorException httpClientErrorException = Mockito.mock(HttpClientErrorException.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        Mockito.doThrow(httpClientErrorException).when(restTemplate).exchange(Mockito.anyString(),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(),
                ArgumentMatchers.<Class<byte[]>>any());

        try {
            classUnderTest.generate(template, placeholders);
            fail();
        } catch (PDFGenerationException exception) {
            assertEquals(httpClientErrorException, exception.getCause());
        }
        NullOrEmptyValidator.requireNonBlank(template);
    }


    @Test
    public void givenHttpRequestGoesThrough_whenGenerateFromHtml_thenReturnProperResponse() throws Exception {
        final String template = "1";
        final Map<String, Object> placeholders = Collections.emptyMap();
        final HttpClientErrorException httpClientErrorException = Mockito.mock(HttpClientErrorException.class);

        byte[] test = "Any String you want".getBytes();

        ResponseEntity<byte[]> myEntity = new ResponseEntity<byte[]>(test,HttpStatus.ACCEPTED);


        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        Mockito.when(restTemplate.exchange(ArgumentMatchers.any(String.class),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(),
                ArgumentMatchers.<Class<byte[]>>any())).thenReturn(myEntity);


        byte[] expected = classUnderTest.generate(template, placeholders);

        Assert.assertNotNull(expected);
    }

}

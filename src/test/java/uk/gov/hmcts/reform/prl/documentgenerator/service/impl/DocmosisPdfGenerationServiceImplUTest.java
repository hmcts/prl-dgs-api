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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.prl.documentgenerator.exception.PDFGenerationException;
import uk.gov.hmcts.reform.prl.documentgenerator.mapper.TemplateDataMapper;
import uk.gov.hmcts.reform.prl.documentgenerator.util.NullOrEmptyValidator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class DocmosisPdfGenerationServiceImplUTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private TemplateDataMapper templateDataMapper;

    @InjectMocks
    @Spy
    private DocmosisPDFGenerationServiceImpl classUnderTest;

    @Before
    public void before() throws IllegalAccessException {
        FieldUtils.writeField(classUnderTest, "docmosisPdfServiceEndpoint", "test", true);
        FieldUtils.writeField(classUnderTest, "docmosisPdfConvertEndpoint", "test", true);
        FieldUtils.writeField(classUnderTest, "docmosisPdfServiceAccessKey", "test", true);

    }

    @Test
    public void givenHttpClientErrorExceptionThrown_whenGenerateCalled_thenThrowPdfGenerationException() {
        final String template = "1";
        final Map<String, Object> placeholders = Collections.emptyMap();
        final HttpClientErrorException httpClientErrorException = Mockito.mock(HttpClientErrorException.class);

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
    public void givenHttpRequestGoesThrough_whenGenerateFromHtml_thenReturnProperResponse() {
        final String template = "1";
        final Map<String, Object> placeholders = Collections.emptyMap();

        byte[] test = "Any String you want".getBytes();

        ResponseEntity<byte[]> myEntity = new ResponseEntity<>(test, HttpStatus.ACCEPTED);

        Mockito.when(restTemplate.exchange(ArgumentMatchers.any(String.class),
                ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.any(),
                ArgumentMatchers.<Class<byte[]>>any())).thenReturn(myEntity);

        byte[] expected = classUnderTest.generate(template, placeholders);

        Assert.assertNotNull(expected);
    }

    @Test
    public void givenFileName_whenConvertPdf_thenReturnProperResponse() throws Exception {
        final Map<String, Object> placeholders = new HashMap<>();

        byte[] test = "Any String you want".getBytes();
        placeholders.put("fileName", test);

        Mockito.when(restTemplate.postForObject(
                ArgumentMatchers.any(String.class),
                ArgumentMatchers.any(),
                ArgumentMatchers.<Class<byte[]>>any()))
            .thenReturn(test);

        String outputFile = "testFile.pdf";

        try {
            byte[] expected = classUnderTest.convertToPdf(placeholders, outputFile);
            Assert.assertNotNull(expected);
        } finally {
            Files.deleteIfExists(Path.of(outputFile));
        }
    }

}

package uk.gov.hmcts.reform.prl.documentgenerator.controller.helper;


import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.prl.documentgenerator.exception.ErrorLoadingTemplateException;
import uk.gov.hmcts.reform.prl.documentgenerator.exception.PDFGenerationException;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
class GlobalExceptionHandlerUTest {

    private final GlobalExceptionHandler classUnderTest = new GlobalExceptionHandler();

    @Test
    void whenHandleBadRequestException_thenReturnBadRequest() {
        final Exception exception = new Exception();

        ResponseEntity<Object> response = classUnderTest.handleBadRequestException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void whenHandleTemplateLoadingException_thenReturnBadRequest() {
        final String message = "some message";
        final Exception exception = new Exception();
        final ErrorLoadingTemplateException errorLoadingTemplateException =
                new ErrorLoadingTemplateException(message, exception);

        ResponseEntity<Object> response = classUnderTest.handleTemplateLoadingException(errorLoadingTemplateException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(message, response.getBody());
    }

    @Test
    void givenHttpClientErrorExceptionWrappedIn_whenHandleDocumentStorageAndPDFGenerationException_thenReturnStatusCodeOfHttpClientErrorException() {
        final HttpStatus httpStatus = HttpStatus.MOVED_PERMANENTLY;

        final HttpClientErrorException httpClientErrorException = new HttpClientErrorException(httpStatus);
        final String message = "some message";

        final PDFGenerationException pdfGenerationException =
                new PDFGenerationException(message, httpClientErrorException);

        ResponseEntity<Object> response = classUnderTest
                .handleDocumentStorageAndPDFGenerationException(pdfGenerationException);

        assertEquals(httpStatus, response.getStatusCode());
        assertEquals(message, response.getBody());
    }

    @Test
    void givenHttpClientErrorResponseCode200_whenHandleDocumentStorageAndPDFGenerationException_thenReturnStatus503() {
        final HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        final HttpClientErrorException httpClientErrorException = new HttpClientErrorException(httpStatus);
        final String message = "some message";

        final PDFGenerationException pdfGenerationException =
                new PDFGenerationException(message, httpClientErrorException);

        ResponseEntity<Object> response =
                classUnderTest.handleDocumentStorageAndPDFGenerationException(pdfGenerationException);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(message, response.getBody());
    }

    @Test
    void givenWrappedInExceptionIsNull_whenHandleDocumentStorageAndPDFGenerationException_thenReturnInternalServerError() {
        final String message = "some message";

        PDFGenerationException pdfGenerationException = new PDFGenerationException(message, null);

        ResponseEntity<Object> response =
                classUnderTest.handleDocumentStorageAndPDFGenerationException(pdfGenerationException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(message, response.getBody());
    }

    @Test
    void givenWrappedInIsNotHttpClientErrorException_whenHandleDocumentStorageAndPDFGenerationException_thenReturnInternalServerError() {
        final String message = "some message";
        final Exception exception = new Exception();

        PDFGenerationException pdfGenerationException = new PDFGenerationException(message, exception);

        ResponseEntity<Object> response =
                classUnderTest.handleDocumentStorageAndPDFGenerationException(pdfGenerationException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(message, response.getBody());
    }
}

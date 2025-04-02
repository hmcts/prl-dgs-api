package uk.gov.hmcts.reform.prl.documentgenerator.controller.helper;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.prl.documentgenerator.exception.ErrorLoadingTemplateException;
import uk.gov.hmcts.reform.prl.documentgenerator.exception.PDFGenerationException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerUTest {

    private final GlobalExceptionHandler classUnderTest = new GlobalExceptionHandler();

    @Test
    void whenHandleBadRequestExceptionThenReturnBadRequest() {
        final Exception exception = new Exception();

        ResponseEntity<Object> response = classUnderTest.handleBadRequestException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void whenHandleTemplateLoadingExceptionThenReturnBadRequest() {
        final String message = "some message";
        final Exception exception = new Exception();
        final ErrorLoadingTemplateException errorLoadingTemplateException =
                new ErrorLoadingTemplateException(message, exception);

        ResponseEntity<Object> response = classUnderTest.handleTemplateLoadingException(errorLoadingTemplateException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(message, response.getBody());
    }

    @Test
    void whenHandleDocStorageAndPdfGenExceptionThenReturnHttpClientErrorStatus() {
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
    void givenHttpClientError200WhenHandleDocStorageAndPdfGenExceptionThenReturn503() {
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
    void givenNullWrappedInExceptionWhenHandleDocStorageAndPdfGenThenReturn500() {
        final String message = "some message";

        PDFGenerationException pdfGenerationException = new PDFGenerationException(message, null);

        ResponseEntity<Object> response =
                classUnderTest.handleDocumentStorageAndPDFGenerationException(pdfGenerationException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(message, response.getBody());
    }

    @Test
    void givenNonHttpClientErrorWrappedInWhenHandleDocStorageAndPdfGenExceptionThenReturn500() {
        final String message = "some message";
        final Exception exception = new Exception();

        PDFGenerationException pdfGenerationException = new PDFGenerationException(message, exception);

        ResponseEntity<Object> response =
                classUnderTest.handleDocumentStorageAndPDFGenerationException(pdfGenerationException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(message, response.getBody());
    }
}

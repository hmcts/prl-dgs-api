package uk.gov.hmcts.reform.prl.documentgenerator.exception;

public class ErrorLoadingTemplateException extends RuntimeException {
    public ErrorLoadingTemplateException(String message, Throwable cause) {
        super(message, cause);
    }
}

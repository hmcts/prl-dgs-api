package uk.gov.hmcts.reform.prl.documentgenerator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestData {
    public static final String TEST_TEMPLATE = "a-certain-template";
    public static final String TEST_TEMPLATE_FILE_NAME = "fileName.pdf";
    public static final String TEST_AUTH_TOKEN = "someToken";
    public static final byte[] TEST_GENERATED_DOCUMENT = new byte[] {1};
    public static final String TEST_S2S_TOKEN = "s2s authenticated";
    public static final String CASE_TYPE = "C100";
    public static final String JURISDICTION = "PRIVATELAW";

    public static final String FILE_URL = "fileURL";
    public static final String MIME_TYPE = "mimeType";
    public static final String TEST_DEFAULT_NAME_FOR_PDF_FILE = "PRLDocument.pdf";

    public static final String TEST_HASH_TOKEN = "hashToken";
}

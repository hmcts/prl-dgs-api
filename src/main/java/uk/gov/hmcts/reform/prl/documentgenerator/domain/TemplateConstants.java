package uk.gov.hmcts.reform.prl.documentgenerator.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TemplateConstants {
    // Generators
    public static final String DOCMOSIS_TYPE = "docmosis";

    // Template Ids
    public static final String TEST_TEMPLATE = "FL-DIV-GOR-ENG-00062.docx";

    // Template Names
    public static final String TEST_TEMPLATE_NAME_FOR_PDF = "TestTemplate.pdf";

    // Template Data Mapper Constants
    public static final String CASE_DATA = "case_data";
    public static final String CASE_DETAILS = "caseDetails";
    public static final String CCD_DATE_FORMAT = "yyyy-MM-dd";
    public static final String CCD_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String LETTER_DATE_FORMAT = "dd MMMM yyyy";
}

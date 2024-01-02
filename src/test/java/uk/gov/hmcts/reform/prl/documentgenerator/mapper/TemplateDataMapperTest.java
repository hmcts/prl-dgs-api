package uk.gov.hmcts.reform.prl.documentgenerator.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.documentgenerator.config.DocmosisBasePdfConfig;
import uk.gov.hmcts.reform.prl.documentgenerator.exception.PDFGenerationException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.documentgenerator.domain.TemplateConstants.CASE_DATA;
import static uk.gov.hmcts.reform.prl.documentgenerator.domain.TemplateConstants.CASE_DETAILS;
import static uk.gov.hmcts.reform.prl.documentgenerator.domain.TemplateConstants.TEMP_PARTY_NAMES_KEY;

@RunWith(MockitoJUnitRunner.class)
public class TemplateDataMapperTest {

    // Test Values
    private static final String TEST_COURT_ADDRESS = "Placeholder Court";

    // Docmosis Base Config Constants
    private static final String TEMPLATE_KEY = "templateKey";
    private static final String TEMPLATE_VAL = "templateVal";
    private static final String FAMILY_IMG_KEY = "familyImgKey";
    private static final String FAMILY_IMG_VAL = "familyImgVal";
    private static final String HMCTS_IMG_KEY = "hmctsImgKey";
    private static final String HMCTS_IMG_VAL = "hmctsImgVal";

    @Spy
    private ObjectMapper mapper;

    @Mock
    private DocmosisBasePdfConfig docmosisBasePdfConfig;

    @InjectMocks
    private TemplateDataMapper templateDataMapper;

    private Map<String, Object> expectedData;


    @Before
    public void setup() {
        // Mock docmosisBasePdfConfig
        mockDocmosisPdfBaseConfig();

        // Setup base data that will always be added to the payload
        expectedData = new HashMap<>();
        expectedData.put(docmosisBasePdfConfig.getDisplayTemplateKey(), docmosisBasePdfConfig.getDisplayTemplateVal());
        expectedData.put(docmosisBasePdfConfig.getFamilyCourtImgKey(), docmosisBasePdfConfig.getFamilyCourtImgVal());
        expectedData.put(docmosisBasePdfConfig.getHmctsImgKey(), docmosisBasePdfConfig.getHmctsImgVal());
    }

    @Test
    public void givenEmptyRequest_whenTemplateDataMapperIsCalled_returnBaseData() {
        Map<String, Object> caseData = new HashMap<>();

        Map<String, Object> requestData = Collections.singletonMap(
            CASE_DETAILS, Collections.singletonMap(CASE_DATA, caseData)
        );

        Map<String, Object> actual = templateDataMapper.map(requestData);

        assertEquals(expectedData, actual);
    }

    @Test
    public void putAllDataInmMap() {
        Map<String, Object> caseData = new HashMap<>();

        Map<String, Object> actual = templateDataMapper.map(caseData);

        assertEquals(expectedData, actual);
    }

    @Test
    public void putAllPartyNamesInMap() {
        Map<String, Object> partyNamesMap = new HashMap<>();
        Map<String, Object> requestData = Collections.singletonMap(
            CASE_DETAILS, Collections.singletonMap(TEMP_PARTY_NAMES_KEY, partyNamesMap)
        );

        Map<String, Object> actual = templateDataMapper.map(requestData);

        assertEquals(expectedData, actual);
    }

    @Test
    public void formatDateFromCCD_exception() {
        String ccdDate = "15-03-2022";
        assertThrows(PDFGenerationException.class, () -> {
            templateDataMapper.formatDateFromCCD(ccdDate);
        });
    }

    @Test
    public void formatDateFromCCD_success() {
        String ccdDate = "2022-12-03";
        assertEquals("03 December 2022", templateDataMapper.formatDateFromCCD(ccdDate));
    }

    @Test
    public void formatDateTimeFromCCD_exception() {
        String ccdDate = "15-03-2022";
        assertThrows(PDFGenerationException.class, () -> {
            templateDataMapper.formatDateTimeFromCCD(ccdDate);
        });
    }

    @Test
    public void formatDateTimeFromCCD_success() {
        String ccdDate = "2017-11-22T10:10:15.455";
        assertEquals("22 November 2017", templateDataMapper.formatDateTimeFromCCD(ccdDate));
    }

    private void mockDocmosisPdfBaseConfig() {
        when(docmosisBasePdfConfig.getDisplayTemplateKey()).thenReturn(TEMPLATE_KEY);
        when(docmosisBasePdfConfig.getDisplayTemplateVal()).thenReturn(TEMPLATE_VAL);
        when(docmosisBasePdfConfig.getFamilyCourtImgKey()).thenReturn(FAMILY_IMG_KEY);
        when(docmosisBasePdfConfig.getFamilyCourtImgVal()).thenReturn(FAMILY_IMG_VAL);
        when(docmosisBasePdfConfig.getHmctsImgKey()).thenReturn(HMCTS_IMG_KEY);
        when(docmosisBasePdfConfig.getHmctsImgVal()).thenReturn(HMCTS_IMG_VAL);
    }
}

package uk.gov.hmcts.reform.prl.documentgenerator.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.documentgenerator.config.DocmosisBasePdfConfig;
import uk.gov.hmcts.reform.prl.documentgenerator.exception.PDFGenerationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.prl.documentgenerator.domain.TemplateConstants.CASE_DATA;
import static uk.gov.hmcts.reform.prl.documentgenerator.domain.TemplateConstants.CASE_DETAILS;
import static uk.gov.hmcts.reform.prl.documentgenerator.domain.TemplateConstants.CCD_DATE_FORMAT;
import static uk.gov.hmcts.reform.prl.documentgenerator.domain.TemplateConstants.CCD_DATE_TIME_FORMAT;
import static uk.gov.hmcts.reform.prl.documentgenerator.domain.TemplateConstants.LETTER_DATE_FORMAT;
import static uk.gov.hmcts.reform.prl.documentgenerator.domain.TemplateConstants.TEMP_PARTY_NAMES_KEY;

@Component
@RequiredArgsConstructor
public class TemplateDataMapper {

    private final DocmosisBasePdfConfig docmosisBasePdfConfig;

    @SuppressWarnings("unchecked")
    public Map<String, Object> map(Map<String, Object> placeholders) {
        Map<String, Object> data = new HashMap<>();
        // Get case data
        if (placeholders.containsKey(CASE_DETAILS)) {
            Map<String, Object> caseDetails = (Map) placeholders.get(CASE_DETAILS);
            if (caseDetails.containsKey(CASE_DATA)) {
                data = (Map<String, Object>) caseDetails.get(CASE_DATA);
            }
            //EXUI -1144 - party names
            if (placeholders.containsKey(TEMP_PARTY_NAMES_KEY)) {
                data.putAll((Map<String, Object>) placeholders.get(TEMP_PARTY_NAMES_KEY));
            }
        } else {
            data.putAll(placeholders);
        }

        // Get page assets
        data.putAll(getPageAssets());
        return data;
    }

    private Map<String, Object> getPageAssets() {
        Map<String, Object> pageAssets = new HashMap<>();
        pageAssets.put(docmosisBasePdfConfig.getDisplayTemplateKey(), docmosisBasePdfConfig.getDisplayTemplateVal());
        pageAssets.put(docmosisBasePdfConfig.getFamilyCourtImgKey(), docmosisBasePdfConfig.getFamilyCourtImgVal());
        pageAssets.put(docmosisBasePdfConfig.getHmctsImgKey(), docmosisBasePdfConfig.getHmctsImgVal());

        return pageAssets;
    }

    public String formatDateFromCCD(String ccdDateString) {
        try {
            ccdDateString = formatDateFromPattern(ccdDateString, CCD_DATE_FORMAT);
        } catch (Exception e) {
            throw new PDFGenerationException("Unable to format CCD Date Type field", e);
        }
        return ccdDateString;
    }

    public String formatDateTimeFromCCD(String ccdDateString) {
        try {
            ccdDateString = formatDateFromPattern(ccdDateString, CCD_DATE_TIME_FORMAT);
        } catch (Exception e) {
            throw new PDFGenerationException("Unable to format CCD DateTime Type field", e);
        }
        return ccdDateString;
    }

    private String formatDateFromPattern(String ccdDateString, String fromPattern) {
        if (Objects.nonNull(ccdDateString)) {
            DateTimeFormatter ccdFormatter = DateTimeFormatter.ofPattern(fromPattern);
            LocalDate ccdDate = LocalDate.parse(ccdDateString, ccdFormatter);

            DateTimeFormatter letterFormatter = DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT);
            ccdDateString = ccdDate.format(letterFormatter);
        }
        return ccdDateString;
    }
}

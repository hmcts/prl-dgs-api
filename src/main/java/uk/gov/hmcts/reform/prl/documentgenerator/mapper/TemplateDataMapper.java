package uk.gov.hmcts.reform.prl.documentgenerator.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

@Component
public class TemplateDataMapper {

    @Autowired
    private DocmosisBasePdfConfig docmosisBasePdfConfig;

    @SuppressWarnings("unchecked")
    public Map<String, Object> map(Map<String, Object> placeholders) {

        // Get case data
        Map<String, Object> data = (Map<String, Object>) ((Map) placeholders.get(CASE_DETAILS)).get(CASE_DATA);

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

    private String formatDateFromCCD(String ccdDateString) {
        try {
            ccdDateString = formatDateFromPattern(ccdDateString, CCD_DATE_FORMAT);
        } catch (Exception e) {
            throw new PDFGenerationException("Unable to format CCD Date Type field", e);
        }
        return ccdDateString;
    }

    private String formatDateTimeFromCCD(String ccdDateString) {
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

package uk.gov.hmcts.reform.prl.documentgenerator.config;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Set;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.prl.documentgenerator.domain.TemplateConstants.DOCMOSIS_TYPE;
import static uk.gov.hmcts.reform.prl.documentgenerator.domain.TemplateConstants.TEST_TEMPLATE;
import static uk.gov.hmcts.reform.prl.documentgenerator.domain.TemplateConstants.TEST_TEMPLATE_NAME_FOR_PDF;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
class TemplatesConfigurationTest {

    @Autowired
    private TemplatesConfiguration classUnderTest;

    private static Set<Triple<String, String, String>> expectedTemplateConfigs;

    @SuppressWarnings("unchecked")
    @BeforeAll
    static void setUp() {
        expectedTemplateConfigs = Sets.newHashSet(
            new ImmutableTriple<>(TEST_TEMPLATE,
                TEST_TEMPLATE_NAME_FOR_PDF, DOCMOSIS_TYPE)
        );
    }

    @Test
    void shouldRetrieveFileName_AndPdfGeneratorName_ByTemplateName() {
        expectedTemplateConfigs.forEach(
            expectedTemplateConfig -> {
                String templateName = expectedTemplateConfig.getLeft();
                String fileName = expectedTemplateConfig.getMiddle();
                String fileGenerator = expectedTemplateConfig.getRight();

                System.out.printf("Testing template %s has file name %s%n", templateName, fileName);
                assertThat(format("Template %s should have file name \"%s\"", templateName, fileName),
                    classUnderTest.getFileNameByTemplateName(templateName), is(fileName));

                System.out.printf("Testing template %s has file generator %s%n", templateName, fileGenerator);
                assertThat(format("Template %s should have file generator \"%s\"", templateName, fileGenerator),
                    classUnderTest.getGeneratorServiceNameByTemplateName(templateName), is(fileGenerator));
            }
        );
    }

    @Test
    void shouldThrowExceptionWhenUnknownTemplateIsRequested() {
        String templateName = "unknown-template";
        IllegalArgumentException illegalArgumentException = assertThrows(
            IllegalArgumentException.class,
            () -> classUnderTest.getFileNameByTemplateName(templateName)
        );

        assertThat(illegalArgumentException.getMessage(), containsString("Unknown template: " + templateName));
    }

    @Test
    void shouldReturnDefaultGeneratorForNonExistentTemplate() {
        assertThat(classUnderTest.getGeneratorServiceNameByTemplateName("non-existent-template"), is(DOCMOSIS_TYPE));
    }

    @Test
    void shouldThrowAnExceptionWhenTemplateNameIsDuplicated() {
        TemplatesConfiguration templatesConfiguration = new TemplatesConfiguration();
        templatesConfiguration.setConfigurationList(Arrays.asList(
            new TemplateConfiguration("thisName", null, null),
            new TemplateConfiguration("thisName", null, null)
        ));

        assertThrows(Exception.class, templatesConfiguration::init);
    }

}

package uk.gov.hmcts.reform.prl;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = DocumentGeneratorControllerIntegrationTest.class)
@AutoConfigureMockMvc
public class DocumentGeneratorControllerIntegrationTest {

    private static final String VALID_INPUT_JSON = "documentgenerator/documents/jsoninput/DA-granted-letter.json";

    @Test
    public void givenTemplateShouldGenerateRequest_DummyTest() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);

        assertFalse(requestBody.isEmpty());
    }
}

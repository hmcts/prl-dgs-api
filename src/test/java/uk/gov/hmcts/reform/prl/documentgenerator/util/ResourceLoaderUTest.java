package uk.gov.hmcts.reform.prl.documentgenerator.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.documentgenerator.exception.ErrorLoadingTemplateException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class ResourceLoaderUTest {
    private static final String NON_EXISTENT_PATH = "somePath";
    private static final String EXISTING_PATH = "ResourceLoadTest.txt";
    private static final String DATA_IN_FILE = "Resource Load Test";

    MockedStatic<NullOrEmptyValidator> nullOrEmptyValidator;

    @BeforeEach
    void beforeTest() {
        nullOrEmptyValidator = Mockito.mockStatic(NullOrEmptyValidator.class);

    }

    @AfterEach
    void afterTest() {
        nullOrEmptyValidator.close();
    }

    @Test
    void testConstructorPrivate() throws Exception {
        Constructor<ResourceLoader> constructor = ResourceLoader.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    void givenFileDoesNotExistWhenLoadResourceThenThrowsErrorLoadingTemplateException() {
        assertThrows(ErrorLoadingTemplateException.class,
                     () -> ResourceLoader.loadResource(NON_EXISTENT_PATH)
        );
    }

    @Test
    void givenFileExistsWhenLoadResourceThenLoadFile() {
        byte[] data = ResourceLoader.loadResource(EXISTING_PATH);
        nullOrEmptyValidator.verify(
            () -> NullOrEmptyValidator.requireNonBlank(anyString())
        );

        assertEquals(DATA_IN_FILE, new String(data, StandardCharsets.UTF_8));
        NullOrEmptyValidator.requireNonBlank(EXISTING_PATH);
    }
}

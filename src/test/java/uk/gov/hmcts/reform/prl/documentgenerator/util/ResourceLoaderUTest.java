package uk.gov.hmcts.reform.prl.documentgenerator.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.documentgenerator.exception.ErrorLoadingTemplateException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class ResourceLoaderUTest {
    private static final String NON_EXISTENT_PATH = "somePath";
    private static final String EXISTING_PATH = "ResourceLoadTest.txt";
    private static final String DATA_IN_FILE = "Resource Load Test";

    MockedStatic<NullOrEmptyValidator> nullOrEmptyValidator ;

    @Before
    public void beforeTest() {
        nullOrEmptyValidator = Mockito.mockStatic(NullOrEmptyValidator.class);

    }

    @After
    public void afterTest() {
        nullOrEmptyValidator.close();
    }

    @Test
    public void testConstructorPrivate() throws Exception {
        Constructor<ResourceLoader> constructor = ResourceLoader.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test(expected = ErrorLoadingTemplateException.class)
    public void givenFileIsDoNotExists_whenLoadResource_thenThrowsErrorLoadingTemplateException() {

        nullOrEmptyValidator.verifyNoInteractions();
        ResourceLoader.loadResource(NON_EXISTENT_PATH);
        NullOrEmptyValidator.requireNonBlank(NON_EXISTENT_PATH);
    }

    @Test
    public void givenFileExists_whenLoadResource_thenLoadFile() {
        byte[] data = ResourceLoader.loadResource(EXISTING_PATH);
        nullOrEmptyValidator.verify(
            () -> NullOrEmptyValidator.requireNonBlank(anyString())
        );

        assertEquals(DATA_IN_FILE, new String(data, StandardCharsets.UTF_8));
        NullOrEmptyValidator.requireNonBlank(EXISTING_PATH);
    }
}

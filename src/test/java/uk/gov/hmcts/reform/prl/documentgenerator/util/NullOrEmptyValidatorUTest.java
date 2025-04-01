package uk.gov.hmcts.reform.prl.documentgenerator.util;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
class NullOrEmptyValidatorUTest {

    private static final String BLANK_STRING = " ";
    private static final String EMPTY_STRING = "";
    private static final String SOME_STRING = "Some String";

    @Test
    void testConstructorPrivate() throws Exception {
        Constructor<NullOrEmptyValidator> constructor = NullOrEmptyValidator.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    void givenArrayIsNullWhenRequireNonEmptyThenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            NullOrEmptyValidator.requireNonEmpty(null)
        );
    }

    @Test
    void givenArrayIsEmptyWhenRequireNonEmptyThenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            NullOrEmptyValidator.requireNonEmpty(ArrayUtils.EMPTY_BYTE_ARRAY)
        );
    }

    @Test
    void givenArrayIsNotEmptyOrNullWhenRequireNonEmptyThenDoesNotThrowException() {
        try {
            NullOrEmptyValidator.requireNonEmpty(new byte[]{1});
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void givenTextIsNullWhenRequireNonBlankThenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            NullOrEmptyValidator.requireNonBlank(null)
        );
    }

    @Test
    void givenTextIsEmptyWhenRequireNonBlankThenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            NullOrEmptyValidator.requireNonBlank(EMPTY_STRING)
        );
    }

    @Test
    void givenTextIsBlankWhenRequireNonBlankThenThrowsIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class, () ->
                NullOrEmptyValidator.requireNonBlank(BLANK_STRING)
        );
    }

    @Test
    void givenTextIsNotBlankWhenRequireNonBlankThenDoesNotThrowException() {
        try {
            NullOrEmptyValidator.requireNonBlank(SOME_STRING);
        } catch (Exception e) {
            fail();
        }
    }

}

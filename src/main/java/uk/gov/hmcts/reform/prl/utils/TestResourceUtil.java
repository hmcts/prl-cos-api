package uk.gov.hmcts.reform.prl.utils;

import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.prl.exception.InvalidResourceException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class TestResourceUtil {
    public static final String RESOURCE_CHARSET_UTF8 = "utf-8";
    public static final String TEST_RESOURCE_NOT_FOUND = "Could not find resource in path ";

    private TestResourceUtil() {

    }

    public static String readFileFrom(final String resourcePath) throws IOException {
        return resourceAsString(resourcePath);
    }

    public static String resourceAsString(final String resourcePath) throws IOException {
        final File file = ResourceUtils.getFile(resourcePath);
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static String loadJson(final String filePath) throws FileNotFoundException {
        return new String(loadResource(filePath), Charset.forName(RESOURCE_CHARSET_UTF8));
    }

    public static byte[] loadResource(final String filePath) throws FileNotFoundException {
        final File file = ResourceUtils.getFile(filePath);
        try {
            URL url =
                    Thread.currentThread()
                            .getContextClassLoader()
                            .getResource(String.valueOf(Files.readAllBytes(file.toPath())));

            if (url == null) {
                throw new IllegalArgumentException(
                        String.format(TEST_RESOURCE_NOT_FOUND + "%s", filePath));
            }
            return Files.readAllBytes(Paths.get(url.toURI()));
        } catch (IOException | URISyntaxException | IllegalArgumentException ioException) {
            throw new InvalidResourceException(TEST_RESOURCE_NOT_FOUND + filePath, ioException);
        }
    }
}

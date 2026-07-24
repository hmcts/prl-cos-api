package uk.gov.hmcts.reform.prl.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YamlPropertySourceFactoryTest {

    @Mock
    private EncodedResource encodedResource;

    @Mock
    private Resource resource;

    private final YamlPropertySourceFactory factory = new YamlPropertySourceFactory();

    @Test
    void shouldCreatePropertySourceWithProvidedName() throws IOException {
        // Given
        String name = "test-properties";
        String yamlContent = "key: value\nanother: 123";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));

        when(encodedResource.getResource()).thenReturn(resource);
        when(resource.getInputStream()).thenReturn(inputStream);

        // When
        PropertySource<?> propertySource = factory.createPropertySource(name, encodedResource);

        // Then
        assertNotNull(propertySource);
        assertEquals(name, propertySource.getName());
        assertEquals("value", propertySource.getProperty("key"));
        assertEquals(123, propertySource.getProperty("another"));
    }

    @Test
    void shouldCreatePropertySourceWithResourceFilenameWhenNameIsNull() throws IOException {
        // Given
        String filename = "application.yml";
        String yamlContent = "app:\n  name: test-app";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));

        when(encodedResource.getResource()).thenReturn(resource);
        when(resource.getFilename()).thenReturn(filename);
        when(resource.getInputStream()).thenReturn(inputStream);

        // When
        PropertySource<?> propertySource = factory.createPropertySource(null, encodedResource);

        // Then
        assertNotNull(propertySource);
        assertEquals(filename, propertySource.getName());
        assertEquals("test-app", propertySource.getProperty("app.name"));
    }
}

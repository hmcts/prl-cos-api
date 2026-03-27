package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.court.PathFinderMapping;
import uk.gov.hmcts.reform.prl.utils.ResourceReader;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PathFinderLookupServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    private PathFinderLookupService pathFinderLookupService;

    private final String jsonContent = "[{\"courtCode\":\"code1\",\"courtName\":\"name1\",\"courtField\":\"field1\"," +
        "\"dfjArea\":\"area1\",\"pathFinderEnabled\":true}]";

    @Before
    public void setUp() throws JsonProcessingException {
        try (MockedStatic<ResourceReader> mockedStatic = mockStatic(ResourceReader.class)) {
            mockedStatic.when(() -> ResourceReader.readString("pathFinderMapping.json")).thenReturn(jsonContent);
            when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(List.of(
                PathFinderMapping.builder()
                    .courtCode("code1")
                    .courtName("name1")
                    .courtField("field1")
                    .dfjArea("area1")
                    .pathFinderEnabled(true)
                    .build()
            ));
            pathFinderLookupService = new PathFinderLookupService(objectMapper);
        }
    }

    @Test
    public void shouldReturnMappingWhenCourtFieldExists() {
        Optional<PathFinderMapping> result = pathFinderLookupService.getPathFinderMappingByCourtField("field1");

        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getCourtField(), is("field1"));
    }

    @Test
    public void shouldReturnEmptyWhenCourtFieldDoesNotExist() {
        Optional<PathFinderMapping> result = pathFinderLookupService.getPathFinderMappingByCourtField("nonexistent");

        assertThat(result.isPresent(), is(false));
    }

    @Test
    public void shouldReturnEmptyWhenCourtFieldIsNull() {
        Optional<PathFinderMapping> result = pathFinderLookupService.getPathFinderMappingByCourtField(null);

        assertThat(result.isPresent(), is(false));
    }

    @Test
    public void shouldReturnEmptyWhenNoMappingsLoaded() throws JsonProcessingException {
        try (MockedStatic<ResourceReader> mockedStatic = mockStatic(ResourceReader.class)) {
            mockedStatic.when(() -> ResourceReader.readString("pathFinderMapping.json")).thenReturn("[]");
            when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(List.of());
            PathFinderLookupService service = new PathFinderLookupService(objectMapper);

            Optional<PathFinderMapping> result = service.getPathFinderMappingByCourtField("field1");

            assertThat(result.isPresent(), is(false));
        }
    }
}

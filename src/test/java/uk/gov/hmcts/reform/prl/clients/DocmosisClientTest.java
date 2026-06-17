package uk.gov.hmcts.reform.prl.clients;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.prl.framework.exceptions.DocumentGenerationException;
import uk.gov.hmcts.reform.prl.models.dto.docmosis.DocmosisRenderRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocmosisClientTest {

    private RestTemplate restTemplate;
    private DocmosisClient docmosisClient;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        docmosisClient = new DocmosisClient(restTemplate);

        ReflectionTestUtils.setField(docmosisClient, "docmosisUrl", "http://localhost:1234");
        ReflectionTestUtils.setField(docmosisClient, "accessKey", "test-access-key");
    }

    @Test
    void render_shouldSetHeadersAndCallRestTemplate() {
        DocmosisRenderRequest request = new DocmosisRenderRequest();
        byte[] expectedResponse = "result".getBytes();

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(byte[].class)
        )).thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        byte[] result = docmosisClient.render(request);

        assertThat(result).isEqualTo(expectedResponse);

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
            eq("http://localhost:1234/rs/render"),
            eq(HttpMethod.POST),
            entityCaptor.capture(),
            eq(byte[].class)
        );

        HttpEntity<?> entity = entityCaptor.getValue();
        assertThat(entity.getBody()).isEqualTo(request);
        HttpHeaders headers = entity.getHeaders();
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(headers.getFirst("accessKey")).isEqualTo("test-access-key");
    }

    @Test
    void render_shouldThrowDocumentGenerationExceptionWhenRestClientExceptionOccurs() {
        DocmosisRenderRequest request = DocmosisRenderRequest.builder()
            .templateName("test-template")
            .build();
        RestClientException cause = new RestClientException("docmosis unavailable");

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(byte[].class)
        )).thenThrow(cause);

        assertThatThrownBy(() -> docmosisClient.render(request))
            .isInstanceOf(DocumentGenerationException.class)
            .hasMessage("Error while rendering Docmosis template test-template")
            .hasCause(cause);
    }

    @Test
    void convert_shouldSetHeadersAndCallRestTemplate() {
        byte[] expectedResponse = "result".getBytes();

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(byte[].class)
        )).thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        byte[] content = "file content".getBytes();
        byte[] result = docmosisClient.convert(content, "source.docx", "output.pdf");

        assertThat(result).isEqualTo(expectedResponse);

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
            eq("http://localhost:1234/rs/convert"),
            eq(HttpMethod.POST),
            entityCaptor.capture(),
            eq(byte[].class)
        );

        HttpEntity<?> entity = entityCaptor.getValue();
        Map<String, Object> body = (Map<String, Object>) entity.getBody();
        assertThat(body)
            .contains(entry("accessKey", List.of("test-access-key")))
            .contains(entry("outputName", List.of("output.pdf")));
        assertThat(body.get("file"))
            .isInstanceOfSatisfying(ArrayList.class, list ->
                assertThat((List<?>) list)
                    .hasSize(1)
                    .allMatch(ByteArrayResource.class::isInstance)
            );

        HttpHeaders headers = entity.getHeaders();
        assertThat(headers.getContentType()).isEqualTo(MediaType.MULTIPART_FORM_DATA);
    }

    @Test
    void convert_shouldThrowDocumentGenerationExceptionWhenRestClientExceptionOccurs() {
        RestClientException cause = new RestClientException("conversion failed");

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(byte[].class)
        )).thenThrow(cause);
        byte[] content = "file content".getBytes();

        assertThatThrownBy(() -> docmosisClient.convert(content, "source.docx", "output.pdf"))
            .isInstanceOf(DocumentGenerationException.class)
            .hasMessage("Error during Docmosis conversion: conversion failed")
            .hasCause(cause);
    }
}

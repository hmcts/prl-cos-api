package uk.gov.hmcts.reform.prl.clients;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.prl.models.dto.docmosis.DocmosisRenderRequest;

@Component
@RequiredArgsConstructor
public class DocmosisClient {

    @Value("${docmosis.url}")
    private String docmosisUrl;
    @Value("${docmosis.access-key}")
    private String accessKey;

    private final RestTemplate restTemplate;

    public byte[] render(DocmosisRenderRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("accessKey", accessKey);

        HttpEntity<DocmosisRenderRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
            docmosisUrl + "/rs/render",
            HttpMethod.POST,
            entity,
            byte[].class
        );

        return response.getBody();
    }

    public byte[] convert(byte[] fileBytes, String sourceFilename, String outputFilename) {
        ByteArrayResource resource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return sourceFilename;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        body.add("accessKey", accessKey);
        body.add("outputName", outputFilename);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
            docmosisUrl + "/rs/convert",
            HttpMethod.POST,
            requestEntity,
            byte[].class
        );

        return response.getBody();
    }
}

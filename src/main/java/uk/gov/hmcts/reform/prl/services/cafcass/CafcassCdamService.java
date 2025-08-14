package uk.gov.hmcts.reform.prl.services.cafcass;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;

import java.net.URLConnection;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassCdamService {


    private final CaseDocumentClient caseDocumentClient;

    private final AuthTokenGenerator authTokenGenerator;

    public ResponseEntity<Resource> getDocument(String authToken, String s2sToken, UUID documentId) {

        ResponseEntity<Resource> resp = caseDocumentClient.getDocumentBinary(
            authToken,
            authTokenGenerator.generate(),
            documentId
        );

        // phaff with headers
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(resp.getHeaders());

        // 3. Only if downstream didn’t already set a Content-Type…
        Resource body = resp.getBody();
        if (resp.getStatusCode().is2xxSuccessful() && body != null
            && !headers.containsKey(HttpHeaders.CONTENT_TYPE)) {

            // 3a. Pick up the filename (from header or Resource itself)
            String filename = headers.getFirst("originalfilename");
            if (filename == null) {
                filename = body.getFilename();
            }

            // 3b. Guess MIME type, fallback to octet-stream
            String guessed = URLConnection.guessContentTypeFromName(filename);
            String mimeType = (guessed != null
                ? guessed
                : MediaType.APPLICATION_OCTET_STREAM_VALUE);

            headers.set(HttpHeaders.CONTENT_TYPE, mimeType);
        }

        // 4. Return a fresh ResponseEntity with our patched headers
        return new ResponseEntity<>(
            resp.getBody(),
            headers,
            resp.getStatusCode()
        );
    }
}

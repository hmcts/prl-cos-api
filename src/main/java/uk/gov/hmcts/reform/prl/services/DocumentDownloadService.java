package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.exception.EmptyFileException;

import java.net.URI;
import java.util.Optional;

import static java.lang.String.join;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentDownloadService {
    private final AuthTokenGenerator authTokenGenerator;
    private final DocumentDownloadClientApi documentDownloadClient;
    private final IdamClient idamClient;

    public byte[] downloadDocument(final String documentUrlString, String authorisation) {

        UserInfo userInfo = idamClient.getUserInfo(authorisation);

        final String userRoles = join(",", userInfo.getRoles());

        log.info("Download document {} by user {} with roles {}", documentUrlString, userInfo.getUid(), userRoles);

        ResponseEntity<Resource> documentDownloadResponse =
            documentDownloadClient.downloadBinary(authorisation,
                                                  authTokenGenerator.generate(),
                                                  userRoles,
                                                  userInfo.getUid(),
                                                  URI.create(documentUrlString).getPath());

        if (isNotEmpty(documentDownloadResponse) && HttpStatus.OK == documentDownloadResponse.getStatusCode()) {
            return Optional.of(documentDownloadResponse)
                .map(HttpEntity::getBody)
                .map(ByteArrayResource.class::cast)
                .map(ByteArrayResource::getByteArray)
                .orElseThrow(EmptyFileException::new);
        }
        throw new IllegalArgumentException(String.format("Download of document from %s unsuccessful.",
                                                         documentUrlString));
    }
}

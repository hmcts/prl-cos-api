package uk.gov.hmcts.reform.prl.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static com.google.common.collect.Lists.newArrayList;

@Service
@Slf4j
public class UploadDocumentService {
    private final AuthTokenGenerator authTokenGenerator;
    private final DocumentUploadClientApi documentUploadClient;
    private final IdamClient idamClient;

    @Autowired
    public UploadDocumentService(AuthTokenGenerator authTokenGenerator, DocumentUploadClientApi documentUploadClient,
                                 IdamClient idamClient) {
        this.authTokenGenerator = authTokenGenerator;
        this.documentUploadClient = documentUploadClient;
        this.idamClient = idamClient;
    }

    public Document uploadPdf(byte[] pdf, String fileName, String authorisation) {
        return uploadDocument(pdf, fileName, MediaType.APPLICATION_PDF_VALUE, authorisation);
    }

    public Document uploadDocument(byte[] pdf, String fileName, String contentType, String authorisation) {
        MultipartFile file = new InMemoryMultipartFile("files", fileName, contentType, pdf);

        UserInfo userInfo = idamClient.getUserInfo(authorisation);

        UploadResponse response = documentUploadClient.upload(authorisation,
                                                              authTokenGenerator.generate(), userInfo.getUid(), newArrayList(file));

        Document document = response.getEmbedded().getDocuments().stream()
            .findFirst()
            .orElseThrow(() ->
                             new RuntimeException("Document upload failed due to empty result"));

        log.debug("Document upload resulted with links: {}, {}", document.links.self.href, document.links.binary.href);

        return document;
    }
}

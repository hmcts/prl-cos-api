package uk.gov.hmcts.reform.prl.services.cafcass;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.exception.cafcass.CafcassDocumentDownloadException;
import uk.gov.hmcts.reform.prl.models.cafcass.CafcassDocumentInfo;
import uk.gov.hmcts.reform.prl.models.cafcass.CafcassDocumentResponse;
import org.springframework.core.io.Resource;

import java.util.UUID;

@Service
@Slf4j
public class CafcassDocumentManagementService {

    //@Autowired
    //CafcassCdamService cafcassCdamService;

    public CafcassDocumentResponse downloadDocument(String authorisation, String serviceAuthorisation, UUID documentId) {
        try {
//            ResponseEntity<Resource> cafcassResponse = Res;
            //ResponseEntity<Resource>  = cafcassCdamService.getDocument(authorisation, serviceAuthorisation, documentId);
            return CafcassDocumentResponse.builder().status("Success").build();

        } catch (Exception e) {
            throw new CafcassDocumentDownloadException("Failed while downloading the document. The error message is "
                    + e.getMessage(), e);
        }
    }
}

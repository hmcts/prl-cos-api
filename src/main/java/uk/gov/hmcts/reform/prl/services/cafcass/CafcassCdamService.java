package uk.gov.hmcts.reform.prl.services.cafcass;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;

import java.util.UUID;

@Service
public class CafcassCdamService {

    @Autowired
    CaseDocumentClient caseDocumentClient;


    /**
     * This method will call CDAM getDocument API
     * and return the result.
     * @param authToken Authorisation token
     * @param s2sToken s2s token
     * @param documentId document id
     * @return
     */
    public ResponseEntity<Resource> getDocument(String authToken, String s2sToken, UUID documentId) {

        return caseDocumentClient.getDocumentBinary(authToken,s2sToken, documentId);
    }

}

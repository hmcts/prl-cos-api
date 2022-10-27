package uk.gov.hmcts.reform.prl.services.noc;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IdentityService {

    public UUID generateId() {
        return UUID.randomUUID();
    }
}

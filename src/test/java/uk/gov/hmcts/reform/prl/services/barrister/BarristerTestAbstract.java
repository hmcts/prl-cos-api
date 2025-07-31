package uk.gov.hmcts.reform.prl.services.barrister;

import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Barrister;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BarristerTestAbstract {
    protected List<Element<PartyDetails>> allApplicants = new ArrayList<>();
    protected List<Element<PartyDetails>> allRespondents = new ArrayList<>();
    protected PartyDetails applicantFL401;
    protected PartyDetails respondentFL401;

    protected static final String PARTY_ID_PREFIX = "c0651c7d-0db9-47aa-9baa-933013f482f";
    protected static final String SOL_PARTY_ID_PREFIX = "c0651c7d-0db9-47aa-9baa-933013f482e";
    protected static final String BARRISTER_PARTY_ID_PREFIX = "c0651c7d-0db9-47aa-9bbb-933013f482e";
    protected static final String BARRISTER_ORG_ID_PREFIX = "d0651c7d-0db9-47aa-9bbb-933013f482e";

    protected void setupApplicantsC100() {
        allApplicants.add(buildPartyDetailsElement(1, true, true, false));
        allApplicants.add(buildPartyDetailsElement(2, true, true, false));
        allApplicants.add(buildPartyDetailsElement(3, true, true, true));
        allApplicants.add(buildPartyDetailsElement(4, true, false, false));
    }

    protected void setupRespondentsC100() {
        allRespondents.add(buildPartyDetailsElement(5, false, true, false));
        allRespondents.add(buildPartyDetailsElement(6, false, true, false));
        allRespondents.add(buildPartyDetailsElement(7, false, true, true));
        allRespondents.add(buildPartyDetailsElement(8, false, false, false));
    }

    void setupApplicantFL401() {
        applicantFL401 = buildPartyDetails(1, "App", "App", "Bar", "Org", true, false);
    }

    void setupRespondentFl401() {
        respondentFL401 = buildPartyDetails(1, "Resp", "Resp", "Bar", "Org", true, false);
    }

    protected Element<PartyDetails> buildPartyDetailsElement(int ind, boolean appRep, boolean hasRep, boolean hasBar) {
        return buildPartyDetailsElement(
            ind,
            (appRep ? "App" : "Resp"),
            (appRep ? "App" : "Resp"),
            (hasBar ? "Bar" : null),
            "Org",
            hasRep,
            hasBar
        );
    }

    protected Element<PartyDetails> buildPartyDetailsElement(int id, String appName, String repName, String barName,
                                                             String orgName, boolean hasRep, boolean hasBar) {
        return Element.<PartyDetails>builder().id(UUID.fromString(PARTY_ID_PREFIX + id))
            .value(buildPartyDetails(id, appName, repName, barName, orgName, hasRep, hasBar))
            .build();
    }

    protected PartyDetails buildPartyDetails(int id, String appName, String repName, String barName, String orgName,
                                             boolean hasRep, boolean hasBar) {
        return getPartyDetails(
            id,
            appName + "FN",
            appName + "LN",
            repName + "FN",
            repName + "LN",
            barName + "FN",
            barName + "LN",
            orgName,
            hasRep,
            hasBar
        );
    }

    protected PartyDetails getPartyDetails(int id, String appFirstName, String appLastName, String repFirstName,
                                           String repLastName, String barFirstName, String barLastName, String orgName,
                                           boolean hasRep, boolean hasBar) {
        Barrister barrister = Barrister.builder()
            .barristerPartyId(UUID.fromString(BARRISTER_PARTY_ID_PREFIX + id))
            .barristerFirstName(barFirstName + id)
            .barristerLastName(barLastName + id)
            .barristerOrgUuid(UUID.fromString(BARRISTER_ORG_ID_PREFIX + id))
            .build();
        return PartyDetails.builder()
            .partyId(UUID.fromString(PARTY_ID_PREFIX + id))
            .firstName(appFirstName + id)
            .lastName(appLastName + id)
            .doTheyHaveLegalRepresentation(hasRep ? YesNoDontKnow.yes : null)
            .solicitorPartyId(hasRep ? UUID.fromString(SOL_PARTY_ID_PREFIX + id) : null)
            .representativeFirstName(hasRep ? repFirstName + id : null)
            .representativeLastName(hasRep ? repLastName + id : null)
            .solicitorOrg(Organisation.builder().organisationName(orgName + id).organisationID(orgName + id).build())
            .barrister(hasBar ? barrister : null)
            .build();
    }

}

package uk.gov.hmcts.reform.prl.services.tab.summary.generators.refuge;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.refuge.RefugeCase;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.refuge.RefugeCaseGenerator;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;

public class RefugeCaseGeneratorTest {

    private final RefugeCaseGenerator generator = new RefugeCaseGenerator();


    PartyDetails refugePartDetails;


    @Test
    public void testGenerateForC100() {

        Address address = Address.builder()
            .addressLine1("test")
            .postCode("test")
            .build();


        refugePartDetails = PartyDetails.builder()
            .firstName("Test")
            .lastName("Test")
            .liveInRefuge(No)
            .dateOfBirth(LocalDate.of(2000, 8, 20))
            .address(address)
            .build();

        Element<PartyDetails> wrappedPartyDetails = Element.<PartyDetails>builder().value(refugePartDetails).build();
        List<Element<PartyDetails>> refugePartDetailsList = Collections.singletonList(wrappedPartyDetails);


        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .caseTypeOfApplication("C100")
                                                         .applicants(refugePartDetailsList)
                                                         .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder().refugeCase(RefugeCase
                                                                               .builder()
                                                                               .isRefugeCase(YesOrNo.No)
                                                                               .build())
                                              .build());
    }

    @Test
    public void testGenerateForFL401() {

        CaseSummary caseSummary = generator.generate(CaseData.builder()
                                                         .caseTypeOfApplication("FL401")
                                                         .applicantsFL401(refugePartDetails)
                                                         .build());

        assertThat(caseSummary).isEqualTo(CaseSummary.builder().refugeCase(RefugeCase
                                                                               .builder()
                                                                               .isRefugeCase(YesOrNo.No)
                                                                               .build())
                                              .build());
    }

}

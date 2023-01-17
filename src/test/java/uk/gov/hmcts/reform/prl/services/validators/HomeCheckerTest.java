package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.FamilyHomeEnum;
import uk.gov.hmcts.reform.prl.enums.LivingSituationEnum;
import uk.gov.hmcts.reform.prl.enums.MortgageNamedAfterEnum;
import uk.gov.hmcts.reform.prl.enums.PeopleLivingAtThisAddressEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoBothEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.Landlord;
import uk.gov.hmcts.reform.prl.models.complextypes.Mortgage;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class HomeCheckerTest {

    @Mock
    private TaskErrorService taskErrorService;

    @InjectMocks
    private HomeChecker homeChecker;

    @Mock
    private Home home;

    @Before
    public void setup() {
        home = Home.builder()
            .doesApplicantHaveHomeRights(YesOrNo.Yes)
            .everLivedAtTheAddress(YesNoBothEnum.yesApplicant)
            .build();
    }

    @Test
    public void whenHomePresentButNotCompleteThenIsFinishedReturnsFalse() {


        CaseData caseData = CaseData.builder().home(home).build();

        assertFalse(homeChecker.isFinished(caseData));
    }

    @Test
    public void whenHomeIsNotPresentThenIsFinishedReturnsFalse() {

        CaseData caseData = CaseData.builder().home(null).build();

        assertFalse(homeChecker.isFinished(caseData));
    }

    @Test
    public void whenHomeIsFilledThenIsFinishedReturnsFalse() {
        Home homefull = Home.builder()
            .everLivedAtTheAddress(YesNoBothEnum.yesApplicant)
            .doesApplicantHaveHomeRights(YesOrNo.No)
            .doAnyChildrenLiveAtAddress(YesOrNo.No)
            .isPropertyRented(YesOrNo.No)
            .isThereMortgageOnProperty(YesOrNo.No)
            .isPropertyAdapted(YesOrNo.No)
            .peopleLivingAtThisAddress(List.of(PeopleLivingAtThisAddressEnum.applicant))
            .familyHome(List.of(FamilyHomeEnum.payForRepairs))
            .livingSituation(List.of(LivingSituationEnum.awayFromHome))
            .build();
        CaseData caseData = CaseData.builder().home(homefull).build();

        assertFalse(homeChecker.isFinished(caseData));
    }

    @Test
    public void whenHomePresentThenIsStartedReturnsTrue() {

        CaseData caseData = CaseData.builder().home(home).build();

        assertTrue(homeChecker.isStarted(caseData));
    }

    @Test
    public void whenHomeIsNotPresentThenIsStartedReturnsFalse() {

        CaseData caseData = CaseData.builder().applicants(null).build();

        assertFalse(homeChecker.isStarted(caseData));
    }

    @Test
    public void whenHomePresentButNotCompletedThenHasMandatoryReturnsFalse() {

        CaseData caseData = CaseData.builder().home(home).build();

        assertFalse(homeChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenHomeIsNotPresentThenHasMandatoryReturnsFalse() {

        CaseData caseData = CaseData.builder().home(null).build();

        assertFalse(homeChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenIncompleteAddressDataThenVerificationReturnsFalse() {
        Address address = Address.builder()
            .addressLine2("Test")
            .country("UK")
            .build();

        assertFalse(homeChecker.verifyAddressCompleted(address));
    }

    @Test
    public void whenCompleteAddressDataThenVerificationReturnsTrue() {
        Address address = Address.builder()
            .addressLine1("Test")
            .addressLine2("Test")
            .addressLine3("Test")
            .county("London")
            .country("UK")
            .postTown("Southgate")
            .postCode("N14 5EF")
            .build();

        assertTrue(homeChecker.verifyAddressCompleted(address));
    }

    @Test
    public void whenChildDetailsFilledThenReturnsTrue() {
        Address address = Address.builder()
            .addressLine1("Test")
            .addressLine2("Test")
            .addressLine3("Test")
            .county("London")
            .country("UK")
            .postTown("Southgate")
            .postCode("N14 5EF")
            .build();

        assertTrue(homeChecker.verifyAddressCompleted(address));
    }

    @Test
    public void whenChildDetailsFilledAlongWithOtherThenIsFinishedReturnsTrue() {
        ChildrenLiveAtAddress childrenLiveAtAddress = ChildrenLiveAtAddress.builder()
            .keepChildrenInfoConfidential(YesOrNo.Yes)
            .childFullName("child")
            .childsAge("12")
            .isRespondentResponsibleForChild(YesOrNo.Yes)
            .build();

        Home homefull = Home.builder()
            .address(Address.builder().addressLine1("123").build())
            .everLivedAtTheAddress(YesNoBothEnum.yesApplicant)
            .doesApplicantHaveHomeRights(YesOrNo.No)
            .doAnyChildrenLiveAtAddress(YesOrNo.Yes)
            .children(List.of(Element.<ChildrenLiveAtAddress>builder().value(childrenLiveAtAddress).build()))
            .isPropertyRented(YesOrNo.No)
            .isThereMortgageOnProperty(YesOrNo.No)
            .isPropertyAdapted(YesOrNo.No)
            .peopleLivingAtThisAddress(List.of(PeopleLivingAtThisAddressEnum.applicant))
            .familyHome(List.of(FamilyHomeEnum.payForRepairs))
            .livingSituation(List.of(LivingSituationEnum.awayFromHome))
            .build();
        CaseData caseData = CaseData.builder()
                            .home(homefull)
                            .build();
        assertTrue(homeChecker.isFinished(caseData));
    }

    @Test
    public void whenMortgageDetailsFilledAlongWithOtherThenIsFinishedReturnsTrue() {
        Address address = Address.builder().addressLine1("123").build();
        Mortgage mortgage = Mortgage.builder()
            .mortgageLenderName("Lender")
            .address(address)
            .mortgageNamedAfter(List.of(MortgageNamedAfterEnum.applicant))
            .build();

        Home homefull = Home.builder()
            .address(address)
            .everLivedAtTheAddress(YesNoBothEnum.yesApplicant)
            .doesApplicantHaveHomeRights(YesOrNo.No)
            .doAnyChildrenLiveAtAddress(YesOrNo.No)
            .isPropertyRented(YesOrNo.No)
            .isThereMortgageOnProperty(YesOrNo.Yes)
            .mortgages(mortgage)
            .isPropertyAdapted(YesOrNo.No)
            .peopleLivingAtThisAddress(List.of(PeopleLivingAtThisAddressEnum.applicant))
            .familyHome(List.of(FamilyHomeEnum.payForRepairs))
            .livingSituation(List.of(LivingSituationEnum.awayFromHome))
            .build();
        CaseData caseData = CaseData.builder()
            .home(homefull)
            .build();
        assertTrue(homeChecker.isFinished(caseData));
    }

    @Test
    public void whenLandlordDetailsFilledAlongWithOtherThenIsFinishedReturnsTrue() {
        Address address = Address.builder().addressLine1("123").build();
        Landlord rentedProperty = Landlord.builder()
            .landlordName("Landlord")
            .address(address)
            .mortgageNamedAfterList(List.of(MortgageNamedAfterEnum.applicant))
            .build();

        Home homefull = Home.builder()
            .address(address)
            .everLivedAtTheAddress(YesNoBothEnum.yesApplicant)
            .doesApplicantHaveHomeRights(YesOrNo.No)
            .doAnyChildrenLiveAtAddress(YesOrNo.No)
            .isPropertyRented(YesOrNo.Yes)
            .landlords(rentedProperty)
            .isThereMortgageOnProperty(YesOrNo.No)
            .isPropertyAdapted(YesOrNo.No)
            .peopleLivingAtThisAddress(List.of(PeopleLivingAtThisAddressEnum.applicant))
            .familyHome(List.of(FamilyHomeEnum.payForRepairs))
            .livingSituation(List.of(LivingSituationEnum.awayFromHome))
            .build();
        CaseData caseData = CaseData.builder()
            .home(homefull)
            .build();
        assertTrue(homeChecker.isFinished(caseData));
    }

    @Test
    public void whenDataPresentGetIntendToLiveAtTheAddressFieldsNotEmpty() {
        Home homefull = Home.builder()
            .everLivedAtTheAddress(YesNoBothEnum.No)
            .intendToLiveAtTheAddress(YesNoBothEnum.yesBothOfThem)
            .build();
        CaseData caseData = CaseData.builder()
            .home(homefull)
            .build();

        Optional<Home> home = ofNullable(caseData.getHome());

        List<Optional<?>> fields = new ArrayList<>();
        homeChecker.getIntendToLiveAtTheAddress(home, fields);
        Assert.assertTrue(fields.size() >= 1 && !fields.get(0).isEmpty());
    }

    @Test
    public void whenNoDataGetIntendToLiveAtTheAddressFieldsIsEmpty() {
        Home homefull = Home.builder()
            .build();
        CaseData caseData = CaseData.builder()
            .home(homefull)
            .build();

        Optional<Home> home = ofNullable(caseData.getHome());

        List<Optional<?>> fields = new ArrayList<>();
        homeChecker.getIntendToLiveAtTheAddress(home, fields);
        Assert.assertFalse(fields.size() > 1 && !fields.get(0).isEmpty());
    }

    @Test
    public void whenNoDataGetDetailPeopleLivingAtThisAddressFieldsIsEmpty() {
        Home homefull = Home.builder()
            .build();
        CaseData caseData = CaseData.builder()
            .home(homefull)
            .build();
        Optional<Home> home = ofNullable(caseData.getHome());
        List<Optional<?>> fields = new ArrayList<>();
        homeChecker.getDetailPeopleLivingAtThisAddress(home,fields);
        Assert.assertFalse(fields.size() > 1 && !fields.get(0).isEmpty());
    }

    @Test
    public void whenDataPresentGetDetailPeopleLivingAtThisAddressFieldsIsNotEmpty() {
        Home homefull = Home.builder()
            .peopleLivingAtThisAddress(List.of(PeopleLivingAtThisAddressEnum.someoneElse))
            .textAreaSomethingElse("Testing")
            .build();
        CaseData caseData = CaseData.builder()
            .home(homefull)
            .build();

        Optional<Home> home = ofNullable(caseData.getHome());

        List<Optional<?>> fields = new ArrayList<>();
        homeChecker.getDetailPeopleLivingAtThisAddress(home, fields);
        Assert.assertTrue(fields.size() >= 1 && !fields.get(0).isEmpty());
    }

    @Test
    public void whenNoDataIsChildDetailsAreCompletedIsEmpty() {
        Home homefull = Home.builder()
            .build();
        CaseData caseData = CaseData.builder()
            .home(homefull)
            .build();
        Optional<Home> home = ofNullable(caseData.getHome());
        List<Optional<?>> fields = new ArrayList<>();
        homeChecker.isChildDetailsAreCompleted(home,fields);
        Assert.assertFalse(fields.size() > 1 && !fields.get(0).isEmpty());
    }

    @Test
    public void whenNoDataPresentIsHowIsThePropertyAdaptedCompleteIsEmpty() {
        Home homefull = Home.builder()
            .build();
        CaseData caseData = CaseData.builder()
            .home(homefull)
            .build();
        Optional<Home> home = ofNullable(caseData.getHome());
        List<Optional<?>> fields = new ArrayList<>();
        homeChecker.isHowIsThePropertyAdaptedComplete(home,fields);
        Assert.assertFalse(fields.size() > 1 && !fields.get(0).isEmpty());
    }

    @Test
    public void whenDataPresentIsHowIsThePropertyAdaptedCompleteIsNotEmpty() {
        Home homefull = Home.builder()
            .isPropertyAdapted(YesOrNo.Yes)
            .howIsThePropertyAdapted("testing")
            .build();
        CaseData caseData = CaseData.builder()
            .home(homefull)
            .build();

        Optional<Home> home = ofNullable(caseData.getHome());

        List<Optional<?>> fields = new ArrayList<>();
        homeChecker.isHowIsThePropertyAdaptedComplete(home, fields);
        Assert.assertTrue(fields.size() >= 1 && !fields.get(0).isEmpty());
    }

    @Test
    public void whenNoDataIsAddressPresentReturnTrue() {
        Address address = Address.builder()
            .addressLine1("Test")
            .addressLine2("Test")
            .addressLine3("Test")
            .county("London")
            .country("UK")
            .postTown("Southgate")
            .postCode("N14 5EF")
            .build();

        Home homefull = Home.builder()
            .address(address)
            .build();

        CaseData caseData = CaseData.builder()
            .home(homefull)
            .build();

        Optional<Home> home = ofNullable(caseData.getHome());
        assertTrue(homeChecker.isAddressPresent(home));
    }

    @Test
    public void whenNoDataIsAddressPresentReturnFalse() {
        Home homefull = Home.builder()
            .build();

        CaseData caseData = CaseData.builder()
            .home(homefull)
            .build();

        Optional<Home> home = ofNullable(caseData.getHome());

        assertFalse(homeChecker.isAddressPresent(home));
    }

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(homeChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}

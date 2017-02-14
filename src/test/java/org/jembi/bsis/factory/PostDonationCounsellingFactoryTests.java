package org.jembi.bsis.factory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jembi.bsis.helpers.builders.DonationBuilder.aDonation;
import static org.jembi.bsis.helpers.builders.DonationViewModelBuilder.aDonationViewModel;
import static org.jembi.bsis.helpers.builders.DonorBuilder.aDonor;
import static org.jembi.bsis.helpers.builders.DonorViewModelBuilder.aDonorViewModel;
import static org.jembi.bsis.helpers.builders.PostDonationCounsellingBackingFormBuilder.aPostDonationCounsellingBackingForm;
import static org.jembi.bsis.helpers.builders.PostDonationCounsellingBuilder.aPostDonationCounselling;
import static org.jembi.bsis.helpers.builders.PostDonationCounsellingViewModelBuilder.aPostDonationCounsellingViewModel;
import static org.jembi.bsis.helpers.matchers.PostDonationCounsellingMatcher.hasSameStateAsPostDonationCounselling;
import static org.jembi.bsis.helpers.matchers.PostDonationCounsellingViewModelMatcher.hasSameStateAsPostDonationCounsellingViewModel;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.jembi.bsis.backingform.PostDonationCounsellingBackingForm;
import org.jembi.bsis.model.counselling.CounsellingStatus;
import org.jembi.bsis.model.counselling.PostDonationCounselling;
import org.jembi.bsis.model.donation.Donation;
import org.jembi.bsis.model.donor.Donor;
import org.jembi.bsis.repository.PostDonationCounsellingRepository;
import org.jembi.bsis.suites.UnitTestSuite;
import org.jembi.bsis.viewmodel.CounsellingStatusViewModel;
import org.jembi.bsis.viewmodel.DonationViewModel;
import org.jembi.bsis.viewmodel.DonorViewModel;
import org.jembi.bsis.viewmodel.PostDonationCounsellingViewModel;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class PostDonationCounsellingFactoryTests extends UnitTestSuite {

  @InjectMocks
  private PostDonationCounsellingFactory postDonationCounsellingFactory;
  @Mock
  private PostDonationCounsellingRepository postDonationCounsellingRepository;
  @Mock
  private DonationFactory donationFactory;
  @Mock
  private DonorViewModelFactory donorFactory;

  @Test
  public void testCreateViewModel_shouldReturnViewModelWithCorrectDonorAndPermissionsTrue() {

    boolean canRemoveStatus = true;
    long donorId = 21L;
    long donationId = 87L;
    long postDonationCounsellingId = 32L;
    
    Donor donor = aDonor().withId(donorId).build();
    Donation donation = aDonation().withId(donationId).withDonor(donor).build();

    PostDonationCounselling postDonationCounselling = aPostDonationCounselling()
        .withId(postDonationCounsellingId)
        .withDonation(donation)
        .thatIsFlaggedForCounselling()
        .thatIsReferred()
        .withNotes("notes")
        .build();
    
    DonorViewModel expectedDonorViewModel = aDonorViewModel().withDonor(donor).build();
    DonationViewModel expectedDonationViewModel = aDonationViewModel().withId(donationId).build();

    PostDonationCounsellingViewModel expectedPostDonationCounsellingViewModel = aPostDonationCounsellingViewModel()
        .withId(postDonationCounsellingId)
        .withDonation(expectedDonationViewModel)
        .withDonor(expectedDonorViewModel)
        .withPermission("canRemoveStatus", canRemoveStatus)
        .thatIsFlaggedForCounselling()
        .thatIsReferred()
        .withNotes("notes")
        .build();

    when(postDonationCounsellingRepository.countNotFlaggedPostDonationCounsellingsForDonor(donorId)).thenReturn(1);
    when(donationFactory.createDonationViewModelWithoutPermissions(donation)).thenReturn(expectedDonationViewModel);
    when(donorFactory.createDonorViewModel(donor)).thenReturn(expectedDonorViewModel);

    PostDonationCounsellingViewModel returnedPostDonationCounsellingViewModel = postDonationCounsellingFactory
        .createViewModel(postDonationCounselling);

    assertThat(returnedPostDonationCounsellingViewModel, hasSameStateAsPostDonationCounsellingViewModel(expectedPostDonationCounsellingViewModel));

  }

  @Test
  public void testCreateViewModel_shouldReturnViewModelWithCorrectDonorAndPermissionsFalse() {

    boolean canRemoveStatus = false;
    long donorId = 21L;
    long donationId = 87L;
    long postDonationCounsellingId = 32L;
    
    Donor donor = aDonor().withId(donorId).build();
    Donation donation = aDonation().withId(donationId).withDonor(donor).build();

    PostDonationCounselling postDonationCounselling = aPostDonationCounselling()
        .withId(postDonationCounsellingId)
        .withDonation(donation)
        .thatIsFlaggedForCounselling()
        .thatIsNotReferred()
        .build();
    
    DonorViewModel expectedDonorViewModel = aDonorViewModel().withDonor(donor).build();
    DonationViewModel expectedDonationViewModel = aDonationViewModel().withId(donationId).build();

    PostDonationCounsellingViewModel expectedPostDonationCounsellingViewModel = aPostDonationCounsellingViewModel()
        .withId(postDonationCounsellingId)
        .withDonation(expectedDonationViewModel)
        .withDonor(expectedDonorViewModel)
        .withPermission("canRemoveStatus", canRemoveStatus)
        .thatIsFlaggedForCounselling()
        .thatIsNotReferred()
        .build();

    when(postDonationCounsellingRepository.countNotFlaggedPostDonationCounsellingsForDonor(donorId)).thenReturn(0);
    when(donationFactory.createDonationViewModelWithoutPermissions(donation)).thenReturn(expectedDonationViewModel);
    when(donorFactory.createDonorViewModel(donor)).thenReturn(expectedDonorViewModel);

    PostDonationCounsellingViewModel returnedPostDonationCounsellingViewModel = postDonationCounsellingFactory
        .createViewModel(postDonationCounselling);

    assertThat(returnedPostDonationCounsellingViewModel, hasSameStateAsPostDonationCounsellingViewModel(expectedPostDonationCounsellingViewModel));

  }

  @Test
  public void testCreateViewModelWithReviecedCounsellingStatus_shouldReturnCorrectViewModel() {

    long donorId = 21L;
    long donationId = 87L;
    long postDonationCounsellingId = 32L;
    Date counsellingDate = new Date();
    CounsellingStatus counsellingStatus = CounsellingStatus.RECEIVED_COUNSELLING;
    String notes = "Given counselling";

    Donor donor = aDonor().withId(donorId).build();
    Donation donation = aDonation().withId(donationId).withDonor(donor).build();

    PostDonationCounselling postDonationCounselling = aPostDonationCounselling()
        .withId(postDonationCounsellingId)
        .withDonation(donation)
        .thatIsNotFlaggedForCounselling()
        .withCounsellingStatus(counsellingStatus)
        .withCounsellingDate(counsellingDate)
        .thatIsNotReferred()
        .withNotes(notes)
        .build();

    DonorViewModel expectedDonorViewModel = aDonorViewModel().withDonor(donor).build();
    DonationViewModel expectedDonationViewModel = aDonationViewModel().withId(donationId).build();
    CounsellingStatusViewModel expectedCounsellingStatus = new CounsellingStatusViewModel(counsellingStatus);

    PostDonationCounsellingViewModel expectedPostDonationCounsellingViewModel = aPostDonationCounsellingViewModel()
        .withId(postDonationCounsellingId)
        .withDonation(expectedDonationViewModel)
        .withDonor(expectedDonorViewModel)
        .withPermission("canRemoveStatus", false)
        .thatIsNotFlaggedForCounselling()
        .thatIsNotReferred()
        .withCounsellingStatusViewModel(expectedCounsellingStatus)
        .withCounsellingDate(counsellingDate)
        .withNotes(notes)
        .build();

    when(postDonationCounsellingRepository.countNotFlaggedPostDonationCounsellingsForDonor(donorId)).thenReturn(0);
    when(donationFactory.createDonationViewModelWithoutPermissions(donation)).thenReturn(expectedDonationViewModel);
    when(donorFactory.createDonorViewModel(donor)).thenReturn(expectedDonorViewModel);

    PostDonationCounsellingViewModel returnedPostDonationCounsellingViewModel = postDonationCounsellingFactory
        .createViewModel(postDonationCounselling);

    assertThat(returnedPostDonationCounsellingViewModel, hasSameStateAsPostDonationCounsellingViewModel(expectedPostDonationCounsellingViewModel));
  }

  @Test
  public void testCreateViewModelWithRefusedCounsellingStatus_shouldReturnCorrectViewModel() {

    long donorId = 21L;
    long donationId = 87L;
    long postDonationCounsellingId = 32L;
    CounsellingStatus counsellingStatus = CounsellingStatus.REFUSED_COUNSELLING;
    String notes = "Did not have time to talk to us";

    Donor donor = aDonor().withId(donorId).build();
    Donation donation = aDonation().withId(donationId).withDonor(donor).build();

    PostDonationCounselling postDonationCounselling = aPostDonationCounselling()
        .withId(postDonationCounsellingId)
        .withDonation(donation)
        .thatIsNotFlaggedForCounselling()
        .withCounsellingStatus(counsellingStatus)
        .withCounsellingDate(null)
        .withNotes(notes)
        .withReferred(null)
        .build();

    DonorViewModel expectedDonorViewModel = aDonorViewModel().withDonor(donor).build();
    DonationViewModel expectedDonationViewModel = aDonationViewModel().withId(donationId).build();
    CounsellingStatusViewModel expectedCounsellingStatus = new CounsellingStatusViewModel(counsellingStatus);

    PostDonationCounsellingViewModel expectedPostDonationCounsellingViewModel = aPostDonationCounsellingViewModel()
        .withId(postDonationCounsellingId)
        .withDonation(expectedDonationViewModel)
        .withDonor(expectedDonorViewModel)
        .withPermission("canRemoveStatus", false)
        .thatIsNotFlaggedForCounselling()
        .withCounsellingStatusViewModel(expectedCounsellingStatus)
        .withCounsellingDate(null)
        .withNotes(notes)
        .withReferred(null)
        .build();

    when(postDonationCounsellingRepository.countNotFlaggedPostDonationCounsellingsForDonor(donorId)).thenReturn(0);
    when(donationFactory.createDonationViewModelWithoutPermissions(donation)).thenReturn(expectedDonationViewModel);
    when(donorFactory.createDonorViewModel(donor)).thenReturn(expectedDonorViewModel);

    PostDonationCounsellingViewModel returnedPostDonationCounsellingViewModel = postDonationCounsellingFactory
        .createViewModel(postDonationCounselling);

    assertThat(returnedPostDonationCounsellingViewModel, hasSameStateAsPostDonationCounsellingViewModel(expectedPostDonationCounsellingViewModel));
  }

  @Test
  public void testCreateEntity_shouldReturnEntityInCorrectState() {
    Date counsellingDate = new Date();

    PostDonationCounsellingBackingForm form = aPostDonationCounsellingBackingForm()
        .withId(1L)
        .withCounsellingDate(counsellingDate)
        .withCounsellingStatus(CounsellingStatus.RECEIVED_COUNSELLING)
        .thatIsNotFlaggedForCounselling()
        .thatIsNotReferred()
        .withNotes("notes")
        .build();

    PostDonationCounselling expectedEntity = aPostDonationCounselling()
        .withId(1L)
        .withCounsellingDate(counsellingDate)
        .withCounsellingStatus(CounsellingStatus.RECEIVED_COUNSELLING)
        .thatIsNotFlaggedForCounselling()
        .withDonation(null) // donation is not mapped in the form, so must be null
        .withNotes("notes")
        .thatIsNotReferred()
        .build();

    PostDonationCounselling returnedEntity = postDonationCounsellingFactory.createEntity(form);

    assertThat(returnedEntity, hasSameStateAsPostDonationCounselling(expectedEntity));
  }
}

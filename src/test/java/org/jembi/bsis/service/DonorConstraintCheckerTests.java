package org.jembi.bsis.service;

import org.jembi.bsis.model.donor.Donor;
import org.jembi.bsis.repository.DonationRepository;
import org.jembi.bsis.repository.DonorDeferralRepository;
import org.jembi.bsis.repository.DonorRepository;
import org.jembi.bsis.service.DonorConstraintChecker;
import org.jembi.bsis.service.DonorDeferralStatusCalculator;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jembi.bsis.helpers.builders.DonationBuilder.aDonation;
import static org.jembi.bsis.helpers.builders.DonorBuilder.aDonor;
import static org.jembi.bsis.helpers.builders.PackTypeBuilder.aPackType;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DonorConstraintCheckerTests {

  private static final Long IRRELEVANT_DONOR_ID = 99L;

  @InjectMocks
  private DonorConstraintChecker donorConstraintChecker;
  @Mock
  private DonorRepository donorRepository;
  @Mock
  private DonationRepository donationRepository;
  @Mock
  private DonorDeferralRepository donorReferralRepository;
  @Mock
  private DonorDeferralStatusCalculator donorDeferralStatusCalculator;

  @Test
  public void testCanDeleteDonorWithDonorWithNotes_shouldReturnFalse() {

    Donor donorWithNotes = aDonor().withNotes("irrelevant.notes").build();

    when(donorRepository.findDonorById(IRRELEVANT_DONOR_ID)).thenReturn(donorWithNotes);

    boolean canDelete = donorConstraintChecker.canDeleteDonor(IRRELEVANT_DONOR_ID);

    assertThat(canDelete, is(false));
  }

  @Test
  public void testCanDeleteDonorWithDonorWithDonations_shouldReturnFalse() {

    Donor donorWithDonations = aDonor().build();

    when(donorRepository.findDonorById(IRRELEVANT_DONOR_ID)).thenReturn(donorWithDonations);
    when(donationRepository.countDonationsForDonor(donorWithDonations)).thenReturn(1);

    boolean canDelete = donorConstraintChecker.canDeleteDonor(IRRELEVANT_DONOR_ID);

    assertThat(canDelete, is(false));
  }

  @Test
  public void testCanDeleteDonorWithDonorWithDeferrals_shouldReturnFalse() {

    Donor donorWithDeferrals = aDonor().build();

    when(donorRepository.findDonorById(IRRELEVANT_DONOR_ID)).thenReturn(donorWithDeferrals);
    when(donationRepository.countDonationsForDonor(donorWithDeferrals)).thenReturn(0);
    when(donorReferralRepository.countDonorDeferralsForDonor(donorWithDeferrals)).thenReturn(1);

    boolean canDelete = donorConstraintChecker.canDeleteDonor(IRRELEVANT_DONOR_ID);

    assertThat(canDelete, is(false));
  }

  @Test
  public void testCanDeleteDonorWithNoConstraints_shouldReturnTrue() {

    Donor existingDonor = aDonor().withNotes("").build();

    when(donorRepository.findDonorById(IRRELEVANT_DONOR_ID)).thenReturn(existingDonor);
    when(donationRepository.countDonationsForDonor(existingDonor)).thenReturn(0);
    when(donorReferralRepository.countDonorDeferralsForDonor(existingDonor)).thenReturn(0);

    boolean canDelete = donorConstraintChecker.canDeleteDonor(IRRELEVANT_DONOR_ID);

    assertThat(canDelete, is(true));
  }

  @Test
  public void testIsDonorEligibleToDonateWithNonDeferredDonorWithoutDonations_shouldReturnTrue() {

    Donor donor = aDonor().withId(IRRELEVANT_DONOR_ID).build();

    when(donorRepository.findDonorById(IRRELEVANT_DONOR_ID)).thenReturn(donor);
    when(donorDeferralStatusCalculator.isDonorCurrentlyDeferred(donor)).thenReturn(false);

    boolean isEligible = donorConstraintChecker.isDonorEligibleToDonate(IRRELEVANT_DONOR_ID);

    assertThat(isEligible, is(true));
  }

  @Test
  public void testIsDonorEligibleToDonateWithDeferredDonorWithoutDonations_shouldReturnFalse() {

    Donor donor = aDonor().withId(IRRELEVANT_DONOR_ID).build();

    when(donorRepository.findDonorById(IRRELEVANT_DONOR_ID)).thenReturn(donor);
    when(donorDeferralStatusCalculator.isDonorCurrentlyDeferred(donor)).thenReturn(true);

    boolean isEligible = donorConstraintChecker.isDonorEligibleToDonate(IRRELEVANT_DONOR_ID);

    assertThat(isEligible, is(false));
  }

  @Test
  public void testIsDonorEligibleToDonateWithNonDeferredDonorWithDonationNotCountedAsDonation_shouldReturnTrue() {

    Donor donor = aDonor()
        .withId(IRRELEVANT_DONOR_ID)
        .withDonation(aDonation()
            .withPackType(aPackType().withCountAsDonation(false).build())
            .build())
        .build();

    when(donorRepository.findDonorById(IRRELEVANT_DONOR_ID)).thenReturn(donor);
    when(donorDeferralStatusCalculator.isDonorCurrentlyDeferred(donor)).thenReturn(false);

    boolean isEligible = donorConstraintChecker.isDonorEligibleToDonate(IRRELEVANT_DONOR_ID);

    assertThat(isEligible, is(true));
  }

  @Test
  public void testIsDonorEligibleToDonateWithNonDeferredDonorWithDonationNotOverlappingPeriod_shouldReturnTrue() {

    Date previousDonationDate = new DateTime().minusDays(3).toDate();

    Donor donor = aDonor()
        .withId(IRRELEVANT_DONOR_ID)
        .withDonation(aDonation()
            .withDonationDate(previousDonationDate)
            .withPackType(aPackType()
                .withCountAsDonation(true)
                .withPeriodBetweenDonations(3)
                .build())
            .build())
        .build();

    when(donorRepository.findDonorById(IRRELEVANT_DONOR_ID)).thenReturn(donor);
    when(donorDeferralStatusCalculator.isDonorCurrentlyDeferred(donor)).thenReturn(false);

    boolean isEligible = donorConstraintChecker.isDonorEligibleToDonate(IRRELEVANT_DONOR_ID);

    assertThat(isEligible, is(true));
  }

  @Test
  public void testIsDonorEligibleToDonateWithNonDeferredDonorWithDonationOverlappingPeriod_shouldReturnFalse() {

    Date previousDonationDate = new DateTime().minusDays(3).toDate();

    Donor donor = aDonor()
        .withId(IRRELEVANT_DONOR_ID)
        .withDonation(aDonation()
            .withDonationDate(previousDonationDate)
            .withPackType(aPackType()
                .withCountAsDonation(true)
                .withPeriodBetweenDonations(5)
                .build())
            .build())
        .build();

    when(donorRepository.findDonorById(IRRELEVANT_DONOR_ID)).thenReturn(donor);
    when(donorDeferralStatusCalculator.isDonorCurrentlyDeferred(donor)).thenReturn(false);

    boolean isEligible = donorConstraintChecker.isDonorEligibleToDonate(IRRELEVANT_DONOR_ID);

    assertThat(isEligible, is(false));
  }

  @Test
  public void testIsDonorDeferred_shouldReturnTrue() {
    Donor donor = aDonor()
        .withId(IRRELEVANT_DONOR_ID)
        .build();

    when(donorRepository.findDonorById(IRRELEVANT_DONOR_ID)).thenReturn(donor);
    when(donorDeferralStatusCalculator.isDonorCurrentlyDeferred(donor)).thenReturn(true);

    boolean isDonorDeferred = donorConstraintChecker.isDonorDeferred(IRRELEVANT_DONOR_ID);
    assertThat(isDonorDeferred, is(true));
  }

  @Test
  public void testIsDonorDeferred_shouldReturnFalse() {
    Donor donor = aDonor()
        .withId(IRRELEVANT_DONOR_ID)
        .build();

    when(donorRepository.findDonorById(IRRELEVANT_DONOR_ID)).thenReturn(donor);
    when(donorDeferralStatusCalculator.isDonorCurrentlyDeferred(donor)).thenReturn(false);

    boolean isDonorDeferred = donorConstraintChecker.isDonorDeferred(IRRELEVANT_DONOR_ID);
    assertThat(isDonorDeferred, is(false));
  }

  @Test
  public void testIsDonorEligibleToDonateTodayWithNonDeferredDonorWithRecentDonation_shouldReturnFalse() {

    Date previousDonationDate = new DateTime().minusDays(3).toDate();
    Date mobileClinicDate = new Date();

    Donor donor = aDonor()
        .withId(IRRELEVANT_DONOR_ID)
        .withDonation(aDonation()
            .withDonationDate(previousDonationDate)
            .withPackType(aPackType()
                .withCountAsDonation(true)
                .withPeriodBetweenDonations(5)
                .build())
            .build())
        .build();

    when(donorRepository.findDonorById(IRRELEVANT_DONOR_ID)).thenReturn(donor);
    when(donorDeferralStatusCalculator.isDonorDeferredOnDate(donor, mobileClinicDate)).thenReturn(false);

    boolean isEligible = donorConstraintChecker.isDonorEligibleToDonateOnDate(IRRELEVANT_DONOR_ID, mobileClinicDate);

    assertThat(isEligible, is(false));
  }

  @Test
  public void testIsDonorEligibleToDonateNextWeekWithNonDeferredDonorWithRecentDonation_shouldReturnTrue() {

    Date previousDonationDate = new DateTime().minusDays(3).toDate();
    Date futureMobileClinicDate = new DateTime().plusDays(7).toDate();

    Donor donor = aDonor()
        .withId(IRRELEVANT_DONOR_ID)
        .withDonation(aDonation()
            .withDonationDate(previousDonationDate)
            .withPackType(aPackType()
                .withCountAsDonation(true)
                .withPeriodBetweenDonations(5)
                .build())
            .build())
        .build();

    when(donorRepository.findDonorById(IRRELEVANT_DONOR_ID)).thenReturn(donor);
    when(donorDeferralStatusCalculator.isDonorDeferredOnDate(donor, futureMobileClinicDate)).thenReturn(false);

    boolean isEligible = donorConstraintChecker.isDonorEligibleToDonateOnDate(IRRELEVANT_DONOR_ID, futureMobileClinicDate);

    assertThat(isEligible, is(true));
  }

  @Test
  public void testIsDonorEligibleToDonateNextWeekWithDeferredDonorWithRecentDonation_shouldReturnFalse() {

    Date previousDonationDate = new DateTime().minusDays(3).toDate();
    Date futureMobileClinicDate = new DateTime().plusDays(7).toDate();

    Donor donor = aDonor()
        .withId(IRRELEVANT_DONOR_ID)
        .withDonation(aDonation()
            .withDonationDate(previousDonationDate)
            .withPackType(aPackType()
                .withCountAsDonation(true)
                .withPeriodBetweenDonations(5)
                .build())
            .build())
        .build();

    when(donorRepository.findDonorById(IRRELEVANT_DONOR_ID)).thenReturn(donor);
    when(donorDeferralStatusCalculator.isDonorDeferredOnDate(donor, futureMobileClinicDate)).thenReturn(true);

    boolean isEligible = donorConstraintChecker.isDonorEligibleToDonateOnDate(IRRELEVANT_DONOR_ID, futureMobileClinicDate);

    assertThat(isEligible, is(false));
  }

  @Test
  public void testIsDonorEligibleToDonateNextWeekWithNonDeferredDonorWithNoDonation_shouldReturnTrue() {

    Date futureMobileClinicDate = new DateTime().plusDays(7).toDate();

    Donor donor = aDonor()
        .withId(IRRELEVANT_DONOR_ID)
        .build();

    when(donorRepository.findDonorById(IRRELEVANT_DONOR_ID)).thenReturn(donor);
    when(donorDeferralStatusCalculator.isDonorDeferredOnDate(donor, futureMobileClinicDate)).thenReturn(false);

    boolean isEligible = donorConstraintChecker.isDonorEligibleToDonateOnDate(IRRELEVANT_DONOR_ID, futureMobileClinicDate);

    assertThat(isEligible, is(true));
  }

  @Test
  public void testIsDonorEligibleToDonateNextWeekWithNonDeferredDonorWithDonationNotCountedAsDonation_shouldReturnTrue() {

    Date futureMobileClinicDate = new DateTime().plusDays(7).toDate();

    Donor donor = aDonor()
        .withId(IRRELEVANT_DONOR_ID)
        .withDonation(aDonation()
            .withPackType(aPackType().withCountAsDonation(false).build())
            .build())
        .build();

    when(donorRepository.findDonorById(IRRELEVANT_DONOR_ID)).thenReturn(donor);
    when(donorDeferralStatusCalculator.isDonorDeferredOnDate(donor, futureMobileClinicDate)).thenReturn(false);

    boolean isEligible = donorConstraintChecker.isDonorEligibleToDonateOnDate(IRRELEVANT_DONOR_ID, futureMobileClinicDate);

    assertThat(isEligible, is(true));
  }
}
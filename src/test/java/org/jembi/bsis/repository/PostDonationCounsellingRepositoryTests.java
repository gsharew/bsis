package org.jembi.bsis.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.jembi.bsis.helpers.builders.DonationBuilder.aDonation;
import static org.jembi.bsis.helpers.builders.DonorBuilder.aDonor;
import static org.jembi.bsis.helpers.builders.LocationBuilder.aVenue;
import static org.jembi.bsis.helpers.builders.PostDonationCounsellingBuilder.aPostDonationCounselling;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.NoResultException;

import org.jembi.bsis.model.counselling.PostDonationCounselling;
import org.jembi.bsis.model.donation.Donation;
import org.jembi.bsis.model.donor.Donor;
import org.jembi.bsis.model.location.Location;
import org.jembi.bsis.repository.PostDonationCounsellingRepository;
import org.jembi.bsis.suites.ContextDependentTestSuite;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PostDonationCounsellingRepositoryTests extends ContextDependentTestSuite {

  private static final Date NO_START_DATE = null;
  private static final Date NO_END_DATE = null;
  private static final Set<Long> NO_VENUES = null;

  @Autowired
  private PostDonationCounsellingRepository postDonationCounsellingRepository;

  @Test
  public void testFindDonorsFlaggedForCounsellingWithNoDates_shouldReturnDonorsFlaggedForCounselling() {

    Donation firstExpectedDonation = aDonation().build();
    Donation secondExpectedDonation = aDonation().build();

    List<Donation> expectedDonations = Arrays.asList(firstExpectedDonation, secondExpectedDonation);

    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(firstExpectedDonation)
        .buildAndPersist(entityManager);

    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(secondExpectedDonation)
        .buildAndPersist(entityManager);

    // Duplicate to test distinct donations
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(firstExpectedDonation)
        .buildAndPersist(entityManager);

    // Excluded by flag
    aPostDonationCounselling()
        .thatIsNotFlaggedForCounselling()
        .withDonation(aDonation().build())
        .buildAndPersist(entityManager);

    List<Donation> returnedDonations = postDonationCounsellingRepository.findDonationsFlaggedForCounselling(
        NO_START_DATE, NO_END_DATE, NO_VENUES);

    assertThat(returnedDonations, is(expectedDonations));
  }

  @Test
  public void testFindDonorsFlaggedForCounsellingWithVenues_shouldReturnDonorsWithDonationsForVenues() {

    Location firstVenue = aVenue().buildAndPersist(entityManager);
    Location secondVenue = aVenue().buildAndPersist(entityManager);
    List<Long> venues = Arrays.asList(firstVenue.getId(), secondVenue.getId());

    Donation firstExpectedDonation = aDonation().withVenue(firstVenue).build();
    Donation secondExpectedDonation = aDonation().withVenue(secondVenue).build();
    List<Donation> expectedDonations = Arrays.asList(firstExpectedDonation, secondExpectedDonation);

    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(firstExpectedDonation)
        .buildAndPersist(entityManager);

    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(secondExpectedDonation)
        .buildAndPersist(entityManager);

    // Excluded by venue
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(aDonation()
            .withVenue(aVenue().build())
            .build())
        .buildAndPersist(entityManager);

    List<Donation> returnedDonations = postDonationCounsellingRepository.findDonationsFlaggedForCounselling(
        NO_START_DATE, NO_END_DATE, new HashSet<>(venues));

    assertThat(returnedDonations, is(expectedDonations));
  }

  @Test
  public void testFindDonorsFlaggedForCounsellingWithStartDate_shouldReturnDonorsWithDonationsAferStartDate() {
    DateTime startDate = new DateTime().minusDays(7);

    Donation firstExpectedDonation = aDonation().withDonationDate(startDate.toDate()).build();
    Donation secondExpectedDonation = aDonation().withDonationDate(startDate.plusDays(3).toDate()).build();
    List<Donation> expectedDonations = Arrays.asList(firstExpectedDonation, secondExpectedDonation);

    // Donation on start date
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(firstExpectedDonation)
        .buildAndPersist(entityManager);

    // Donation after start date
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(secondExpectedDonation)
        .buildAndPersist(entityManager);

    // Excluded by donation before start date
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(aDonation()
            .withDonor(aDonor().build())
            .withDonationDate(startDate.minusDays(3).toDate())
            .build())
        .buildAndPersist(entityManager);

    List<Donation> returnedDonations = postDonationCounsellingRepository.findDonationsFlaggedForCounselling(
        startDate.toDate(), NO_END_DATE, NO_VENUES);

    assertThat(returnedDonations, is(expectedDonations));
  }

  @Test
  public void testFindDonorsFlaggedForCounsellingWithEndDate_shouldReturnDonorsWithDonationsBeforeEndDate() {
    DateTime endDate = new DateTime().minusDays(7);

    Donation firstExpectedDonation = aDonation().withDonationDate(endDate.toDate()).build();
    Donation secondExpectedDonation = aDonation().withDonationDate(endDate.minusDays(3).toDate()).build();
    List<Donation> expectedDonations = Arrays.asList(firstExpectedDonation, secondExpectedDonation);

    // Donation on end date
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(firstExpectedDonation)
        .buildAndPersist(entityManager);

    // Donation before end date
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(secondExpectedDonation)
        .buildAndPersist(entityManager);

    // Excluded by donation after end date
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(aDonation()
            .withDonationDate(endDate.plusDays(3).toDate())
            .build())
        .buildAndPersist(entityManager);

    List<Donation> returnedDonations = postDonationCounsellingRepository.findDonationsFlaggedForCounselling(
        NO_START_DATE, endDate.toDate(), NO_VENUES);

    assertThat(returnedDonations, is(expectedDonations));
  }

  @Test
  public void testFindDonorsFlaggedForCounsellingWithDates_shouldReturnDonorsWithDonationsInDateRange() {
    DateTime startDate = new DateTime().minusDays(14);
    DateTime endDate = new DateTime().minusDays(7);

    Donation expectedDonation = aDonation().withDonationDate(startDate.plusDays(1).toDate()).build();
    Donation expectedDonation2 = aDonation().withDonationDate(startDate.plusDays(2).toDate()).build();
    List<Donation> expectedDonations = Arrays.asList(expectedDonation);

    // Donation in date range
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(expectedDonation)
        .buildAndPersist(entityManager);

    // Excluded by isDeleted
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsDeleted()
        .withDonation(expectedDonation2)
        .buildAndPersist(entityManager);

    // Excluded by donation before start date
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(aDonation()
            .withDonationDate(startDate.minusDays(1).toDate())
            .build())
        .buildAndPersist(entityManager);

    // Excluded by donation after end date
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(aDonation()
            .withDonationDate(endDate.plusDays(1).toDate())
            .build())
        .buildAndPersist(entityManager);

    List<Donation> returnedDonations = postDonationCounsellingRepository.findDonationsFlaggedForCounselling(
        startDate.toDate(), endDate.toDate(), NO_VENUES);

    assertThat(returnedDonations, is(expectedDonations));
  }

  @Test(expected = NoResultException.class)
  public void testFindFlaggedPostDonationCounsellingForDonorWithNoPostDonationCounselling_shouldThrow() {

    Donor donor = aDonor().buildAndPersist(entityManager);

    postDonationCounsellingRepository.findPostDonationCounsellingForDonor(donor.getId());
  }

  @Test
  public void testFindFlaggedPostDonationCounsellingForDonor_shouldReturnFirstFlaggedPostDonationCounsellingForDonor() {

    Donor donor = aDonor().build();

    // Excluded by date
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(aDonation()
            .withDonor(donor)
            .withDonationDate(new DateTime().minusDays(3).toDate())
            .build())
        .buildAndPersist(entityManager);

    // Excluded by donor
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(aDonation()
            .withDonor(aDonor().build())
            .withDonationDate(new DateTime().minusDays(7).toDate())
            .build())
        .buildAndPersist(entityManager);

    // Excluded by isDeleted
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsDeleted()
        .withDonation(aDonation()
            .withDonor(donor)
            .withDonationDate(new DateTime().minusDays(5).toDate())
            .build())
        .buildAndPersist(entityManager);

    PostDonationCounselling expectedPostDonationCounselling = aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(aDonation()
            .withDonor(donor)
            .withDonationDate(new DateTime().minusDays(5).toDate())
            .build())
        .buildAndPersist(entityManager);

    PostDonationCounselling returnedPostDonationCounselling = postDonationCounsellingRepository
        .findPostDonationCounsellingForDonor(donor.getId());

    assertThat(returnedPostDonationCounselling, is(expectedPostDonationCounselling));
  }

  @Test
  public void testCountFlaggedPostDonationCounsellingsForDonorWithNoPostDonationCounsellings_shouldReturnZero() {

    Donor donor = aDonor().buildAndPersist(entityManager);

    int returnedCount = postDonationCounsellingRepository.countFlaggedPostDonationCounsellingsForDonor(donor.getId());

    assertThat(returnedCount, is(0));
  }

  @Test
  public void testCountFlaggedPostDonationCounsellingsForDonor_shouldReturnCorrectCount() {

    Donor donor = aDonor().build();

    // Excluded by donor
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(aDonation()
            .withDonor(aDonor().build())
            .build())
        .buildAndPersist(entityManager);

    // Excluded by flag
    aPostDonationCounselling()
        .thatIsNotFlaggedForCounselling()
        .withDonation(aDonation().withDonor(donor).build())
        .buildAndPersist(entityManager);

    // Excluded by isDeleted
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsDeleted()
        .withDonation(aDonation().withDonor(donor).build())
        .buildAndPersist(entityManager);

    // Expected
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(aDonation().withDonor(donor).build())
        .buildAndPersist(entityManager);
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(aDonation().withDonor(donor).build())
        .buildAndPersist(entityManager);

    int returnedCount = postDonationCounsellingRepository.countFlaggedPostDonationCounsellingsForDonor(donor.getId());

    assertThat(returnedCount, is(2));
  }

  @Test
  public void testCountNotFlaggedPostDonationCounsellingsForDonor_shouldReturnCorrectCount() {
    Donor donor = aDonor().build();

    // Excluded by donor
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(aDonation()
            .withDonor(aDonor().build())
            .build())
        .buildAndPersist(entityManager);

    // Excluded by flag
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .withDonation(aDonation().withDonor(donor).build())
        .buildAndPersist(entityManager);

    // Excluded by isDeleted
    aPostDonationCounselling()
        .thatIsFlaggedForCounselling()
        .thatIsDeleted()
        .withDonation(aDonation().withDonor(donor).build())
        .buildAndPersist(entityManager);

    // Expected
    aPostDonationCounselling()
        .thatIsNotFlaggedForCounselling()
        .thatIsNotDeleted()
        .withDonation(aDonation().withDonor(donor).build())
        .buildAndPersist(entityManager);

    int returnedCount = postDonationCounsellingRepository.countNotFlaggedPostDonationCounsellingsForDonor(donor.getId());

    assertThat(returnedCount, is(1));

  }

  @Test
  public void testFindPostDonationCounsellingForDonationWithNoExistingCounselling_shouldReturnNull() {

    Donation donation = aDonation().buildAndPersist(entityManager);

    PostDonationCounselling returnedCounselling = postDonationCounsellingRepository.findPostDonationCounsellingForDonation(
        donation);

    assertThat(returnedCounselling, is(nullValue()));
  }

  @Test
  public void testFindPostDonationCounsellingForDonation_shouldReturnCorrectCounselling() {

    Donation donation = aDonation().buildAndPersist(entityManager);

    // Excluded by donation - persisted before expected counselling to check order
    aPostDonationCounselling().withDonation(aDonation().build()).buildAndPersist(entityManager);

    PostDonationCounselling expectedCounselling = aPostDonationCounselling()
        .withDonation(donation)
        .buildAndPersist(entityManager);

    // Excluded by donation - persisted after expected counselling to check order
    aPostDonationCounselling().withDonation(aDonation().build()).buildAndPersist(entityManager);

    PostDonationCounselling returnedCounselling = postDonationCounsellingRepository.findPostDonationCounsellingForDonation(
        donation);

    assertThat(returnedCounselling, is(expectedCounselling));
  }
}
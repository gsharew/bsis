package org.jembi.bsis.repository.bloodtesting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jembi.bsis.model.bloodtesting.BloodTest;
import org.jembi.bsis.model.bloodtesting.BloodTestCategory;
import org.jembi.bsis.model.bloodtesting.BloodTestResult;
import org.jembi.bsis.model.bloodtesting.BloodTestType;
import org.jembi.bsis.model.bloodtesting.TTIStatus;
import org.jembi.bsis.model.donation.Donation;
import org.jembi.bsis.repository.DonationBatchRepository;
import org.jembi.bsis.repository.DonationRepository;
import org.jembi.bsis.service.BloodTestingRuleEngine;
import org.jembi.bsis.viewmodel.BloodTestResultViewModel;
import org.jembi.bsis.viewmodel.BloodTestingRuleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class BloodTestingRepository {

  private static final Logger LOGGER = Logger.getLogger(BloodTestingRepository.class);

  @PersistenceContext
  private EntityManager em;

  @Autowired
  private DonationRepository donationRepository;

  @Autowired
  private DonationBatchRepository donationBatchRepository;

  @Autowired
  private BloodTestRepository bloodTestRepository;

  @Autowired
  private BloodTestingRuleEngine ruleEngine;

  public List<BloodTest> getBloodTypingTests() {
    String queryStr = "SELECT b FROM BloodTest b WHERE b.isActive=:isActive AND b.category=:category";
    TypedQuery<BloodTest> query = em.createQuery(queryStr, BloodTest.class);
    query.setParameter("isActive", true);
    query.setParameter("category", BloodTestCategory.BLOODTYPING);
    List<BloodTest> bloodTests = query.getResultList();
    return bloodTests;
  }

  public List<BloodTest> getBloodTestsOfType(BloodTestType type) {
    return getBloodTestsOfTypes(Arrays.asList(type));
  }

  private List<BloodTest> getBloodTestsOfTypes(List<BloodTestType> types) {
    String queryStr = "SELECT b FROM BloodTest b WHERE "
        + "b.bloodTestType IN (:types) AND " + "b.isActive=:isActive";
    TypedQuery<BloodTest> query = em.createQuery(queryStr, BloodTest.class);
    query.setParameter("types", types);
    query.setParameter("isActive", true);
    List<BloodTest> bloodTests = query.getResultList();
    return bloodTests;
  }

  /**
   * Save the BloodTestingRuleResult and update the Donation blood ABO/Rh and blood typing statuses
   *
   * @param bloodTestResultsForDonation Map of test results with the BloodTest identifier as the
   *                                    key
   * @param donation                    Donation associated with the test results
   * @param testedOn                    Date the tests were done
   * @param ruleResult                  BloodTestingRuleResult from the BloodTestingRulesEngine
   * @param reEntry                     boolean true if the results are the re-entry and false if the results are first entry
   */
  public void saveBloodTestResultsToDatabase(
      Map<Long, String> bloodTestResultsForDonation,
      Donation donation, Date testedOn,
      BloodTestingRuleResult ruleResult,
      boolean reEntry) {

    Map<Long, BloodTestResult> mostRecentTestResults = getRecentTestResultsForDonation(donation.getId());
    for (Long testId : bloodTestResultsForDonation.keySet()) {
      BloodTestResult btResult = mostRecentTestResults.get(testId);
      updateOrCreateBloodTestResult(btResult, testId, bloodTestResultsForDonation.get(testId), donation, testedOn, reEntry);
    }
    if (reEntry && ruleResult != null) {
      updateDonationWithTestResults(donation, ruleResult);
      em.persist(donation);
    }
  }

  private BloodTestResult updateOrCreateBloodTestResult(BloodTestResult btResult, Long testId, String testResult,
      Donation donation, Date testedOn, boolean reEntry) {

    if (btResult == null) {
      btResult = new BloodTestResult();
      BloodTest bloodTest = findBloodTestById(testId);
      btResult.setBloodTest(bloodTest);
      // not updating the inverse relation which means the
      // donation.getBloodTypingResults() will not
      // contain this result
      btResult.setDonation(donation);
      btResult.setTestedOn(testedOn);
      btResult.setNotes("");
      btResult.setResult(testResult);
      // re-entry is not always required for initial tests, depends on the implementation, and it's
      // controlled from the frontend
      btResult.setReEntryRequired(!reEntry);
    } else {
      if (!testResult.equals(btResult.getResult())) {
        btResult.setResult(testResult);
        // re-entry is only required if the initial test result is being modified
        btResult.setReEntryRequired(!reEntry);
      } else {
        // only clear the re-entry required flag if the update is a re-entry
        if (btResult.getReEntryRequired() && reEntry) {
          btResult.setReEntryRequired(false);
        }
      }
    }
    em.persist(btResult);
    return btResult;
  }

  public List<BloodTest> findActiveBloodTests() {

    return em.createQuery(
        "SELECT b " +
            "FROM BloodTest b " +
            "WHERE b.isActive = :isActive ",
            BloodTest.class)
        .setParameter("isActive", true)
        .getResultList();
  }

  public List<BloodTest> getAllBloodTestsIncludeInactive() {
    String queryStr = "SELECT b FROM BloodTest b";
    TypedQuery<BloodTest> query = em.createQuery(queryStr, BloodTest.class);
    List<BloodTest> bloodTests = query.getResultList();
    return bloodTests;
  }

  public List<BloodTestingRuleResult> getAllTestsStatusForDonationBatches(
      List<Long> donationBatchIds) {

    List<BloodTestingRuleResult> bloodTestingRuleResults = new ArrayList<BloodTestingRuleResult>();

    for (Long donationBatchId : donationBatchIds) {
      List<Donation> donations = donationBatchRepository.findDonationsInBatch(donationBatchId);

      for (Donation donation : donations) {

        if (!donation.getPackType().getTestSampleProduced()) {
          // This donation did not produce a test sample so skip it
          continue;
        }

        BloodTestingRuleResult ruleResult = ruleEngine.applyBloodTests(
            donation, new HashMap<Long, String>());
        bloodTestingRuleResults.add(ruleResult);
      }
    }

    return bloodTestingRuleResults;
  }

  public List<BloodTestingRuleResult> getAllTestsStatusForDonationBatchesByBloodTestType(List<Long> donationBatchIds,
                                                                                         BloodTestType bloodTestType) {

    List<BloodTestingRuleResult> bloodTestingRuleResults = getAllTestsStatusForDonationBatches(donationBatchIds);
    List<BloodTestingRuleResult> filteredRuleResults = new ArrayList<BloodTestingRuleResult>();
    for (BloodTestingRuleResult result : bloodTestingRuleResults) {
      Map<String, BloodTestResultViewModel> modelMap = result.getRecentTestResults();
      Map<String, BloodTestResultViewModel> filteredModelMap = new HashMap<String, BloodTestResultViewModel>();
      for (String key : modelMap.keySet()) {
        BloodTestResultViewModel model = modelMap.get(key);
        if (model.getBloodTest().getBloodTestType().equals(bloodTestType)) {
          filteredModelMap.put(key, model);
        }
      }
      result.setRecentTestResults(filteredModelMap);
      filteredRuleResults.add(result);
    }
    bloodTestingRuleResults = filteredRuleResults;

    return bloodTestingRuleResults;
  }

  public BloodTestingRuleResult getAllTestsStatusForDonation(
      Long donationId) {
    Donation donation = donationRepository
        .findDonationById(donationId);
    return ruleEngine.applyBloodTests(donation,
        new HashMap<Long, String>());
  }

  public Map<Long, BloodTestResult> getRecentTestResultsForDonation(
      Long donationId) {
    String queryStr = "SELECT bt FROM BloodTestResult bt WHERE "
        + "bt.donation.id=:donationId AND bt.isDeleted = :testOutcomeDeleted";
    TypedQuery<BloodTestResult> query = em.createQuery(queryStr,
        BloodTestResult.class);
    query.setParameter("donationId", donationId);
    query.setParameter("testOutcomeDeleted", false);
    List<BloodTestResult> bloodTestResults = query.getResultList();
    Map<Long, BloodTestResult> recentBloodTestResults = new HashMap<Long, BloodTestResult>();
    for (BloodTestResult bt : bloodTestResults) {
      Long bloodTestId = bt.getBloodTest().getId();
      BloodTestResult existingBloodTestResult = recentBloodTestResults
          .get(bloodTestId);
      if (existingBloodTestResult == null) {
        recentBloodTestResults.put(bloodTestId, bt);
      } else if (existingBloodTestResult.getTestedOn().before(
          bt.getTestedOn())) {
        // before is very important here
        recentBloodTestResults.put(bloodTestId, bt);
      }
    }
    return recentBloodTestResults;
  }

  /**
   * Compare two strings and check that they are either both empty, or they are equal.
   *
   * @param first First string
   * @param second First string
   * @return true if they are empty or equal, otherwise false.
   */
  private boolean bothEmptyOrEquals(String first, String second) {

    if (StringUtils.isEmpty(first)) {
      return StringUtils.isEmpty(second);
    }

    return first.equals(second);
  }

  /**
   * FIXME: this method belongs in the BloodTestsService and has replaced the BloodTestsUpdatedEvent
   * Because there are many references in this repository class, to minimise changes, it was added here.
   */
  public boolean updateDonationWithTestResults(Donation donation, BloodTestingRuleResult ruleResult) {
    boolean donationUpdated = false;

    String oldExtraInformation = donation.getExtraBloodTypeInformation();
    String newExtraInformation = addNewExtraInformation(oldExtraInformation, ruleResult.getExtraInformation());

    String oldBloodAbo = donation.getBloodAbo();
    String newBloodAbo = ruleResult.getBloodAbo();

    String oldBloodRh = donation.getBloodRh();
    String newBloodRh = ruleResult.getBloodRh();

    TTIStatus oldTtiStatus = donation.getTTIStatus();
    TTIStatus newTtiStatus = ruleResult.getTTIStatus();

    BloodTypingStatus oldBloodTypingStatus = donation.getBloodTypingStatus();
    BloodTypingStatus newBloodTypingStatus = ruleResult.getBloodTypingStatus();

    BloodTypingMatchStatus oldBloodTypingMatchStatus = donation.getBloodTypingMatchStatus();
    BloodTypingMatchStatus newBloodTypingMatchStatus = ruleResult.getBloodTypingMatchStatus();

    if (!bothEmptyOrEquals(newExtraInformation, oldExtraInformation) || !bothEmptyOrEquals(newBloodAbo, oldBloodAbo)
        || !bothEmptyOrEquals(newBloodRh, oldBloodRh) || !Objects.equals(newTtiStatus, oldTtiStatus)
        || !Objects.equals(newBloodTypingStatus, oldBloodTypingStatus)
        || !Objects.equals(oldBloodTypingMatchStatus, newBloodTypingMatchStatus)) {
      donation.setExtraBloodTypeInformation(newExtraInformation);
      donation.setBloodAbo(newBloodAbo);
      donation.setBloodRh(newBloodRh);
      donation.setTTIStatus(ruleResult.getTTIStatus());
      donation.setBloodTypingStatus(ruleResult.getBloodTypingStatus());
      donation.setBloodTypingMatchStatus(ruleResult.getBloodTypingMatchStatus());

      donationUpdated = true;
    }

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Updating Donation '" + donation.getId() + "' with Abo/Rh="
          + donation.getBloodAbo() + donation.getBloodRh() + " TTIStatus="
          + donation.getTTIStatus() + " BloodTypingStatus=" + donation.getBloodTypingStatus()
          + " " + donation.getBloodTypingMatchStatus());
    }

    return donationUpdated;
  }

  /**
   * FIXME: this method also belongs in the BloodTestsService (see above updateDonationWithTestResults)
   */
  private String addNewExtraInformation(String donationExtraInformation, Set<String> extraInformationNewSet) {
    String newExtraInformation;
    Set<String> oldExtraInformationSet = new HashSet<String>();
    if (StringUtils.isNotBlank(donationExtraInformation)) {
      oldExtraInformationSet.addAll(Arrays.asList(donationExtraInformation.split(",")));
      extraInformationNewSet.removeAll(oldExtraInformationSet); // remove duplicates
      newExtraInformation = donationExtraInformation + StringUtils.join(extraInformationNewSet, ",");
    } else {
      newExtraInformation = StringUtils.join(extraInformationNewSet, ",");
    }
    return newExtraInformation;
  }
  
  private BloodTest findBloodTestById(Long bloodTestId) {
    String queryStr = "SELECT bt FROM BloodTest bt WHERE " + "bt.id=:bloodTestId";
    TypedQuery<BloodTest> query = em.createQuery(queryStr, BloodTest.class);
    query.setParameter("bloodTestId", bloodTestId);
    return query.getSingleResult();
  }
}

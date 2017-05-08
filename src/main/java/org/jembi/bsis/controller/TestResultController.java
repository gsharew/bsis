package org.jembi.bsis.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.jembi.bsis.backingform.TestResultsBackingForms;
import org.jembi.bsis.backingform.validator.TestResultsBackingFormsValidator;
import org.jembi.bsis.factory.DonationFactory;
import org.jembi.bsis.factory.TestBatchFactory;
import org.jembi.bsis.model.bloodtesting.BloodTestType;
import org.jembi.bsis.model.donation.BloodTypingMatchStatus;
import org.jembi.bsis.model.donation.Donation;
import org.jembi.bsis.model.donationbatch.DonationBatch;
import org.jembi.bsis.model.testbatch.TestBatch;
import org.jembi.bsis.repository.DonationRepository;
import org.jembi.bsis.repository.TestBatchRepository;
import org.jembi.bsis.repository.bloodtesting.BloodTestingRepository;
import org.jembi.bsis.service.BloodTestsService;
import org.jembi.bsis.utils.CustomDateFormatter;
import org.jembi.bsis.utils.PermissionConstants;
import org.jembi.bsis.viewmodel.BloodTestFullViewModel;
import org.jembi.bsis.viewmodel.BloodTestResultViewModel;
import org.jembi.bsis.viewmodel.BloodTestingRuleResult;
import org.jembi.bsis.viewmodel.DonationTestOutcomesReportViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Transactional
@RestController
@RequestMapping("testresults")
public class TestResultController {

  @Autowired
  private DonationRepository donationRepository;

  @Autowired
  private TestBatchRepository testBatchRepository;

  @Autowired
  private BloodTestingRepository bloodTestingRepository;

  @Autowired
  private BloodTestsService bloodTestsService;

  @Autowired
  private TestBatchFactory testBatchViewModelFactory;

  @Autowired
  private TestResultsBackingFormsValidator testResultsBackingFormsValidator;

  @Autowired
  private DonationFactory donationFactory;

  @InitBinder
  protected void initDonationFormBinder(WebDataBinder binder) {
    binder.setValidator(testResultsBackingFormsValidator);
  }

  @RequestMapping(value = "{donationIdentificationNumber}", method = RequestMethod.GET)
  @PreAuthorize("hasRole('" + PermissionConstants.VIEW_TEST_OUTCOME + "')")
  public ResponseEntity<Map<String, Object>> findTestResult(@PathVariable String donationIdentificationNumber) {

    Map<String, Object> map = new HashMap<String, Object>();
    Donation c = donationRepository.findDonationByDonationIdentificationNumber(donationIdentificationNumber);
    map.put("donation", donationFactory.createDonationViewModelWithoutPermissions(c));

    if (c.getPackType().getTestSampleProduced()) {
      BloodTestingRuleResult results = bloodTestingRepository.getAllTestsStatusForDonation(c.getId());
      map.put("testResults", results);
    } else {
      map.put("testResults", null);
    }
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  @RequestMapping(value = "/search", method = RequestMethod.GET)
  @PreAuthorize("hasRole('" + PermissionConstants.VIEW_TEST_OUTCOME + "')")
  public ResponseEntity<Map<String, Object>> findTestResultsForTestBatch(HttpServletRequest request, @RequestParam(
      value = "testBatch", required = true) UUID testBatchId,
      @RequestParam(value = "bloodTestType", required = false) BloodTestType bloodTestType) {

    TestBatch testBatch = testBatchRepository.findTestBatchById(testBatchId);
    List<BloodTestingRuleResult> ruleResults = getBloodTestingRuleResults(bloodTestType, testBatch);

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("testResults", ruleResults);
    map.put("testBatchCreatedDate", CustomDateFormatter.format(testBatch.getCreatedDate()));

    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  @RequestMapping(value = "/report", method = RequestMethod.GET)
  @PreAuthorize("hasRole('" + PermissionConstants.VIEW_TEST_OUTCOME + "')")
  public ResponseEntity<Map<String, Object>> getTestBatchOutcomesReport(@RequestParam(value = "testBatch",
      required = true) UUID testBatchId) {

    TestBatch testBatch = testBatchRepository.findTestBatchById(testBatchId);
    List<DonationTestOutcomesReportViewModel> donationTestOutcomesReports =
        testBatchViewModelFactory.createDonationTestOutcomesReportViewModels(testBatch);

    Map<String, Object> map = bloodTestsService.getBloodTestShortNames();
    map.put("donationTestOutcomesReports", donationTestOutcomesReports);
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  @RequestMapping(value = "/overview", method = RequestMethod.GET)
  @PreAuthorize("hasRole('" + PermissionConstants.VIEW_TEST_OUTCOME + "')")
  public ResponseEntity<Map<String, Object>> findTestResultsOverviewForTestBatch(HttpServletRequest request,
      @RequestParam(value = "testBatch", required = true) UUID testBatchId) {

    TestBatch testBatch = testBatchRepository.findTestBatchById(testBatchId);
    List<BloodTestingRuleResult> ruleResults = getBloodTestingRuleResults(testBatch);
    Map<String, Object> map = calculateOverviewFlags(ruleResults);
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  protected List<BloodTestingRuleResult> getBloodTestingRuleResults(TestBatch testBatch) {
    return getBloodTestingRuleResults(null, testBatch);
  }

  protected List<BloodTestingRuleResult> getBloodTestingRuleResults(BloodTestType bloodTestType, TestBatch testBatch) {
    Set<DonationBatch> donationBatches = testBatch.getDonationBatches();
    List<UUID> donationBatchIds = new ArrayList<UUID>();
    for (DonationBatch donationBatch : donationBatches) {
      donationBatchIds.add(donationBatch.getId());
    }

    List<BloodTestingRuleResult> ruleResults;
    if (bloodTestType == null) {
      ruleResults = bloodTestingRepository.getAllTestsStatusForDonationBatches(donationBatchIds);
    } else {
      ruleResults =
          bloodTestingRepository.getAllTestsStatusForDonationBatchesByBloodTestType(donationBatchIds, bloodTestType);
    }
    return ruleResults;
  }

  private Map<String, Object> calculateOverviewFlags(List<BloodTestingRuleResult> ruleResults) {

    Map<String, Object> overviewFlags = new HashMap<String, Object>();
    overviewFlags.put("hasReEntryRequiredTTITests", false);
    overviewFlags.put("hasReEntryRequiredBloodTypingTests", false);
    overviewFlags.put("hasReEntryRequiredRepeatBloodTypingTests", false);
    overviewFlags.put("hasReEntryRequiredConfirmatoryTTITests", false);
    overviewFlags.put("hasReEntryRequiredRepeatTTITests", false);
    overviewFlags.put("hasRepeatBloodTypingTests", false);
    overviewFlags.put("hasConfirmatoryTTITests", false);
    overviewFlags.put("hasRepeatTTITests", false);
    overviewFlags.put("hasPendingRepeatTTITests", false);
    overviewFlags.put("hasPendingConfirmatoryTTITests", false);
    overviewFlags.put("hasPendingRepeatBloodTypingTests", false);
    overviewFlags.put("hasPendingBloodTypingConfirmations", false);

    for (BloodTestingRuleResult result : ruleResults) {

      Map<Long, BloodTestResultViewModel> resultViewModelMap = result.getRecentTestResults();
      for (Long key : resultViewModelMap.keySet()) {
        BloodTestResultViewModel bloodTestResultViewModel = resultViewModelMap.get(key);
        BloodTestFullViewModel bloodTest = bloodTestResultViewModel.getBloodTest();
        if (bloodTestResultViewModel.getReEntryRequired().equals(true)) {
          if (bloodTest.getBloodTestType().equals(BloodTestType.BASIC_TTI)) {
            overviewFlags.put("hasReEntryRequiredTTITests", true);
          } else if (bloodTest.getBloodTestType().equals(BloodTestType.BASIC_BLOODTYPING)) {
            overviewFlags.put("hasReEntryRequiredBloodTypingTests", true);
          } else if (bloodTest.getBloodTestType().equals(BloodTestType.REPEAT_BLOODTYPING)) {
            overviewFlags.put("hasReEntryRequiredRepeatBloodTypingTests", true);
          } else if (bloodTest.getBloodTestType().equals(BloodTestType.CONFIRMATORY_TTI)) {
            overviewFlags.put("hasReEntryRequiredConfirmatoryTTITests", true);
          } else if (bloodTest.getBloodTestType().equals(BloodTestType.REPEAT_TTI)) {
            overviewFlags.put("hasReEntryRequiredRepeatTTITests", true);
          }
        }
        if (bloodTest.getBloodTestType().equals(BloodTestType.REPEAT_TTI)) {
          overviewFlags.put("hasRepeatTTITests", true);
        } else if (bloodTest.getBloodTestType().equals(BloodTestType.CONFIRMATORY_TTI)) {
          overviewFlags.put("hasConfirmatoryTTITests", true);
        } else if (bloodTest.getBloodTestType().equals(BloodTestType.REPEAT_BLOODTYPING)) {
          overviewFlags.put("hasRepeatBloodTypingTests", true);
        }
      }
      if (result.getPendingBloodTypingTestsIds().size() > 0) {
        overviewFlags.put("hasPendingRepeatBloodTypingTests", true);
        overviewFlags.put("hasRepeatBloodTypingTests", true);
      }
      if (result.getPendingConfirmatoryTTITestsIds().size() > 0) {
        overviewFlags.put("hasPendingConfirmatoryTTITests", true);
        overviewFlags.put("hasConfirmatoryTTITests", true);
      }
      if (result.getPendingRepeatTTITestsIds().size() > 0) {
        overviewFlags.put("hasPendingRepeatTTITests", true);
        overviewFlags.put("hasRepeatTTITests", true);
      }
      if (result.getBloodTypingMatchStatus().equals(BloodTypingMatchStatus.AMBIGUOUS)) {
        // A confirmation is required to resolve the ambiguous result.
        overviewFlags.put("hasPendingBloodTypingConfirmations", true);
      }
    }
    return overviewFlags;
  }

  @PreAuthorize("hasRole('" + PermissionConstants.ADD_TEST_OUTCOME + "')")
  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity<Map<String, Object>> saveTestResults(
      @RequestBody @Valid TestResultsBackingForms testResultsBackingForms, @RequestParam(value = "reEntry",
          required = false, defaultValue = "false") boolean reEntry) {

    HttpStatus responseStatus = HttpStatus.CREATED;
    Map<String, Object> responseMap = new HashMap<>();
    bloodTestsService.saveBloodTests(testResultsBackingForms.getTestOutcomesForDonations(), reEntry);
    return new ResponseEntity<>(responseMap, responseStatus);
  }

}

package org.jembi.bsis.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jembi.bsis.helpers.builders.DonationBuilder.aDonation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jembi.bsis.helpers.builders.BloodTestBuilder;
import org.jembi.bsis.model.bloodtesting.BloodTest;
import org.jembi.bsis.model.bloodtesting.BloodTestCategory;
import org.jembi.bsis.model.bloodtesting.BloodTestResult;
import org.jembi.bsis.model.bloodtesting.rules.BloodTestingRule;
import org.jembi.bsis.model.bloodtesting.rules.BloodTestingRuleResultSet;
import org.jembi.bsis.model.donation.BloodTypingMatchStatus;
import org.jembi.bsis.model.donation.BloodTypingStatus;
import org.jembi.bsis.model.donation.Donation;
import org.jembi.bsis.model.donation.TTIStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BloodTestResultConstraintCheckerTests {

  private static final boolean DONATION_RELEASED = true;
  private static final boolean DONATION_NOT_RELEASED = false;

  @InjectMocks
  private BloodTestResultConstraintChecker bloodTestResultConstraintChecker;

  @Test
  public void testCanEditCompleteAmbiguousBloodTest() {
    Donation donation = aDonation().build();
    BloodTest bloodTest = new BloodTest();
    bloodTest.setCategory(BloodTestCategory.BLOODTYPING);
    BloodTestResult bloodTestResult = new BloodTestResult();
    bloodTestResult.setDonation(donation);
    bloodTestResult.setBloodTest(bloodTest);
    BloodTestingRuleResultSet bloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), new HashMap<Long, BloodTestResult>(),
        new ArrayList<BloodTestingRule>());
    bloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.AMBIGUOUS);
    bloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);

    boolean canEdit = bloodTestResultConstraintChecker.canEdit(bloodTestingRuleResultSet, bloodTestResult, DONATION_NOT_RELEASED);

    assertThat(canEdit, is(true));
  }

  @Test
  public void testCanEditCompleteNoMatchBloodTest() {
    Donation donation = aDonation().build();
    BloodTest bloodTest = new BloodTest();
    bloodTest.setCategory(BloodTestCategory.BLOODTYPING);
    BloodTestResult bloodTestResult = new BloodTestResult();
    bloodTestResult.setDonation(donation);
    bloodTestResult.setBloodTest(bloodTest);
    BloodTestingRuleResultSet bloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), new HashMap<Long, BloodTestResult>(),
        new ArrayList<BloodTestingRule>());
    bloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.NO_MATCH);
    bloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);

    boolean canEdit = bloodTestResultConstraintChecker.canEdit(bloodTestingRuleResultSet, bloodTestResult, DONATION_NOT_RELEASED);

    assertThat(canEdit, is(true));
  }

  @Test
  public void testCanEditCompleteNoMatchBloodTestWithOtherPendingAboTests() {
    Donation donation = aDonation().build();
    BloodTest bloodTest = new BloodTest();
    bloodTest.setCategory(BloodTestCategory.BLOODTYPING);
    bloodTest.setId(1l);
    BloodTestResult bloodTestResult = new BloodTestResult();
    bloodTestResult.setDonation(donation);
    bloodTestResult.setBloodTest(bloodTest);
    BloodTestingRuleResultSet bloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), new HashMap<Long, BloodTestResult>(),
        generateTestBloodTestingRules());
    bloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.MATCH);
    bloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);
    List<Long> pendingTestIds = new ArrayList<>();
    pendingTestIds.add(123L);
    bloodTestingRuleResultSet.setPendingAboTestsIds(pendingTestIds);
    BloodTestingRule rule1 = new BloodTestingRule();
    rule1.setBloodTest(BloodTestBuilder.aBasicBloodTypingBloodTest().withId(2L).build());
    rule1.setPendingTestsIds("123");
    List<BloodTestingRule> rules = new ArrayList<>();
    rules.add(rule1);
    bloodTestingRuleResultSet.setBloodTestingRules(rules);

    boolean canEdit = bloodTestResultConstraintChecker.canEdit(bloodTestingRuleResultSet, bloodTestResult, DONATION_NOT_RELEASED);

    assertThat(canEdit, is(true));
  }

  @Test
  public void testCanEditCompleteNoMatchBloodTestWithAboPendingTests() {
    Donation donation = aDonation().build();
    BloodTest bloodTest = new BloodTest();
    bloodTest.setCategory(BloodTestCategory.BLOODTYPING);
    bloodTest.setId(1l);
    BloodTestResult bloodTestResult = new BloodTestResult();
    bloodTestResult.setDonation(donation);
    bloodTestResult.setBloodTest(bloodTest);
    BloodTestingRuleResultSet bloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), new HashMap<Long, BloodTestResult>(),
        new ArrayList<BloodTestingRule>());
    bloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.MATCH);
    bloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);
    List<Long> pendingTestIds = new ArrayList<>();
    pendingTestIds.add(123L);
    bloodTestingRuleResultSet.setPendingAboTestsIds(pendingTestIds);
    BloodTestingRule rule1 = new BloodTestingRule();
    rule1.setBloodTest(bloodTest);
    rule1.setPendingTestsIds("123");
    List<BloodTestingRule> rules = new ArrayList<>();
    rules.add(rule1);
    bloodTestingRuleResultSet.setBloodTestingRules(rules);

    boolean canEdit = bloodTestResultConstraintChecker.canEdit(bloodTestingRuleResultSet, bloodTestResult, DONATION_NOT_RELEASED);

    assertThat(canEdit, is(true));
  }

  @Test
  public void testCanEditCompleteNoMatchBloodTestWithOtherPendingRhTests() {
    Donation donation = aDonation().build();
    BloodTest bloodTest = new BloodTest();
    bloodTest.setCategory(BloodTestCategory.BLOODTYPING);
    bloodTest.setId(1l);
    BloodTestResult bloodTestResult = new BloodTestResult();
    bloodTestResult.setDonation(donation);
    bloodTestResult.setBloodTest(bloodTest);
    BloodTestingRuleResultSet bloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), new HashMap<Long, BloodTestResult>(),
        generateTestBloodTestingRules());
    bloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.MATCH);
    bloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);
    List<Long> pendingTestIds = new ArrayList<>();
    pendingTestIds.add(3L);
    bloodTestingRuleResultSet.setPendingRhTestsIds(pendingTestIds);
    BloodTestingRule rule1 = new BloodTestingRule();
    rule1.setBloodTest(BloodTestBuilder.aBasicBloodTypingBloodTest().withId(2L).build());
    rule1.setPendingTestsIds("3");
    List<BloodTestingRule> rules = new ArrayList<>();
    rules.add(rule1);
    bloodTestingRuleResultSet.setBloodTestingRules(rules);

    boolean canEdit = bloodTestResultConstraintChecker.canEdit(bloodTestingRuleResultSet, bloodTestResult, DONATION_NOT_RELEASED);

    assertThat(canEdit, is(true));
  }

  @Test
  public void testCanEditCompleteNoMatchBloodTestWithRhPendingTests() {
    Donation donation = aDonation().build();
    BloodTest bloodTest = new BloodTest();
    bloodTest.setCategory(BloodTestCategory.BLOODTYPING);
    bloodTest.setId(1l);
    BloodTestResult bloodTestResult = new BloodTestResult();
    bloodTestResult.setDonation(donation);
    bloodTestResult.setBloodTest(bloodTest);
    BloodTestingRuleResultSet bloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), new HashMap<Long, BloodTestResult>(),
        new ArrayList<BloodTestingRule>());
    bloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.MATCH);
    bloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);
    List<Long> pendingTestIds = new ArrayList<>();
    pendingTestIds.add(123L);
    bloodTestingRuleResultSet.setPendingRhTestsIds(pendingTestIds);
    BloodTestingRule rule1 = new BloodTestingRule();
    rule1.setBloodTest(bloodTest);
    rule1.setPendingTestsIds("123");
    List<BloodTestingRule> rules = new ArrayList<>();
    rules.add(rule1);
    bloodTestingRuleResultSet.setBloodTestingRules(rules);

    boolean canEdit = bloodTestResultConstraintChecker.canEdit(bloodTestingRuleResultSet, bloodTestResult, DONATION_NOT_RELEASED);

    assertThat(canEdit, is(true));
  }

  @Test
  public void testCanEditNotDoneBloodTest() {
    Donation donation = aDonation().build();
    BloodTest bloodTest = new BloodTest();
    bloodTest.setCategory(BloodTestCategory.BLOODTYPING);
    BloodTestResult bloodTestResult = new BloodTestResult();
    bloodTestResult.setDonation(donation);
    bloodTestResult.setBloodTest(bloodTest);
    BloodTestingRuleResultSet bloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), new HashMap<Long, BloodTestResult>(),
        new ArrayList<BloodTestingRule>());
    bloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.NO_MATCH);
    bloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.NOT_DONE);

    boolean canEdit = bloodTestResultConstraintChecker.canEdit(bloodTestingRuleResultSet, bloodTestResult, DONATION_NOT_RELEASED);

    assertThat(canEdit, is(true));
  }

  @Test
  public void testCanEditNoTypeDeterminedBloodTest() {
    Donation donation = aDonation().build();
    BloodTest bloodTest = new BloodTest();
    bloodTest.setCategory(BloodTestCategory.BLOODTYPING);
    BloodTestResult bloodTestResult = new BloodTestResult();
    bloodTestResult.setDonation(donation);
    bloodTestResult.setBloodTest(bloodTest);
    BloodTestingRuleResultSet bloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), new HashMap<Long, BloodTestResult>(),
        new ArrayList<BloodTestingRule>());
    bloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.NO_TYPE_DETERMINED);
    bloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);

    boolean canEdit = bloodTestResultConstraintChecker.canEdit(bloodTestingRuleResultSet, bloodTestResult, DONATION_NOT_RELEASED);

    assertThat(canEdit, is(false));
  }

  @Test
  public void testCanEditReleasedDonationBloodTypingTestResult() {
    Donation donation = aDonation().build();
    BloodTest bloodTest = new BloodTest();
    bloodTest.setCategory(BloodTestCategory.BLOODTYPING);
    BloodTestResult bloodTestResult = new BloodTestResult();
    bloodTestResult.setDonation(donation);
    bloodTestResult.setBloodTest(bloodTest);
    BloodTestingRuleResultSet bloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), new HashMap<Long, BloodTestResult>(),
        new ArrayList<BloodTestingRule>());
    bloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.MATCH);
    bloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);

    boolean canEdit = bloodTestResultConstraintChecker.canEdit(bloodTestingRuleResultSet, bloodTestResult, DONATION_RELEASED);

    assertThat(canEdit, is(false));
  }

  @Test
  public void testCanEditTTISafeWithNoPendingBloodTest() {
    Donation donation = aDonation().build();
    BloodTest bloodTest = new BloodTest();
    bloodTest.setCategory(BloodTestCategory.TTI);
    BloodTestResult bloodTestResult = new BloodTestResult();
    bloodTestResult.setDonation(donation);
    bloodTestResult.setBloodTest(bloodTest);
    BloodTestingRuleResultSet bloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), new HashMap<Long, BloodTestResult>(),
        generateTestBloodTestingRules());
    bloodTestingRuleResultSet.setTtiStatus(TTIStatus.SAFE);
    List<Long> pendingTtiTestIds = new ArrayList<>();
    bloodTestingRuleResultSet.setPendingRepeatAndConfirmatoryTtiTestsIds(pendingTtiTestIds);
    BloodTestingRule rule1 = new BloodTestingRule();
    rule1.setBloodTest(BloodTestBuilder.aBasicTTIBloodTest().withId(1L).build());
    rule1.setPendingTestsIds("2");
    List<BloodTestingRule> rules = new ArrayList<>();
    rules.add(rule1);
    bloodTestingRuleResultSet.setBloodTestingRules(rules);


    boolean canEdit = bloodTestResultConstraintChecker.canEdit(bloodTestingRuleResultSet, bloodTestResult, DONATION_NOT_RELEASED);

    assertThat(canEdit, is(true));
  }

  @Test
  public void testCanEditTTISafeWithPendingBloodTest() {
    Donation donation = aDonation().build();
    BloodTest bloodTest = new BloodTest();
    bloodTest.setCategory(BloodTestCategory.TTI);
    bloodTest.setId(1l);
    BloodTestResult bloodTestResult = new BloodTestResult();
    bloodTestResult.setDonation(donation);
    bloodTestResult.setBloodTest(bloodTest);
    bloodTestResult.setResult("OUTCOME");
    Map<Long, String> availableTestResults = new HashMap<Long, String>();
    availableTestResults.put(1L, "RESULT");
    BloodTestingRuleResultSet bloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), availableTestResults, new HashMap<Long, BloodTestResult>(),
        new ArrayList<BloodTestingRule>());
    bloodTestingRuleResultSet.setTtiStatus(TTIStatus.SAFE);
    List<Long> pendingTtiTestIds = new ArrayList<>();
    pendingTtiTestIds.add(2L);
    bloodTestingRuleResultSet.setPendingRepeatAndConfirmatoryTtiTestsIds(pendingTtiTestIds);
    BloodTestingRule rule1 = new BloodTestingRule();
    rule1.setBloodTest(bloodTest);
    rule1.setPendingTestsIds("2");
    List<BloodTestingRule> rules = new ArrayList<>();
    rules.add(rule1);
    bloodTestingRuleResultSet.setBloodTestingRules(rules);

    boolean canEdit = bloodTestResultConstraintChecker.canEdit(bloodTestingRuleResultSet, bloodTestResult, DONATION_NOT_RELEASED);

    assertThat(canEdit, is(true));
  }

  @Test
  public void testCanEditTTISafeWithOtherPendingBloodTest() {
    Donation donation = aDonation().build();
    BloodTest bloodTest = new BloodTest();
    bloodTest.setCategory(BloodTestCategory.TTI);
    bloodTest.setId(1l);
    BloodTestResult bloodTestResult = new BloodTestResult();
    bloodTestResult.setDonation(donation);
    bloodTestResult.setBloodTest(bloodTest);
    bloodTestResult.setResult("OUTCOME");
    Map<Long, String> availableTestResults = new HashMap<>();
    availableTestResults.put(1L, "RESULT");
    availableTestResults.put(2L, "RESULT");
    BloodTestingRuleResultSet bloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), availableTestResults, new HashMap<Long, BloodTestResult>(),
        generateTestBloodTestingRules());
    bloodTestingRuleResultSet.setTtiStatus(TTIStatus.SAFE);
    List<Long> pendingTtiTestIds = new ArrayList<>();
    pendingTtiTestIds.add(3L);
    bloodTestingRuleResultSet.setPendingRepeatAndConfirmatoryTtiTestsIds(pendingTtiTestIds);
    BloodTestingRule rule1 = new BloodTestingRule();
    rule1.setBloodTest(bloodTest);
    rule1.setPendingTestsIds("2");
    BloodTestingRule rule2 = new BloodTestingRule();
    rule2.setBloodTest(BloodTestBuilder.aBasicTTIBloodTest().withId(2L).build());
    rule2.setPendingTestsIds("3");
    List<BloodTestingRule> rules = new ArrayList<>();
    rules.add(rule1);
    rules.add(rule2);
    bloodTestingRuleResultSet.setBloodTestingRules(rules);

    boolean canEdit = bloodTestResultConstraintChecker.canEdit(bloodTestingRuleResultSet, bloodTestResult, DONATION_NOT_RELEASED);

    assertThat(canEdit, is(false));
  }

  @Test
  public void testCanEditTTISafeWithOtherRelatedPendingBloodTest() {
    Donation donation = aDonation().build();
    BloodTest bloodTest = new BloodTest();
    bloodTest.setCategory(BloodTestCategory.TTI);
    bloodTest.setId(1l);
    BloodTestResult bloodTestResult = new BloodTestResult();
    bloodTestResult.setDonation(donation);
    bloodTestResult.setBloodTest(bloodTest);
    bloodTestResult.setResult("OUTCOME");
    Map<Long, String> availableTestResults = new HashMap<>();
    availableTestResults.put(1L, "RESULT");
    availableTestResults.put(3L, "RESULT");
    BloodTestingRuleResultSet bloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), availableTestResults, new HashMap<Long, BloodTestResult>(),
        generateTestBloodTestingRules());
    bloodTestingRuleResultSet.setTtiStatus(TTIStatus.SAFE);
    List<Long> pendingTtiTestIds = new ArrayList<>();
    pendingTtiTestIds.add(2L);
    bloodTestingRuleResultSet.setPendingRepeatAndConfirmatoryTtiTestsIds(pendingTtiTestIds);
    BloodTestingRule rule1 = new BloodTestingRule();
    rule1.setBloodTest(bloodTest);
    rule1.setPendingTestsIds("2");
    BloodTestingRule rule2 = new BloodTestingRule();
    rule2.setBloodTest(BloodTestBuilder.aBasicTTIBloodTest().withId(2L).build());
    rule2.setPendingTestsIds("3");
    List<BloodTestingRule> rules = new ArrayList<>();
    rules.add(rule1);
    rules.add(rule2);
    bloodTestingRuleResultSet.setBloodTestingRules(rules);

    boolean canEdit = bloodTestResultConstraintChecker.canEdit(bloodTestingRuleResultSet, bloodTestResult, DONATION_NOT_RELEASED);

    assertThat(canEdit, is(false));
  }

  @Test
  public void testCanEditReleasedDonationTTITestResult() {
    Donation donation = aDonation().build();
    BloodTest bloodTest = new BloodTest();
    bloodTest.setCategory(BloodTestCategory.TTI);
    BloodTestResult bloodTestResult = new BloodTestResult();
    bloodTestResult.setDonation(donation);
    bloodTestResult.setBloodTest(bloodTest);
    BloodTestingRuleResultSet bloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), new HashMap<Long, BloodTestResult>(),
        new ArrayList<BloodTestingRule>());
    bloodTestingRuleResultSet.setBloodTypingMatchStatus(BloodTypingMatchStatus.MATCH);
    bloodTestingRuleResultSet.setBloodTypingStatus(BloodTypingStatus.COMPLETE);

    boolean canEdit = bloodTestResultConstraintChecker.canEdit(bloodTestingRuleResultSet, bloodTestResult, DONATION_RELEASED);

    assertThat(canEdit, is(false));
  }

  @Test
  public void testCanEditUnknownTestCategory() {
    Donation donation = aDonation().build();
    BloodTest bloodTest = new BloodTest();
    bloodTest.setCategory(null);
    bloodTest.setId(1l);
    BloodTestResult bloodTestResult = new BloodTestResult();
    bloodTestResult.setDonation(donation);
    bloodTestResult.setBloodTest(bloodTest);
    BloodTestingRuleResultSet bloodTestingRuleResultSet = new BloodTestingRuleResultSet(donation,
        new HashMap<Long, String>(), new HashMap<Long, String>(), new HashMap<Long, BloodTestResult>(),
        new ArrayList<BloodTestingRule>());

    boolean canEdit = bloodTestResultConstraintChecker.canEdit(bloodTestingRuleResultSet, bloodTestResult, DONATION_NOT_RELEASED);

    assertThat(canEdit, is(true));
  }

  private List<BloodTestingRule> generateTestBloodTestingRules() {
    BloodTestingRule rule1 = new BloodTestingRule();
    rule1.setBloodTest(BloodTestBuilder.aBasicTTIBloodTest().withId(1L).build());
    rule1.setPendingTestsIds("");
    BloodTestingRule rule2 = new BloodTestingRule();
    rule2.setBloodTest(BloodTestBuilder.aBasicTTIBloodTest().withId(2L).build());
    rule2.setPendingTestsIds("3");
    BloodTestingRule rule3 = new BloodTestingRule();
    rule3.setBloodTest(BloodTestBuilder.aBasicTTIBloodTest().withId(3L).build());
    rule3.setPendingTestsIds("4");
    BloodTestingRule rule4 = new BloodTestingRule();
    rule4.setBloodTest(BloodTestBuilder.aBasicTTIBloodTest().withId(4L).build());
    rule4.setPendingTestsIds("");
    List<BloodTestingRule> bloodTestingRules = new ArrayList<BloodTestingRule>();
    bloodTestingRules.add(rule1);
    bloodTestingRules.add(rule2);
    bloodTestingRules.add(rule3);
    bloodTestingRules.add(rule4);
    return bloodTestingRules;
  }
}

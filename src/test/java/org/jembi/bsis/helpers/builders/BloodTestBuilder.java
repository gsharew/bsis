package org.jembi.bsis.helpers.builders;

import org.jembi.bsis.model.bloodtesting.BloodTest;
import org.jembi.bsis.model.bloodtesting.BloodTestCategory;
import org.jembi.bsis.model.bloodtesting.BloodTestType;

public class BloodTestBuilder extends AbstractEntityBuilder<BloodTest> {

  private Long id;
  private BloodTestCategory bloodTestCategory;
  private BloodTestType bloodTestType;
  private String positiveResults;
  private String negativeResults;
  private boolean flagComponentsForDiscard;
  private boolean flagComponentsContainingPlasmaForDiscard;
  private String validResults;
  private Boolean isEmptyAllowed;
  private String testName;
  private String testNameShort;
  private Integer rankInCategory;
  private Boolean isDeleted = Boolean.FALSE;
  private Boolean isActive = Boolean.TRUE;

  public BloodTestBuilder withId(Long id) {
    this.id = id;
    return this;
  }

  public BloodTestBuilder withBloodTestType(BloodTestType bloodTestType) {
    this.bloodTestType = bloodTestType;
    return this;
  }

  public BloodTestBuilder withCategory(BloodTestCategory bloodTestCategory) {
    this.bloodTestCategory = bloodTestCategory;
    return this;
  }

  public BloodTestBuilder withPositiveResults(String positiveResults) {
    this.positiveResults = positiveResults;
    return this;
  }

  public BloodTestBuilder withNegativeResults(String negativeResults) {
    this.negativeResults = negativeResults;
    return this;
  }

  public BloodTestBuilder withFlagComponentsForDiscard(boolean flagComponentsForDiscard) {
    this.flagComponentsForDiscard = flagComponentsForDiscard;
    return this;
  }
  
  public BloodTestBuilder thatShouldFlagComponentsContainingPlasmaForDiscard(){
    this.flagComponentsContainingPlasmaForDiscard = true;
    return this;
  }
  
  public BloodTestBuilder thatShouldNotFlagComponentsContainingPlasmaForDiscard(){
    this.flagComponentsContainingPlasmaForDiscard = false;
    return this;
  }
  
  public BloodTestBuilder withValidResults(String validResults) {
    this.validResults = validResults;
    return this;
  }

  public BloodTestBuilder withIsEmptyAllowed(Boolean isEmptyAllowed) {
    this.isEmptyAllowed = isEmptyAllowed;
    return this;
  }

  public BloodTestBuilder withTestName(String testName) {
    this.testName = testName;
    return this;
  }

  public BloodTestBuilder withTestNameShort(String testNameShort) {
    this.testNameShort = testNameShort;
    return this;
  }

  public BloodTestBuilder withRankInCategory(Integer rankInCategory) {
    this.rankInCategory = rankInCategory;
    return this;
  }

  public BloodTestBuilder thatIsDeleted() {
    this.isDeleted = Boolean.TRUE;
    return this;
  }

  public BloodTestBuilder thatIsInActive() {
    this.isActive = Boolean.FALSE;
    return this;
  }

  @Override
  public BloodTest build() {
    BloodTest bloodTest = new BloodTest();
    bloodTest.setId(id);
    bloodTest.setCategory(bloodTestCategory);
    bloodTest.setBloodTestType(bloodTestType);
    bloodTest.setPositiveResults(positiveResults);
    bloodTest.setNegativeResults(negativeResults);
    bloodTest.setFlagComponentsForDiscard(flagComponentsForDiscard);
    bloodTest.setValidResults(validResults);
    bloodTest.setFlagComponentsContainingPlasmaForDiscard(flagComponentsContainingPlasmaForDiscard);
    bloodTest.setIsEmptyAllowed(isEmptyAllowed);
    bloodTest.setTestName(testName);
    bloodTest.setTestNameShort(testNameShort);
    bloodTest.setRankInCategory(rankInCategory);
    bloodTest.setIsActive(isActive);
    bloodTest.setIsDeleted(isDeleted);
    return bloodTest;
  }

  public static BloodTestBuilder aBloodTest() {
    return new BloodTestBuilder();
  }

  public static BloodTestBuilder aBasicTTIBloodTest() {
    return new BloodTestBuilder().withCategory(BloodTestCategory.TTI).withBloodTestType(BloodTestType.BASIC_TTI);
  }

  public static BloodTestBuilder aRepeatTTIBloodTest() {
    return new BloodTestBuilder().withCategory(BloodTestCategory.TTI).withBloodTestType(BloodTestType.REPEAT_TTI);
  }

  public static BloodTestBuilder aConfirmatoryTTIBloodTest() {
    return new BloodTestBuilder().withCategory(BloodTestCategory.TTI).withBloodTestType(BloodTestType.CONFIRMATORY_TTI);
  }

  public static BloodTestBuilder aBasicBloodTypingBloodTest() {
    return new BloodTestBuilder()
      .withCategory(BloodTestCategory.BLOODTYPING)
      .withBloodTestType(BloodTestType.BASIC_BLOODTYPING);
  }

  public static BloodTestBuilder aRepeatBloodTypingBloodTestViewModel() {
    return new BloodTestBuilder()
      .withCategory(BloodTestCategory.BLOODTYPING)
      .withBloodTestType(BloodTestType.REPEAT_BLOODTYPING);
  }
}
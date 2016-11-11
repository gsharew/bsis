package org.jembi.bsis.backingform.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jembi.bsis.helpers.builders.BloodTestBackingFormBuilder.aBloodTestBackingForm;
import static org.jembi.bsis.helpers.builders.BloodTestBuilder.aBasicTTIBloodTest;
import static org.jembi.bsis.helpers.builders.BloodTestingRuleBackingFormBuilder.aBloodTestingRuleBackingForm;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;

import org.jembi.bsis.backingform.BloodTestBackingForm;
import org.jembi.bsis.backingform.BloodTestingRuleBackingForm;
import org.jembi.bsis.model.bloodtesting.BloodTest;
import org.jembi.bsis.model.bloodtesting.BloodTestCategory;
import org.jembi.bsis.model.bloodtesting.rules.DonationField;
import org.jembi.bsis.model.donation.TTIStatus;
import org.jembi.bsis.repository.BloodTestRepository;
import org.jembi.bsis.suites.UnitTestSuite;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

public class BloodTestingRuleBackingFormValidatorTests extends UnitTestSuite {

  @InjectMocks
  private BloodTestingRuleBackingFormValidator bloodTestingRuleBackingFormvalidator;
  
  @Mock
  private BloodTestRepository bloodTestRepository;
  
  private BloodTest getBaseBloodTest() {
    return aBasicTTIBloodTest().withId(1L).withValidResults("POS").build();
  }

  private BloodTestingRuleBackingForm getBaseBloodTestingRuleBackingForm() {
    BloodTestBackingForm bloodTestBackingForm = aBloodTestBackingForm()
        .withId(1L)
        .withCategory(BloodTestCategory.TTI)
        .withValidResults(new LinkedHashSet<>(Arrays.asList("POS", "NEG")))
        .build();
    BloodTestingRuleBackingForm backingForm = aBloodTestingRuleBackingForm()
        .withBloodTest(bloodTestBackingForm)
        .withDonationFieldChanged(DonationField.TTISTATUS)
        .withNewInformation(TTIStatus.TTI_UNSAFE.name())
        .withPattern("POS")
        .withPendingTestsIds(new LinkedHashSet<>(Arrays.asList("2")))
        .build();
    return backingForm;
  }

  @Test
  public void testValidateValidForm_shouldHaveNoErrors() {

    // Set up data
    BloodTestingRuleBackingForm backingForm = getBaseBloodTestingRuleBackingForm();
    
    // Set up mocks
    when(bloodTestRepository.findBloodTestById(1L)).thenReturn(getBaseBloodTest());
    when(bloodTestRepository.verifyBloodTestExists(2L)).thenReturn(true);

    // Run test
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "BloodTestingRuleForm");
    bloodTestingRuleBackingFormvalidator.validateForm(backingForm, errors);

    // Verify
    assertThat(errors.getErrorCount(), is(0));
  }
  
  @Test
  public void testValidateFormWithNoBloodTest_shouldHaveOneError() {

    // Set up data
    BloodTestingRuleBackingForm backingForm = getBaseBloodTestingRuleBackingForm();
    backingForm.setBloodTest(null);

    // Set up mocks
    when(bloodTestRepository.findBloodTestById(1L)).thenReturn(getBaseBloodTest());
    when(bloodTestRepository.verifyBloodTestExists(2L)).thenReturn(true);

    // Run test
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "BloodTestingRuleForm");
    bloodTestingRuleBackingFormvalidator.validateForm(backingForm, errors);

    // Verify
    assertThat(errors.getErrorCount(), is(1));
    assertThat(errors.getFieldError("bloodTest").getCode(), is("errors.required"));
  }
  
  @Test
  public void testValidateFormWithBloodTestWithNoId_shouldHaveOneError() {

    // Set up data
    BloodTestingRuleBackingForm backingForm = getBaseBloodTestingRuleBackingForm();
    backingForm.setBloodTest(aBloodTestBackingForm().build());

    // Set up mocks
    when(bloodTestRepository.findBloodTestById(1L)).thenReturn(getBaseBloodTest());
    when(bloodTestRepository.verifyBloodTestExists(2L)).thenReturn(true);

    // Run test
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "BloodTestingRuleForm");
    bloodTestingRuleBackingFormvalidator.validateForm(backingForm, errors);

    // Verify
    assertThat(errors.getErrorCount(), is(1));
    assertThat(errors.getFieldError("bloodTest").getCode(), is("errors.required"));
  }

  @Test
  public void testValidateFormWithInvalidBloodTest_shouldHaveOneError() {

    // Set up data
    BloodTestingRuleBackingForm backingForm = getBaseBloodTestingRuleBackingForm();

    // Set up mocks
    when(bloodTestRepository.findBloodTestById(1L)).thenReturn(null);
    when(bloodTestRepository.verifyBloodTestExists(2L)).thenReturn(true);

    // Run test
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "BloodTestingRuleForm");
    bloodTestingRuleBackingFormvalidator.validateForm(backingForm, errors);

    // Verify
    assertThat(errors.getErrorCount(), is(1));
    assertThat(errors.getFieldError("bloodTest").getCode(), is("errors.invalid"));
  }

  @Test
  public void testValidateFormWithNoPattern_shouldHaveOneError() {

    // Set up data
    BloodTestingRuleBackingForm backingForm = getBaseBloodTestingRuleBackingForm();
    backingForm.setPattern(null);

    // Set up mocks
    when(bloodTestRepository.findBloodTestById(1L)).thenReturn(getBaseBloodTest());
    when(bloodTestRepository.verifyBloodTestExists(2L)).thenReturn(true);

    // Run test
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "BloodTestingRuleForm");
    bloodTestingRuleBackingFormvalidator.validateForm(backingForm, errors);

    // Verify
    assertThat(errors.getErrorCount(), is(1));
    assertThat(errors.getFieldError("pattern").getCode(), is("errors.required"));
  }

  @Test
  public void testValidateFormWithInvalidLengthPattern_shouldHaveOneError() {

    // Set up data
    BloodTestingRuleBackingForm backingForm = getBaseBloodTestingRuleBackingForm();
    backingForm.setPattern("longPattern longPattern longPattern longPattern longPattern");

    // Set up mocks
    when(bloodTestRepository.findBloodTestById(1L)).thenReturn(getBaseBloodTest());
    when(bloodTestRepository.verifyBloodTestExists(2L)).thenReturn(true);

    // Run test
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "BloodTestingRuleForm");
    bloodTestingRuleBackingFormvalidator.validateForm(backingForm, errors);

    // Verify
    assertThat(errors.getErrorCount(), is(1));
    assertThat(errors.getFieldError("pattern").getCode(), is("errors.fieldLength"));
  }

  @Test
  public void testValidateFormWithInvalidResultPattern_shouldHaveOneError() {

    // Set up data
    BloodTestingRuleBackingForm backingForm = getBaseBloodTestingRuleBackingForm();
    // pattern is not in blood test validResultsList
    backingForm.setPattern("XX");

    // Set up mocks
    when(bloodTestRepository.findBloodTestById(1L)).thenReturn(getBaseBloodTest());
    when(bloodTestRepository.verifyBloodTestExists(2L)).thenReturn(true);

    // Run test
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "BloodTestingRuleForm");
    bloodTestingRuleBackingFormvalidator.validateForm(backingForm, errors);

    // Verify
    assertThat(errors.getErrorCount(), is(1));
    assertThat(errors.getFieldError("pattern").getCode(), is("errors.invalid"));
  }

  @Test
  public void testValidateFormWithNoDonationField_shouldHaveOneError() {

    // Set up data
    BloodTestingRuleBackingForm backingForm = getBaseBloodTestingRuleBackingForm();
    backingForm.setDonationFieldChanged(null);

    // Set up mocks
    when(bloodTestRepository.findBloodTestById(1L)).thenReturn(getBaseBloodTest());
    when(bloodTestRepository.verifyBloodTestExists(2L)).thenReturn(true);

    // Run test
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "BloodTestingRuleForm");
    bloodTestingRuleBackingFormvalidator.validateForm(backingForm, errors);

    // Verify
    assertThat(errors.getErrorCount(), is(1));
    assertThat(errors.getFieldError("donationFieldChanged").getCode(), is("errors.required"));
  }

  @Test
  public void testValidateFormWithInvalidDonationField_shouldHaveOneError() {

    // Set up data
    BloodTestingRuleBackingForm backingForm = getBaseBloodTestingRuleBackingForm();
    // donationFieldChanged doesn't match category TTI
    backingForm.setDonationFieldChanged(DonationField.BLOODABO);

    // Set up mocks
    when(bloodTestRepository.findBloodTestById(1L)).thenReturn(getBaseBloodTest());
    when(bloodTestRepository.verifyBloodTestExists(2L)).thenReturn(true);

    // Run test
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "BloodTestingRuleForm");
    bloodTestingRuleBackingFormvalidator.validateForm(backingForm, errors);

    // Verify
    assertThat(errors.getErrorCount(), is(1));
    assertThat(errors.getFieldError("donationFieldChanged").getCode(), is("errors.invalid"));
  }

  @Test
  public void testValidateFormWithNoNewInformation_shouldHaveOneError() {

    // Set up data
    BloodTestingRuleBackingForm backingForm = getBaseBloodTestingRuleBackingForm();
    backingForm.setNewInformation(null);

    // Set up mocks
    when(bloodTestRepository.findBloodTestById(1L)).thenReturn(getBaseBloodTest());
    when(bloodTestRepository.verifyBloodTestExists(2L)).thenReturn(true);

    // Run test
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "BloodTestingRuleForm");
    bloodTestingRuleBackingFormvalidator.validateForm(backingForm, errors);

    // Verify
    assertThat(errors.getErrorCount(), is(1));
    assertThat(errors.getFieldError("newInformation").getCode(), is("errors.required"));
  }

  @Test
  public void testValidateFormWithInvalidLengthNewInformation_shouldHaveOneError() {

    // Set up data
    BloodTestingRuleBackingForm backingForm = getBaseBloodTestingRuleBackingForm();
    backingForm.setNewInformation("longNewInfo longNewInfo longNewInfo");

    // Set up mocks
    when(bloodTestRepository.findBloodTestById(1L)).thenReturn(getBaseBloodTest());
    when(bloodTestRepository.verifyBloodTestExists(2L)).thenReturn(true);

    // Run test
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "BloodTestingRuleForm");
    bloodTestingRuleBackingFormvalidator.validateForm(backingForm, errors);

    // Verify
    assertThat(errors.getErrorCount(), is(1));
    assertThat(errors.getFieldError("newInformation").getCode(), is("errors.fieldLength"));
  }

  @Test
  public void testValidateFormWithInvalidNewInformation_shouldHaveOneError() {

    // Set up data
    BloodTestingRuleBackingForm backingForm = getBaseBloodTestingRuleBackingForm();
    // newInformation doesn't match donationFieldChanged (TTISTATUS)
    backingForm.setNewInformation("AB");

    // Set up mocks
    when(bloodTestRepository.findBloodTestById(1L)).thenReturn(getBaseBloodTest());
    when(bloodTestRepository.verifyBloodTestExists(2L)).thenReturn(true);

    // Run test
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "BloodTestingRuleForm");
    bloodTestingRuleBackingFormvalidator.validateForm(backingForm, errors);

    // Verify
    assertThat(errors.getErrorCount(), is(1));
    assertThat(errors.getFieldError("newInformation").getCode(), is("errors.invalid"));
  }

  @Test
  public void testValidateFormWithNoPendingTestsIds_shouldHaveOneError() {

    // Set up data
    BloodTestingRuleBackingForm backingForm = getBaseBloodTestingRuleBackingForm();
    backingForm.setPendingTestsIds(null);

    // Set up mocks
    when(bloodTestRepository.findBloodTestById(1L)).thenReturn(getBaseBloodTest());
    when(bloodTestRepository.verifyBloodTestExists(2L)).thenReturn(true);

    // Run test
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "BloodTestingRuleForm");
    bloodTestingRuleBackingFormvalidator.validateForm(backingForm, errors);

    // Verify
    assertThat(errors.getErrorCount(), is(1));
    assertThat(errors.getFieldError("pendingTestsIds").getCode(), is("errors.required"));
  }

  @Test
  public void testValidateFormWithInvalidPendingTestsIds_shouldHaveOneError() {

    // Set up data
    BloodTestingRuleBackingForm backingForm = getBaseBloodTestingRuleBackingForm();
    backingForm.setPendingTestsIds(new LinkedHashSet<>(Arrays.asList("2A", "xx")));

    // Set up mocks
    when(bloodTestRepository.findBloodTestById(1L)).thenReturn(getBaseBloodTest());
    when(bloodTestRepository.verifyBloodTestExists(2L)).thenReturn(false);

    // Run test
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "BloodTestingRuleForm");
    bloodTestingRuleBackingFormvalidator.validateForm(backingForm, errors);

    // Verify
    assertThat(errors.getErrorCount(), is(1));
    assertThat(errors.getFieldError("pendingTestsIds").getCode(), is("errors.invalid"));
  }

  @Test
  public void testValidateFormWithNonExistentPendingTestsIdsId_shouldHaveOneError() {

    // Set up data
    BloodTestingRuleBackingForm backingForm = getBaseBloodTestingRuleBackingForm();

    // Set up mocks
    when(bloodTestRepository.findBloodTestById(1L)).thenReturn(getBaseBloodTest());
    when(bloodTestRepository.verifyBloodTestExists(2L)).thenReturn(false);

    // Run test
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "BloodTestingRuleForm");
    bloodTestingRuleBackingFormvalidator.validateForm(backingForm, errors);

    // Verify
    assertThat(errors.getErrorCount(), is(1));
    assertThat(errors.getFieldError("pendingTestsIds").getCode(), is("errors.invalid"));
  }

  @Test
  public void testValidateFormWithNoIsDeleted_shouldHaveOneError() {

    // Set up data
    BloodTestingRuleBackingForm backingForm = getBaseBloodTestingRuleBackingForm();
    backingForm.setIsDeleted(null);

    // Set up mocks
    when(bloodTestRepository.findBloodTestById(1L)).thenReturn(getBaseBloodTest());
    when(bloodTestRepository.verifyBloodTestExists(2L)).thenReturn(true);

    // Run test
    Errors errors = new MapBindingResult(new HashMap<String, String>(), "BloodTestingRuleForm");
    bloodTestingRuleBackingFormvalidator.validateForm(backingForm, errors);

    // Verify
    assertThat(errors.getErrorCount(), is(1));
    assertThat(errors.getFieldError("isDeleted").getCode(), is("errors.required"));
  }

}

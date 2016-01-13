package backingform.validator;

import java.text.ParseException;
import java.util.Date;

import model.donor.Donor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import repository.DonorRepository;
import repository.SequenceNumberRepository;
import utils.CustomDateFormatter;
import backingform.DonorBackingForm;

@Component
public class DonorBackingFormValidator extends BaseValidator<DonorBackingForm> {
  
  @Autowired
  private DonorRepository donorRepository;
  
  @Autowired
  private SequenceNumberRepository sequenceNumberRepository;

  @Override
  public void validateForm(DonorBackingForm form, Errors errors) throws Exception {
    
    updateAutogeneratedFields(form);

    if (isDuplicateDonorNumber(form.getDonor())){
      errors.rejectValue("donor.donorNumber", "donorNumber.nonunique",
          "There exists a donor with the same donor number.");
    }

    validateBirthDate(form, errors);    
    validateBloodGroup(form, errors);

    commonFieldChecks(form, "donor", errors);
	  
  }

  private void updateAutogeneratedFields(DonorBackingForm form) {
    if (StringUtils.isBlank(form.getDonorNumber()) && isFieldAutoGenerated("donor", "donorNumber")) {
       form.setDonorNumber(sequenceNumberRepository.getSequenceNumber("Donor","donorNumber"));
    }
  }

  private boolean validateBirthDate(DonorBackingForm form, Errors errors) {

  String birthDate = form.getBirthDate();
  
    Boolean isAgeFormatCorrect = form.isAgeFormatCorrect();
    if (isAgeFormatCorrect != null && !isAgeFormatCorrect) {
      errors.rejectValue("age", "ageFormat.incorrect", "Age should be number of years");
      return false;
    }    
    
    try{

    	// if valid date
    	if (CustomDateFormatter.isDateStringValid(birthDate) && birthDate != null && !birthDate.isEmpty()){
    		
    	  Date date = CustomDateFormatter.getDateFromString(birthDate);
    	  
		  // verify Birthdate is not in the future
		  if(isFutureDate(date)){
			  errors.rejectValue("donor.birthDate", "date.futureDate", "Cannot be a future date");
		  }
    	}
	  
    }
    // If Date String is not valid, reject value
    catch(ParseException ex){
    	errors.rejectValue("donor.birthDate", "dateFormat.incorrect",
    	CustomDateFormatter.getDateErrorMessage());
    	return false;
    }
  
    return true;
  }
 

  
  private void validateBloodGroup(DonorBackingForm form, Errors errors) {
	  String bloodAbo = form.getBloodAbo();
	  String bloodRh = form.getBloodRh();
	  
	  if(bloodAbo.isEmpty() && !bloodRh.isEmpty()){
		  errors.rejectValue("donor.bloodAbo", "bloodGroup.incomplete", "Both ABO and Rh values are required");
	  }
	  if(!bloodAbo.isEmpty() && bloodRh.isEmpty()){
		  errors.rejectValue("donor.bloodRh", "bloodGroup.incomplete", "Both ABO and Rh values are required");
	  }
	  
  }

  private boolean isDuplicateDonorNumber(Donor donor) {
    String donorNumber = donor.getDonorNumber();
    if (StringUtils.isBlank(donorNumber)) {
      return false;
    }

    Donor existingDonor = donorRepository.findDonorByDonorNumber(donorNumber,true);
    if (existingDonor != null && !existingDonor.getId().equals(donor.getId())) {
      return true;
    }

    return false;
  }
}

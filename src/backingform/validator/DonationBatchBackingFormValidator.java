package backingform.validator;

import java.util.ArrayList;
import java.util.List;

import model.donationbatch.DonationBatch;
import model.location.Location;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import repository.DonationBatchRepository;
import repository.SequenceNumberRepository;
import backingform.DonationBatchBackingForm;

@Component
public class DonationBatchBackingFormValidator extends BaseValidator<DonationBatchBackingForm> {
  
  @Autowired
  private DonationBatchRepository donationBatchRepository;
  
  @Autowired
  private SequenceNumberRepository sequenceNumberRepository;

  @Override
  public void validateForm(DonationBatchBackingForm form, Errors errors) throws Exception {
    updateAutoGeneratedFields(form);

    if ( form.getId() != null && isDuplicateDonationBatchNumber(form.getDonationBatch()))
      errors.rejectValue("donationBatch.batchNumber", "batchNumber.nonunique",
          "There exists a donation batch with the same batch number.");

    Location venue = form.getDonationBatch().getVenue();
	if (venue == null || venue.getId() == null) {
	  errors.rejectValue("donationBatch.venue", "venue.empty",
	    "Venue is required.");
	} 
	else {
      ArrayList<Long> venueIds = new ArrayList<>();
      venueIds.add(venue.getId());
	  if (venue.getIsVenue() == false) {
	  errors.rejectValue("donationBatch.venue", "venue.invalid",
		"Location is not a Venue.");
	  }
      else if (form.getId() == null && findOpenDonationBatches(venueIds).size() > 0) {
        errors.rejectValue("donationBatch.venue", "venue.openBatch",
                "There is already an open donation batch for that venue.");
      }
	}

    commonFieldChecks(form, "donationBatch", errors);
  }

  private void updateAutoGeneratedFields(DonationBatchBackingForm form) {
    if (StringUtils.isBlank(form.getBatchNumber()) && isFieldAutoGenerated("donationBatch", "batchNumber")) {
      form.setBatchNumber(sequenceNumberRepository.getNextBatchNumber());
    }
  }

  private boolean isDuplicateDonationBatchNumber(DonationBatch donationBatch) {
    String batchNumber = donationBatch.getBatchNumber();
    if (StringUtils.isBlank(batchNumber)) {
      return false;
    }

    DonationBatch existingDonationBatch = donationBatchRepository.findDonationBatchByBatchNumberIncludeDeleted(batchNumber);
    if (existingDonationBatch != null && !existingDonationBatch.getId().equals(donationBatch.getId())) {
      return true;
    }

    return false;
  }

  private List<DonationBatch> findOpenDonationBatches(List<Long> venueIds) {
    return donationBatchRepository.findDonationBatches(false, venueIds, null, null);
  }
}

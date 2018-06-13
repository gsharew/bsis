package org.jembi.bsis.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.jembi.bsis.model.donation.Donation;
import org.jembi.bsis.model.testbatch.TestBatch;
import org.jembi.bsis.model.testbatch.TestBatchStatus;
import org.jembi.bsis.repository.SequenceNumberRepository;
import org.jembi.bsis.repository.TestBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TestBatchCRUDService {

  private static final Logger LOGGER = Logger.getLogger(TestBatchCRUDService.class);

  @Autowired
  private TestBatchRepository testBatchRepository;
  @Autowired
  private TestBatchConstraintChecker testBatchConstraintChecker;
  @Autowired
  private TestBatchStatusChangeService testBatchStatusChangeService;
  @Autowired
  private SequenceNumberRepository sequenceNumberRepository;

  public TestBatch createTestBatch(TestBatch testBatch) {
    testBatch.setBatchNumber(sequenceNumberRepository.getNextTestBatchNumber());
    testBatch.setStatus(TestBatchStatus.OPEN);
    testBatch.setIsDeleted(Boolean.FALSE);
    testBatchRepository.save(testBatch);
    return testBatch;
  }

  public TestBatch updateTestBatch(TestBatch updatedTestBatch) {

    TestBatch existingTestBatch = testBatchRepository.findTestBatchById(updatedTestBatch.getId());

    if (existingTestBatch.getStatus() != TestBatchStatus.CLOSED && !testBatchConstraintChecker.canEditTestBatch(existingTestBatch)) {
      throw new IllegalStateException("Test batch cannot be updated");
    }

    if (updatedTestBatch.getTestBatchDate() != null) {
      existingTestBatch.setTestBatchDate(updatedTestBatch.getTestBatchDate());
    }

    existingTestBatch.setLocation(updatedTestBatch.getLocation());

    if (updatedTestBatch.getStatus() != null) {
      existingTestBatch = changeTestBatchStatus(existingTestBatch, updatedTestBatch.getStatus());
    }

    return testBatchRepository.update(existingTestBatch);
  }

  public void deleteTestBatch(UUID testBatchId) {
    TestBatch testBatch = testBatchRepository.findTestBatchById(testBatchId);
    if (!testBatchConstraintChecker.canDeleteTestBatch(testBatch)) {
      throw new IllegalStateException("Test batch cannot be deleted");
    }
    testBatchRepository.deleteTestBatch(testBatchId);
  }

  protected TestBatch changeTestBatchStatus(TestBatch testBatch, TestBatchStatus newStatus) {
    LOGGER.info("Updating status of test batch " + testBatch.getId() + " to " + newStatus);

    TestBatchStatus oldStatus = testBatch.getStatus();
    if (newStatus == testBatch.getStatus()) {
      // The status is not being changed so return early
      return testBatch;
    }

    if (oldStatus == TestBatchStatus.OPEN && newStatus == TestBatchStatus.RELEASED
        && !testBatchConstraintChecker.canReleaseTestBatch(testBatch).canRelease()) {
      throw new IllegalStateException("Test batch cannot be released");
    }

    if (newStatus == TestBatchStatus.CLOSED && !testBatchConstraintChecker.canCloseTestBatch(testBatch)) {
      throw new IllegalStateException("Only released test batches can be closed");
    }

    if (newStatus == TestBatchStatus.OPEN && !testBatchConstraintChecker.canReopenTestBatch(testBatch)) {
      throw new IllegalStateException("Only closed test batches can be reopened");
    }

    // Set the new status
    testBatch.setStatus(newStatus);

    testBatch = testBatchRepository.update(testBatch);

    if (oldStatus == TestBatchStatus.OPEN && newStatus == TestBatchStatus.RELEASED) {
      testBatchStatusChangeService.handleRelease(testBatch);
    }

    return testBatch;
  }
  
  public TestBatch addDonationsToTestBatch(UUID testBatchId, List<Donation> donations) {
    TestBatch testBatch = testBatchRepository.findTestBatchById(testBatchId);

    if (!testBatchConstraintChecker.canAddOrRemoveDonation(testBatch)) {
      throw new IllegalStateException("Donations can only be added to open test batches");
    }

    Set<Donation> donationsToAdd = new HashSet<>();
    for (Donation donation : donations) {
      if (!donation.getPackType().getTestSampleProduced()) {
        LOGGER.debug("Cannot add DIN '" + donation.getDonationIdentificationNumber()
            + "' to a TestBatch because it does not produce test samples. It will be ignored.");
        continue;
      }
      if (donation.getTestBatch() != null && !donation.getTestBatch().getId().equals(testBatchId)) {
        LOGGER.debug("Cannot add DIN '" + donation.getDonationIdentificationNumber()
            + "' to a TestBatch because it has been assigned to another TestBatch. It will be ignored.");
        continue;
      }
      // donation can be added
      donationsToAdd.add(donation);
    }

    // At least one donation must be successfully added to the testBatch
    if (donationsToAdd.size() == 0) {
      throw new IllegalArgumentException("None of these donations can be added to this testBatch.");
    }

    testBatch.getDonations().addAll(donationsToAdd);

    for (Donation donation : donationsToAdd) {
      donation.setTestBatch(testBatch);
    }
    
    testBatchRepository.save(testBatch);
    return testBatch;
  }
}

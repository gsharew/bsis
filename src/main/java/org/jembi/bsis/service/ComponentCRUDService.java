package org.jembi.bsis.service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.jembi.bsis.constant.GeneralConfigConstants;
import org.jembi.bsis.model.component.Component;
import org.jembi.bsis.model.component.ComponentStatus;
import org.jembi.bsis.model.componentbatch.ComponentBatch;
import org.jembi.bsis.model.componentmovement.ComponentStatusChange;
import org.jembi.bsis.model.componentmovement.ComponentStatusChangeReason;
import org.jembi.bsis.model.componentmovement.ComponentStatusChangeReasonCategory;
import org.jembi.bsis.model.componentmovement.ComponentStatusChangeReasonType;
import org.jembi.bsis.model.componenttype.ComponentType;
import org.jembi.bsis.model.componenttype.ComponentTypeCombination;
import org.jembi.bsis.model.componenttype.ComponentTypeTimeUnits;
import org.jembi.bsis.model.donation.Donation;
import org.jembi.bsis.model.donor.Donor;
import org.jembi.bsis.model.inventory.InventoryStatus;
import org.jembi.bsis.model.location.Location;
import org.jembi.bsis.model.packtype.PackType;
import org.jembi.bsis.repository.ComponentRepository;
import org.jembi.bsis.repository.ComponentStatusChangeReasonRepository;
import org.jembi.bsis.repository.ComponentTypeCombinationRepository;
import org.jembi.bsis.repository.ComponentTypeRepository;
import org.jembi.bsis.repository.DonationBatchRepository;
import org.jembi.bsis.repository.DonationRepository;
import org.jembi.bsis.utils.SecurityUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class ComponentCRUDService {

  private static final Logger LOGGER = Logger.getLogger(ComponentCRUDService.class);

  @Autowired
  private ComponentRepository componentRepository;

  @Autowired
  private ComponentTypeRepository componentTypeRepository;
  
  @Autowired
  private ComponentStatusCalculator componentStatusCalculator;
  
  @Autowired
  private ComponentConstraintChecker componentConstraintChecker;

  @Autowired
  private DateGeneratorService dateGeneratorService;

  @Autowired
  private ComponentStatusChangeReasonRepository componentStatusChangeReasonRepository;

  @Autowired
  private GeneralConfigAccessorService generalConfigAccessorService;

  @Autowired
  private ComponentTypeCombinationRepository componentTypeCombinationRepository;
  
  @Autowired
  private DonationBatchRepository donationBatchRepository;
  
  @Autowired
  private DonationRepository donationRepository;

  @Autowired
  private BleedTimeService bleedTimeService;
  
  private Long timeSinceDonation;

  public Component createInitialComponent(Donation donation) {

    // Create initial component only if the countAsDonation is true and the config option is enabled
    if (!donation.getPackType().getCountAsDonation() ||
        !generalConfigAccessorService.getBooleanValue(GeneralConfigConstants.CREATE_INITIAL_COMPONENTS)) {
      return null;
    }

    ComponentType componentType = donation.getPackType().getComponentType();

    Component component = new Component();
    component.setIsDeleted(false);
    component.setComponentType(componentType);
    component.setComponentCode(componentType.getComponentTypeCode());
    component.setDonation(donation);
    component.setStatus(ComponentStatus.QUARANTINED);

    // set new component creation date to match donation date and bleedStartTime if specified
    component.setCreatedOn(calculateCreatedOn(donation.getDonationDate(), donation.getBleedStartTime()));
    component.setCreatedDate(donation.getCreatedDate());
    component.setCreatedBy(donation.getCreatedBy());

    // set new component expiresOn date to be 'expiresAfter' days after the createdOn date
    component.setExpiresOn(calculateExpiresOn(component.getCreatedOn(), componentType.getExpiresAfter(), componentType.getExpiresAfterUnits()));

    // set the component venue and component batch (if already created for the donation batch)
    ComponentBatch componentBatch = donationBatchRepository.findComponentBatchByDonationbatchId(
        donation.getDonationBatch().getId());
        
    if (componentBatch == null) {
      // Set the location to the venue of the donation batch
      component.setLocation(donation.getDonationBatch().getVenue());
    } else {
      // Assign the component to the component batch
      component.setComponentBatch(componentBatch);
      // Set the location to the processing site of the component batch
      component.setLocation(componentBatch.getLocation());
    }

    componentRepository.save(component);
    return component;
  }

  /**
   * Change the specified Component's PackType and update. Note: this will change the Component's
   * type, the Component's code and the expiresOn date
   * 
   * @param newPackType PackType Donation's updated PackType
   * @param component Component to update
   * @return Component that has been updated
   */
  public Component updateComponentWithNewPackType(Component component, PackType newPackType) {
    ComponentType newComponentType = newPackType.getComponentType();

    component.setComponentType(newComponentType);
    component.setComponentCode(newComponentType.getComponentTypeCode());
    component.setExpiresOn(calculateExpiresOn(component.getCreatedOn(), newComponentType.getExpiresAfter(), newComponentType.getExpiresAfterUnits()));

    componentRepository.update(component);
    return component;
  }

  private Date calculateCreatedOn(Date donationDate, Date bleedStartTime) {
    Date createdOn = donationDate; // default to donationDate
    if (bleedStartTime != null) {
      createdOn = dateGeneratorService.generateDateTime(donationDate, bleedStartTime);
    }
    return createdOn;
  }

  private Date calculateExpiresOn(Date createdOn, Integer expiresAfter, ComponentTypeTimeUnits expiresAfterUnits) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(createdOn);

    // set component expiry date
    if (expiresAfterUnits == ComponentTypeTimeUnits.DAYS)
      cal.add(Calendar.DAY_OF_YEAR, expiresAfter);
    else if (expiresAfterUnits == ComponentTypeTimeUnits.HOURS)
      cal.add(Calendar.HOUR, expiresAfter);
    else if (expiresAfterUnits== ComponentTypeTimeUnits.YEARS)
      cal.add(Calendar.YEAR, expiresAfter);
    return cal.getTime();
  }

  /**
   * Change the status of components belonging to the donor from AVAILABLE to UNSAFE.
   */
  public void markComponentsBelongingToDonorAsUnsafe(Donor donor) {

    LOGGER.info("Marking components as unsafe for donor: " + donor);

    for (Donation donation : donor.getDonations()) {
      
      if (donation.getIsDeleted()) {
        // Skip deleted donations
        continue;
      }
      
      markComponentsBelongingToDonationAsUnsafe(donation);
    }
  }

  /**
   * Change the status of components linked to the donation from AVAILABLE to UNSAFE.
   */
  public void markComponentsBelongingToDonationAsUnsafe(Donation donation) {

    LOGGER.info("Marking components as unsafe for donation: " + donation);
    
    for (Component component : donation.getComponents()) {

      if (component.getIsDeleted()) {
        // Skip deleted components
        continue;
      }

      markComponentAsUnsafe(component, ComponentStatusChangeReasonType.TEST_RESULTS);
    }
  }

  public Component deleteComponent(long componentId) {
    LOGGER.info("Deleting component " + componentId);

    Component existingComponent = componentRepository.findComponentById(componentId);
    existingComponent.setIsDeleted(true);
    return componentRepository.update(existingComponent);
  }

  public void updateComponentStatusesForDonation(Donation donation) {

    LOGGER.info("Updating component statuses for donation: " + donation);

    for (Component component : donation.getComponents()) {

      if (!component.getIsDeleted() && componentStatusCalculator.updateComponentStatus(component)) {
        componentRepository.update(component);
      }
    }
  }

  public Component processComponent(long parentComponentId, long componentTypeCombinationId, Date processedOn) {

    Component parentComponent = componentRepository.findComponentById(parentComponentId);
    parentComponent.setProcessedOn(processedOn);
    ComponentTypeCombination componentTypeCombination =
        componentTypeCombinationRepository.findComponentTypeCombinationById(componentTypeCombinationId);
    
    if (!componentConstraintChecker.canProcess(parentComponent)) {
      throw new IllegalStateException("Component " + parentComponentId + " cannot be processed.");
    }

    ComponentStatus parentStatus = parentComponent.getStatus();

    // map of new components, storing component type and num. of units
    Map<ComponentType, Integer> newComponents = new HashMap<ComponentType, Integer>();

    // iterate over components in combination, adding them to the new components map, along with the
    // num. of units of each component
    for (ComponentType pt : componentTypeCombination.getComponentTypes()) {
      boolean check = false;
      ComponentType componentType = componentTypeRepository.getComponentTypeById(pt.getId());
      for (ComponentType ptm : newComponents.keySet()) {
        if (pt.getId() == ptm.getId()) {
          Integer count = newComponents.get(ptm) + 1;
          newComponents.put(componentType, count);
          check = true;
          break;
        }
      }
      if (!check) {
        newComponents.put(componentType, 1);
      }
    }
    
    // Remove parent component from inventory
    if (parentComponent.getInventoryStatus() == InventoryStatus.IN_STOCK) {
      parentComponent.setInventoryStatus(InventoryStatus.REMOVED);
    }

    for (ComponentType pt : newComponents.keySet()) {

      int noOfUnits = newComponents.get(pt);

      // Add New component
      if (!parentStatus.equals(ComponentStatus.PROCESSED) && !parentStatus.equals(ComponentStatus.DISCARDED)) {
        createChildComponent(pt, noOfUnits, parentComponent);
      }
    }

    // Set source component status to PROCESSED
    parentComponent.setStatus(ComponentStatus.PROCESSED);

    return updateComponent(parentComponent);
  }

  /**
   * Mark child component as unsafe where applicable.
   *
   * If the parent component is unsafe then the child component will be marked as unsafe (using
   * the reason UNSAFE_PARENT) except where the status change reason type is
   * TEST_RESULTS_CONTAINS_PLASMA and this component doesn't contain plasma.
   *
   * If the donation bleed time exceeds the maximum bleed time, or if the time since the donation
   * exceeds the max time since donation, for this component type then this component will be
   * marked as unsafe using EXCEEDS_MAX_BLEED_TIME or EXCEEDS_MAXTIME_SINCE_DONATION
   *
   * @param component the component
   */
  private void markChildComponentAsUnsafeWhereApplicable(Component component) {
    Component initialComponent = component.getParentComponent();
    Donation donation = initialComponent.getDonation();
    // If the component was processed more than once, get the initial component as the parent of the parent
    while (!initialComponent.isInitialComponent()) {
      initialComponent = initialComponent.getParentComponent();
    }

    if (initialComponent.getStatusChanges() != null) {
      for (ComponentStatusChange statusChange : initialComponent.getStatusChanges()) {
        
        if (statusChange.getIsDeleted()) {
          // skip deleted status change reasons
          continue;
        }

        if (!component.getComponentType().getContainsPlasma()
            && statusChange.getStatusChangeReason().getCategory() == ComponentStatusChangeReasonCategory.UNSAFE
            && (statusChange.getStatusChangeReason().getType() == ComponentStatusChangeReasonType.TEST_RESULTS_CONTAINS_PLASMA
            || statusChange.getStatusChangeReason().getType() == ComponentStatusChangeReasonType.LOW_WEIGHT)) {
          // skip because the component doesn't contain plasma and the unsafe reason only applies to
          // components that do contain plasma
          continue;
        }

        if (statusChange.getStatusChangeReason().getCategory() == ComponentStatusChangeReasonCategory.UNSAFE) {
          markComponentAsUnsafe(component, ComponentStatusChangeReasonType.UNSAFE_PARENT);
          return;
        }

      }
    }

    long bleedTime = bleedTimeService.getBleedTime(donation.getBleedStartTime(), donation.getBleedEndTime());
    if (component.getComponentType().getMaxBleedTime() != null
        && bleedTime >= component.getComponentType().getMaxBleedTime()) {
      markComponentAsUnsafe(component, ComponentStatusChangeReasonType.EXCEEDS_MAX_BLEED_TIME);
    } else {
      if (initialComponent.getProcessedOn() == null) {
        LOGGER.warn("Ignoring the time since donation check since the processedOn date is null");
      } else {
        long timeSinceDonation = bleedTimeService.getTimeSinceDonation(
            initialComponent.getCreatedOn(), initialComponent.getProcessedOn());
        if (component.getComponentType().getMaxTimeSinceDonation() != null
              && timeSinceDonation >= component.getComponentType().getMaxTimeSinceDonation()) {
          markComponentAsUnsafe(component, ComponentStatusChangeReasonType.EXCEEDS_MAXTIME_SINCE_DONATION);
        }
      }
    }
  }

  public void discardComponents(List<Long> componentIds, Long discardReasonId, String discardReasonText) {
    for (Long id : componentIds) {
      discardComponent(id, discardReasonId, discardReasonText);
    }
  }

  public Component removeComponentFromStock(Component component) {
    LOGGER.info("Removing component " + component.getId() + " from stock");

    component.setInventoryStatus(InventoryStatus.REMOVED);
    return componentRepository.update(component);
  }

  public Component discardComponent(Long componentId, Long discardReasonId, String discardReasonText) {
    Component existingComponent = componentRepository.findComponentById(componentId);
    
    // update existing component status
    existingComponent.setStatus(ComponentStatus.DISCARDED);
    existingComponent.setDiscardedOn(new Date());
    
    // create a component status change for the component
    ComponentStatusChange statusChange = new ComponentStatusChange();
    statusChange.setNewStatus(ComponentStatus.DISCARDED);
    statusChange.setStatusChangedOn(new Date());
    ComponentStatusChangeReason discardReason = new ComponentStatusChangeReason();
    discardReason.setId(discardReasonId);
    statusChange.setStatusChangeReason(discardReason);
    statusChange.setStatusChangeReasonText(discardReasonText);
    statusChange.setChangedBy(SecurityUtils.getCurrentUser());
    statusChange.setComponent(existingComponent);
    existingComponent.addStatusChange(statusChange);
    
    // remove component from inventory
    if (existingComponent.getInventoryStatus() == InventoryStatus.IN_STOCK) {
      existingComponent.setInventoryStatus(InventoryStatus.REMOVED);
    }
    
    return updateComponent(existingComponent);
  }
  
  public Component undiscardComponent(long componentId) {
    Component existingComponent = componentRepository.findComponentById(componentId);
    
    if (!componentConstraintChecker.canUndiscard(existingComponent)) {
      throw new IllegalStateException("Component " + componentId + " cannot be undiscarded.");
    }
    
    LOGGER.info("Undiscarding component " + componentId);
    
    // Add component back into inventory if it has previously been removed
    if (existingComponent.getInventoryStatus() == InventoryStatus.REMOVED) {
      existingComponent.setInventoryStatus(InventoryStatus.IN_STOCK);
    }

    return rollBackComponentStatus(existingComponent, ComponentStatusChangeReasonCategory.DISCARDED);
  }
  
  public Component preProcessComponent(long componentId, Integer componentWeight, Date bleedStartTime, Date bleedEndTime) {
    Component existingComponent = componentRepository.findComponentById(componentId);

    // update donation bleed times
    Donation existingDonation = existingComponent.getDonation();
    existingDonation.setBleedStartTime(bleedStartTime);
    existingDonation.setBleedEndTime(bleedEndTime);
    donationRepository.updateDonation(existingDonation);

    // update component createdOn
    // note: existingComponent is the initial component because pre-processing is only done for the initial component
    Date newCreatedOn = dateGeneratorService.generateDateTime(existingComponent.getCreatedOn(), bleedStartTime);
    existingComponent.setCreatedOn(newCreatedOn);
    
    // check if the weight is being updated
    if (existingComponent.getWeight() != null && existingComponent.getWeight() == componentWeight) {
      return existingComponent;
    }

    // check if it is possible to update the weight
    if (!componentConstraintChecker.canPreProcess(existingComponent)) {
      throw new IllegalStateException("The component " + componentId 
          + " cannot be pre-processed");
    }
    // it's OK to update the weight
    existingComponent.setWeight(componentWeight);

    // roll back Component Status if unsafe. Note that only statuses that can be rolled back will
    // and should be e.g. for reasons of invalid weight or low weight.
    if (existingComponent.getStatus().equals(ComponentStatus.UNSAFE)) {
      rollBackComponentStatus(existingComponent, ComponentStatusChangeReasonCategory.UNSAFE);
    }

    // Mark the component as unsafe if necessary.
    if (componentStatusCalculator.shouldComponentBeDiscardedForInvalidWeight(existingComponent)) {
      existingComponent = markComponentAsUnsafe(existingComponent, ComponentStatusChangeReasonType.INVALID_WEIGHT);
    } else if (componentStatusCalculator.shouldComponentBeDiscardedForLowWeight(existingComponent)) {
      existingComponent = markComponentAsUnsafe(existingComponent, ComponentStatusChangeReasonType.LOW_WEIGHT);
    }

    return updateComponent(existingComponent);
  }

  public Component recordChildComponentWeight(long componentId, Integer componentWeight) {
    Component existingComponent = componentRepository.findComponentById(componentId);
    
    // check if the weight is being updated
    if (existingComponent.getWeight() != null && existingComponent.getWeight() == componentWeight) {
      return existingComponent;
    }

    // check if it is possible to update the weight of the child component
    if (!componentConstraintChecker.canRecordChildComponentWeight(existingComponent)) {
      throw new IllegalStateException("The weight of child component " + componentId 
          + " cannot be updated");
    }
    // it's OK to update the weight
    existingComponent.setWeight(componentWeight);

    return updateComponent(existingComponent);
  }
  
  public Component findComponentById(Long id) {
    return componentRepository.findComponentById(id);
  }
  
  public List<Component> findComponentsByDINAndType(String donationIdentificationNumber, Long componentTypeId) {
    return componentRepository.findComponentsByDINAndType(donationIdentificationNumber, componentTypeId);
  }

  public Component unprocessComponent(Component parentComponent) {
    if (!componentConstraintChecker.canUnprocess(parentComponent)) {
      throw new IllegalStateException("Component " + parentComponent.getId() + " cannot be unprocessed.");
    }
    LOGGER.info("Unprocessing component: " + parentComponent);
    List<Component> children = componentRepository.findChildComponents(parentComponent);
    for (Component child : children) {
      // mark all child components as deleted
      child.setIsDeleted(true);
      componentRepository.update(child);
    }

    // Add component back into inventory if it has previously been removed
    if (parentComponent.getInventoryStatus() == InventoryStatus.REMOVED) {
      parentComponent.setInventoryStatus(InventoryStatus.IN_STOCK);
    }

    // Reset processedOn date
    parentComponent.setProcessedOn(null);

    // FIXME: Create component status change for when processing a component
    return rollBackComponentStatus(parentComponent, null);
  }
  
  public Component markComponentAsUnsafe(Component component, ComponentStatusChangeReasonType reasonType) {

    LOGGER.info("Marking component " + component.getId() + " as UNSAFE with reason type: " + reasonType);

    // Create a component status change, with category UNSAFE and type reasonType, for the component
    ComponentStatusChange statusChange = new ComponentStatusChange();
    statusChange.setNewStatus(ComponentStatus.UNSAFE);
    statusChange.setStatusChangedOn(dateGeneratorService.generateDate());
    statusChange.setChangedBy(SecurityUtils.getCurrentUser());
    statusChange.setComponent(component);
    ComponentStatusChangeReason unsafeReason;
    try {
      unsafeReason = componentStatusChangeReasonRepository
        .findComponentStatusChangeReasonByCategoryAndType(ComponentStatusChangeReasonCategory.UNSAFE, reasonType);
    } catch(NoResultException e) {
      throw new IllegalArgumentException("Component status change reason with category UNSAFE and type " + reasonType + " doesn't exist");
    }
    statusChange.setStatusChangeReason(unsafeReason);
    component.addStatusChange(statusChange);

    // Check if the component is already in a final state
    if (!ComponentStatus.isFinalStatus(component.getStatus())) {
      // Set component as UNSAFE
      component.setStatus(ComponentStatus.UNSAFE);
    }
    return updateComponent(component);

  }
  
  public Component issueComponent(Component component, Location issueTo) {
    if (!issueTo.getIsUsageSite()) {
      throw new IllegalArgumentException("Can't issue a component to a location which is not a usage site: " + issueTo);
    }
    
    Date issuedDate = dateGeneratorService.generateDate();
    
    component.setInventoryStatus(InventoryStatus.REMOVED);
    component.setStatus(ComponentStatus.ISSUED);
    component.setIssuedOn(issuedDate);
    component.setLocation(issueTo);

    // Create a component status change for the component
    ComponentStatusChange statusChange = new ComponentStatusChange();
    statusChange.setNewStatus(ComponentStatus.ISSUED);
    statusChange.setStatusChangedOn(issuedDate);
    statusChange.setComponent(component);
    ComponentStatusChangeReason statusChangeReason = componentStatusChangeReasonRepository
        .findFirstComponentStatusChangeReasonForCategory(ComponentStatusChangeReasonCategory.ISSUED);
    statusChange.setStatusChangeReason(statusChangeReason);
    statusChange.setChangedBy(SecurityUtils.getCurrentUser());
    component.addStatusChange(statusChange);

    return updateComponent(component);
  }
  
  public Component transferComponent(Component component, Location transferTo) {
    if (!transferTo.getIsDistributionSite()) {
      throw new IllegalArgumentException("Can't transfer a component to a location which is not a distribution site: " + transferTo);
    }
    component.setLocation(transferTo);
    return updateComponent(component);
  }
  
  public Component returnComponent(Component component, Location returnedTo) {
    if (!returnedTo.getIsDistributionSite()) {
      throw new IllegalArgumentException(
          "Can't return a component to a location which is not a distribution site: " + returnedTo);
    }

    component.setStatus(ComponentStatus.AVAILABLE);
    component.setInventoryStatus(InventoryStatus.IN_STOCK);
    component.setLocation(returnedTo);

    // Create a component status change for the component
    Date now = dateGeneratorService.generateDate();
    ComponentStatusChange statusChange = new ComponentStatusChange();
    statusChange.setNewStatus(component.getStatus());
    statusChange.setStatusChangedOn(now);
    statusChange.setComponent(component);
    ComponentStatusChangeReason statusChangeReason = componentStatusChangeReasonRepository
        .findFirstComponentStatusChangeReasonForCategory(ComponentStatusChangeReasonCategory.RETURNED);
    statusChange.setStatusChangeReason(statusChangeReason);
    statusChange.setChangedBy(SecurityUtils.getCurrentUser());
    component.addStatusChange(statusChange);

    return updateComponent(component);
  }
  
  public Component putComponentInStock(Component component) {
    component.setInventoryStatus(InventoryStatus.IN_STOCK);
    return updateComponent(component);
  }

  /**
   * Roll back component status. This method is called whenever a component status needs to be
   * rolled back:
   * 
   * - when un-discarding: rollbackCategory = DISCARDED 
   * - when updating the weight: rollbackCategory = UNSAFE 
   * - when un-processing: Doesn't exist yet
   *
   * @param component the component
   * @param rollBackCategory the rollback category
   * @return the component
   */
  private Component rollBackComponentStatus(Component component, ComponentStatusChangeReasonCategory rollBackCategory) {
    
    ComponentStatus componentStatus = ComponentStatus.QUARANTINED;

    if (component.getStatusChanges() != null) {
      for (ComponentStatusChange statusChange : component.getStatusChanges()) {
        if (!statusChange.getIsDeleted()) {
          
          // If an UNSAFE status change that can't be rolled back is found, set the component status to UNSAFE
          if (statusChange.getStatusChangeReason().getCategory().equals(ComponentStatusChangeReasonCategory.UNSAFE)
              && !ComponentStatusChangeReasonType.canBeRolledBack(statusChange.getStatusChangeReason().getType())) {
            componentStatus = ComponentStatus.UNSAFE;
          }

          // Delete the status changes with reason category rollBackCategory, that can be rolled back
          if (statusChange.getStatusChangeReason().getCategory().equals(rollBackCategory)
              && ComponentStatusChangeReasonType.canBeRolledBack(statusChange.getStatusChangeReason().getType())) {
            statusChange.setIsDeleted(true);
          }
        }
      }
    }

    LOGGER.info("Rolling back component status change: " + rollBackCategory + " for component id " + component.getId());

    component.setStatus(componentStatus);
    return updateComponent(component);

  }

  private Component addComponent(Component component) {
    componentStatusCalculator.updateComponentStatus(component);
    componentRepository.save(component);
    return component;
  }
  
  private Component updateComponent(Component component) {
    componentStatusCalculator.updateComponentStatus(component);
    return componentRepository.update(component);
  }

  /**
   * Change the status of components linked to the donation from AVAILABLE to UNSAFE only If they contains Plasma.
   */
  public void markComponentsBelongingToDonationAsUnsafeIfContainsPlasma(Donation donation) {

    LOGGER.info("Marking components containing plasma as unsafe for donation: " + donation);

    for (Component component : donation.getComponents()) {

      if (component.getIsDeleted()) {
        // Skip deleted components
        continue;
      }

      if (component.getComponentType().getContainsPlasma()) {
        markComponentAsUnsafe(component, ComponentStatusChangeReasonType.TEST_RESULTS_CONTAINS_PLASMA);
      }
    }
  }

  /**
   * Create child components from produced component types
   *
   * @param pt produced component type
   * @param noOfUnits number of units produced
   * @param parentComponent component being processed
   */
  private void createChildComponent(ComponentType pt, int noOfUnits, Component parentComponent) {
    String componentTypeCode = pt.getComponentTypeCode();
    for (int i = 1; i <= noOfUnits; i++) {
      Component component = new Component();
      component.setIsDeleted(false);

      // if there is more than one unit of the component, append unit number suffix
      if (noOfUnits > 1) {
        component.setComponentCode(componentTypeCode + "-0" + i);
      } else {
        component.setComponentCode(componentTypeCode);
      }
      component.setComponentType(pt);
      component.setDonation(parentComponent.getDonation());
      component.setParentComponent(parentComponent);
      component.setStatus(ComponentStatus.QUARANTINED);
      component.setCreatedOn(parentComponent.getProcessedOn());
      component.setLocation(parentComponent.getLocation());
      component.setComponentBatch(parentComponent.getComponentBatch());

      DateTime donationBleedDateTime = new DateTime()
          .withDate(new LocalDate(parentComponent.getDonation().getDonationDate()))
          .withTime(new LocalTime(parentComponent.getDonation().getBleedStartTime()));

      component.setExpiresOn(calculateExpiresOn(donationBleedDateTime.toDate(), pt.getExpiresAfter(), pt.getExpiresAfterUnits()));

      addComponent(component);

      markChildComponentAsUnsafeWhereApplicable(component);
    }
  }
}

package org.jembi.bsis.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.jembi.bsis.backingform.TransfusionBackingForm;
import org.jembi.bsis.controllerservice.TransfusionControllerService;
import org.jembi.bsis.controllerservice.TransfusionReactionTypeControllerService;
import org.jembi.bsis.factory.ComponentTypeFactory;
import org.jembi.bsis.factory.LocationFactory;
import org.jembi.bsis.model.transfusion.TransfusionOutcome;
import org.jembi.bsis.repository.ComponentTypeRepository;
import org.jembi.bsis.repository.LocationRepository;
import org.jembi.bsis.utils.PermissionConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfusions")
public class TransfusionController {
  
  @Autowired
  private LocationRepository locationRepository;
  @Autowired
  private LocationFactory locationFactory;
  @Autowired
  private ComponentTypeRepository componentTypeRepository;
  @Autowired
  private ComponentTypeFactory componentTypeFactory;
  @Autowired
  private TransfusionReactionTypeControllerService transfusionReactionTypeControllerService;
  @Autowired
  private TransfusionControllerService transfusionControllerService;
  
  @RequestMapping(value = "/form", method = RequestMethod.GET)
  @PreAuthorize("hasRole('" + PermissionConstants.ADD_TRANSFUSION_DATA + "')")
  public Map<String, Object> addTransfusionFormGenerator(HttpServletRequest request) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("addTransfusionForm", new TransfusionBackingForm());
    map.put("componentTypes", componentTypeFactory.createViewModels(componentTypeRepository.getAllComponentTypesThatCanBeIssued()));    
    map.put("usageSites", locationFactory.createFullViewModels(locationRepository.getUsageSites()));
    map.put("transfusionReactionTypes", transfusionReactionTypeControllerService.getTransfusionReactionTypes());
    map.put("transfusionOutcomes", TransfusionOutcome.values());
    return map;
  }
  
  @RequestMapping(method = RequestMethod.POST)
  @PreAuthorize("hasRole('" + PermissionConstants.ADD_TRANSFUSION_DATA + "')")
  public ResponseEntity<Map<String, Object>> addTransfusionForm(@Valid @RequestBody TransfusionBackingForm backingForm) {
    Map<String, Object> map = new HashMap<>();
    map.put("transfusionForm", transfusionControllerService.createTransfusionForm(backingForm));
    return new ResponseEntity<>(map, HttpStatus.CREATED);
  }
  
}
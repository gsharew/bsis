package org.jembi.bsis.controller;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.jembi.bsis.backingform.ComponentTypeCombinationBackingForm;
import org.jembi.bsis.controllerservice.ComponentTypeCombinationControllerService;
import org.jembi.bsis.utils.PermissionConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("componenttypecombinations")
public class ComponentTypeCombinationController {

  @Autowired
  private ComponentTypeCombinationControllerService componentTypeCombinationControllerService;

  @RequestMapping(method = RequestMethod.GET, value = "/search")
  @PreAuthorize("hasRole('" + PermissionConstants.MANAGE_COMPONENT_COMBINATIONS + "')")
  public ResponseEntity<Map<String, Object>> getComponentTypeCombinations() {

    Map<String, Object> map = new HashMap<>();
    map.put("componentTypeCombinations", componentTypeCombinationControllerService.getComponentTypeCombinations(true));
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.GET, value = "/form")
  @PreAuthorize("hasRole('" + PermissionConstants.MANAGE_COMPONENT_COMBINATIONS + "')")
  public ResponseEntity<Map<String, Object>> getForm() {
    Map<String, Object> map = new HashMap<>();
    map.put("componentTypeCombination", new ComponentTypeCombinationBackingForm());
    map.put("sourceComponentTypes", componentTypeCombinationControllerService.getAllComponentTypes());
    map.put("producedComponentTypes", componentTypeCombinationControllerService.getAllComponentTypes());
    return new ResponseEntity<>(map, HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.POST)
  @PreAuthorize("hasRole('" + PermissionConstants.MANAGE_COMPONENT_COMBINATIONS + "')")
  public ResponseEntity<Map<String, Object>> addComponentTypeCombination(@Valid @RequestBody ComponentTypeCombinationBackingForm backingForm) {
    Map<String, Object> map = new HashMap<>();
    map.put("componentTypeCombination", componentTypeCombinationControllerService.createComponentTypeCombination(backingForm));
    return new ResponseEntity<>(map, HttpStatus.CREATED);
  }

  @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
  @PreAuthorize("hasRole('" + PermissionConstants.MANAGE_COMPONENT_COMBINATIONS + "')")
  public ResponseEntity<Map<String, Object>> updateComponentTypeCombination(@PathVariable("id") Long componentTypeCombinationBackingId,
                                                             @Valid @RequestBody ComponentTypeCombinationBackingForm backingForm) {

    // Use the id parameter from the path
    backingForm.setId(componentTypeCombinationBackingId);

    Map<String, Object> map = new HashMap<>();
    map.put("componentTypeCombination", componentTypeCombinationControllerService.updateComponentTypeCombination(backingForm));
    return new ResponseEntity<>(map, HttpStatus.OK);
  }
}

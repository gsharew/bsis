package org.jembi.bsis.service;

import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.apache.commons.lang3.StringUtils;
import org.jembi.bsis.model.component.Component;
import org.springframework.stereotype.Service;

@Service
public class ComponentVolumeService {

  private static final Logger LOGGER = Logger.getLogger(ComponentVolumeService.class);
  
  public Integer calculateVolume(Component component) {
    String warningMessage = "";
    if (component.getWeight() == null) {
      warningMessage +="weight not set";
    }
    if (component.getComponentType().getGravity() == null) {
      String gravityWarningMessage = "gravity not set for component type with id '"
          + component.getComponentType().getId()+"'";
      warningMessage += (StringUtils.isBlank(warningMessage) ? gravityWarningMessage : 
        " and "+gravityWarningMessage);
    }
    if (StringUtils.isNotBlank(warningMessage)) {
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Component with id '"+component.getId()+"' has the "
            +"following properties not configured correctly: "+warningMessage);
      }
      return null;
    }
    return BigDecimal.valueOf((Double.valueOf(component.getWeight()) 
        / component.getComponentType().getGravity()))
      .round(new MathContext(2, RoundingMode.HALF_UP)).intValue();
  }
}
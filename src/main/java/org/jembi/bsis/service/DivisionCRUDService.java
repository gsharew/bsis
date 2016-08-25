package org.jembi.bsis.service;

import javax.transaction.Transactional;

import org.jembi.bsis.model.location.Division;
import org.jembi.bsis.repository.DivisionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Transactional
@Service
public class DivisionCRUDService {

  @Autowired
  DivisionRepository divisionRepository;

  public Division createDivision(Division division) {
    Division div = divisionRepository.createDivision(division);
    return div;
  }
  
  public Division updateDivision(Division division) {
    Division existingDivision = divisionRepository.findDivisionById(division.getId());
    existingDivision.setName(division.getName());
    existingDivision.setLevel(division.getLevel());
    existingDivision.setParent(division.getParent());
    return divisionRepository.update(existingDivision);
  }

}

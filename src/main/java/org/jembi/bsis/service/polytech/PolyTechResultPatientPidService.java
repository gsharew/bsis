package org.jembi.bsis.service.polytech;

import java.util.List;


import org.jembi.bsis.model.polytech.PolyTechResultPatientPidModel;

import org.jembi.bsis.repository.polytech.PolyTechResultPatientPidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class PolyTechResultPatientPidService {

    @Autowired
    private PolyTechResultPatientPidRepository polyTechResultPatientPidRepository;
    public List<PolyTechResultPatientPidModel> getAll() {
       List<PolyTechResultPatientPidModel> polyTechResultPatientPidModels = polyTechResultPatientPidRepository.getAllData();
       return polyTechResultPatientPidModels;
    }

    public PolyTechResultPatientPidModel insertData(PolyTechResultPatientPidModel polyTechResultPatientPidModel){
        return polyTechResultPatientPidRepository.addData(polyTechResultPatientPidModel);
    }

}

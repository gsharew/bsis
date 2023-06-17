package org.jembi.bsis.factory.polytech;

import org.jembi.bsis.backingform.polytech.PolyTechResultPatientPidBackingForm;
import org.jembi.bsis.model.polytech.PolyTechResultPatientPidModel;
import org.springframework.stereotype.Service;

@Service
public class PolyTechResultPatientPidFactory {

    public PolyTechResultPatientPidModel createEntity(PolyTechResultPatientPidBackingForm form) {
        PolyTechResultPatientPidModel polyTechResultPatientPidModel = new PolyTechResultPatientPidModel();
        polyTechResultPatientPidModel.setFirstname(form.getFirstname());
        polyTechResultPatientPidModel.setLastname(form.getLastname());
        polyTechResultPatientPidModel.setUsername(form.getUsername());
        polyTechResultPatientPidModel.setPassword(form.getPassword());
        return polyTechResultPatientPidModel;
    }

}

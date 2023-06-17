package org.jembi.bsis.controller.polytech;

import org.jembi.bsis.backingform.polytech.PolyTechResultPatientPidBackingForm;
import org.jembi.bsis.factory.polytech.PolyTechResultPatientPidFactory;
import org.jembi.bsis.model.polytech.PolyTechResultPatientPidModel;
import org.jembi.bsis.model.user.User;
import org.jembi.bsis.repository.polytech.PolyTechResultPatientPidRepository;
import org.jembi.bsis.utils.PermissionConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/poly_tech")
public class PolyTechResultPatientPidController {

    @Autowired
    private PolyTechResultPatientPidFactory polyTechResultPatientPidFactory;

    @Autowired
    private PolyTechResultPatientPidRepository polyTechResultPatientPidRepository;

    @RequestMapping(method = RequestMethod.POST)
    @PreAuthorize("hasRole('" + PermissionConstants.MANAGE_USERS + "')")
    @ResponseStatus(HttpStatus.CREATED)
    public PolyTechResultPatientPidModel addData(@Valid @RequestBody PolyTechResultPatientPidBackingForm form) {
        PolyTechResultPatientPidModel polyTechResultPatientPidModel = polyTechResultPatientPidFactory.createEntity(form);
        String hashedPassword = getHashedPassword(polyTechResultPatientPidModel.getPassword());
        polyTechResultPatientPidModel.setPassword(hashedPassword);
        polyTechResultPatientPidModel = polyTechResultPatientPidRepository.addData(polyTechResultPatientPidModel);
        return polyTechResultPatientPidModel;
    }


    @RequestMapping(method = RequestMethod.GET)
    @PreAuthorize("hasRole('" + PermissionConstants.MANAGE_USERS + "')")
    public List<PolyTechResultPatientPidModel> getAllData() {
        //return new ArrayList<>();
        return polyTechResultPatientPidRepository.getAllData();
    }

//    @RequestMapping(method = RequestMethod.GET)
//    @PreAuthorize("hasRole('" + PermissionConstants.MANAGE_USERS + "')")
//    public Map<String, Object> getAllData() {
//        Map<String, Object> map = new HashMap<String, Object>();
//        List<PolyTechResultPatientPidModel> polyTechResultPatientPidModels = polyTechResultPatientPidRepository.getAllData();
//        for(PolyTechResultPatientPidModel polyTechResultPatientPidModel : polyTechResultPatientPidModels){
//            map.put("user", polyTechResultPatientPidModel);
//        }
//        return map;
//    }

    private String getHashedPassword(String rawPassword) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(rawPassword);
        return hashedPassword;
    }

}

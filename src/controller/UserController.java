package controller;

import backingform.UserBackingForm;
import backingform.validator.UserBackingFormValidator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityExistsException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import model.user.Role;
import model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import repository.RoleRepository;
import repository.UserRepository;
import utils.PermissionConstants;
import viewmodel.UserViewModel;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UtilController utilController;

    @Autowired
    private RoleRepository roleRepository;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new UserBackingFormValidator(binder.getValidator(), utilController, userRepository));
    }

    @RequestMapping(value = "/configure", method = RequestMethod.GET)
    @PreAuthorize("hasRole('" + PermissionConstants.MANAGE_USERS + "')")
    public 
    Map<String, Object> configureUsersFormGenerator(HttpServletRequest request) {

        Map<String, Object> map = new HashMap<String, Object>();
        addAllUsersToModel(map);
        map.put("refreshUrl", utilController.getUrl(request));
        map.put("userRoles", roleRepository.getAllRoles());
        return map;
    }



    @RequestMapping(value = "{id}/edit", method = RequestMethod.GET)
    @PreAuthorize("hasRole('" + PermissionConstants.MANAGE_USERS + "')")
    public 
    Map<String, Object> editUserFormGenerator(HttpServletRequest request,
            @PathVariable Integer id) {
        UserBackingForm form = new UserBackingForm();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("requestUrl", utilController.getUrl(request));
        if (id != null) {
            form.setId(id);
            User user = userRepository.findUserById(id);
            if (user != null) {
                form = new UserBackingForm(user);
                form.setCurrentPassword(user.getPassword());
                map.put("userRoles", roleRepository.getAllRoles());
                map.put("existingUser", true);
            } else {
                form = new UserBackingForm();
                map.put("existingUser", false);
            }
        }
        map.put("allRoles", roleRepository.getAllRoles());
        map.put("userRoles", form.getRoles());
        map.put("editUserForm", form);
        map.put("refreshUrl", utilController.getUrl(request));
        // to ensure custom field names are displayed in the form
        return map;
    }

    @RequestMapping(method = RequestMethod.POST)
    @PreAuthorize("hasRole('" + PermissionConstants.MANAGE_USERS + "')")
    public ResponseEntity
            addUser(@Valid @RequestBody UserBackingForm form) {
        
            User user = form.getUser();
            user.setIsDeleted(false);
            user.setRoles(assignUserRoles(form));
            user.setIsActive(true);
            userRepository.addUser(user);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasRole('" + PermissionConstants.MANAGE_USERS + "')")
    public ResponseEntity updateUser(
            @Valid @RequestBody UserBackingForm form,
            @PathVariable Integer id) {

        form.setIsDeleted(false);
        User user = form.getUser();
        user.setId(id);
        if (form.isModifyPassword()) {
            user.setPassword(form.getPassword());
        } else {
            user.setPassword(form.getCurrentPassword());
        } 
        user.setRoles(assignUserRoles(form));
        user.setIsActive(true);
        userRepository.updateUser(user, true);
    
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
    

    @RequestMapping(value = "/current", method = RequestMethod.GET)
    public User getUserDetails(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName(); //get logged in username
        return userRepository.findUser(userName);
    }
    
       private void addAllUsersToModel(Map<String, Object> m) {
        List<UserViewModel> users = userRepository.getAllUsers();
        m.put("allUsers", users);
    }

    public List<Role> assignUserRoles(UserBackingForm userForm) {
        List<String> userRoles = userForm.getUserRoles();
        List<Role> roles = new ArrayList<Role>();
        for (String roleId : userRoles) {
            roles.add(userRepository.findRoleById(Long.parseLong(roleId)));
        }
        return roles;
    }

    public String userRole(Integer id) {
        String userRole = "";
        User user = userRepository.findUserById(id);
        List<Role> roles = user.getRoles();
        if (roles != null && roles.size() > 0) {
            for (Role r : roles) {
                userRole = userRole + " " + r.getId();
            }

        }
        return userRole;
    }


}

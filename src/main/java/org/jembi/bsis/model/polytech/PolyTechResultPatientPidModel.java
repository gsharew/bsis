package org.jembi.bsis.model.polytech;
import org.hibernate.envers.Audited;
import org.jembi.bsis.model.BaseUUIDEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Audited
public class PolyTechResultPatientPidModel extends BaseUUIDEntity {
    private static final long serialVersionUID = 1L;
    @Column(length = 30)
    String username;
    @Column(length = 30)
    String firstname;
    @Column(length = 30)
    String lastname;
    @Column(length = 80)
    String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

package org.jembi.bsis.repository.polytech;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.jembi.bsis.model.polytech.PolyTechResultPatientPidModel;
import org.jembi.bsis.repository.AbstractRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PolyTechResultPatientPidRepository extends AbstractRepository<PolyTechResultPatientPidModel> {

    @PersistenceContext
    private EntityManager em;

    public List<PolyTechResultPatientPidModel> getAllData() {
        TypedQuery<PolyTechResultPatientPidModel> query = em
                .createQuery("SELECT data FROM PolyTechResultPatientPidModel data", PolyTechResultPatientPidModel.class);
        return query.getResultList();
    }

    public PolyTechResultPatientPidModel addData(PolyTechResultPatientPidModel polyTechResultPatientPidModel) {
        em.persist(polyTechResultPatientPidModel);
        em.flush();
        em.refresh(polyTechResultPatientPidModel);
        return polyTechResultPatientPidModel;
    }

}
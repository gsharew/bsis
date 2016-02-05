package repository;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import model.donor.DonorStatus;
import model.donor.MobileClinicDonor;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class MobileClinicRepository {

/**
 * The Constant LOGGER.
*/
@SuppressWarnings("unused")
private static final Logger LOGGER = Logger.getLogger(MobileClinicRepository.class);
    
  @PersistenceContext
  private EntityManager em;
  
  public List<MobileClinicDonor> findMobileClinicDonorsByVenue(Long venueId) throws NoResultException {
      return em.createQuery(
              "SELECT d FROM MobileClinicDonor d " +
              "WHERE d.venue.id = :venueId " +
              "AND d.isDeleted = :isDeleted " +
              "AND d.donorStatus NOT IN :excludedStatuses " +
              "ORDER BY d.lastName asc, d.firstName asc",
              MobileClinicDonor.class)
              .setParameter("venueId", venueId)
              .setParameter("isDeleted", false)
              .setParameter("excludedStatuses", Arrays.asList(DonorStatus.MERGED))
              .getResultList();
  }
  
}

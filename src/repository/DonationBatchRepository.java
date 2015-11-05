package repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import model.donation.Donation;
import model.donationbatch.DonationBatch;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class DonationBatchRepository {

  @PersistenceContext
  EntityManager em;

  public DonationBatchRepository() {
  }

  public DonationBatch findDonationBatchByIdEager(Integer batchId) {
    String queryString = "SELECT distinct b FROM DonationBatch b LEFT JOIN FETCH b.donations LEFT JOIN FETCH b.venue " +
                         "WHERE b.id = :batchId and b.isDeleted = :isDeleted";
    TypedQuery<DonationBatch> query = em.createQuery(queryString, DonationBatch.class);
    query.setParameter("isDeleted", Boolean.FALSE);
    DonationBatch b = query.setParameter("batchId", batchId).getSingleResult();
    return b;
  }

  public DonationBatch findDonationBatchById(Integer batchId) {
    String queryString = "SELECT distinct b FROM DonationBatch b LEFT JOIN FETCH b.donations " +
                         "WHERE b.id = :batchId and b.isDeleted = :isDeleted";
    TypedQuery<DonationBatch> query = em.createQuery(queryString, DonationBatch.class);
    query.setParameter("isDeleted", Boolean.FALSE);
    return query.setParameter("batchId", batchId).getSingleResult();
  }

  public DonationBatch findDonationBatchByBatchNumber(String batchNumber) throws NoResultException,NonUniqueResultException {
    String queryString = "SELECT distinct b FROM DonationBatch b LEFT JOIN FETCH b.donations " +
        "WHERE b.batchNumber = :batchNumber and b.isDeleted = :isDeleted";
    TypedQuery<DonationBatch> query = em.createQuery(queryString, DonationBatch.class);
    query.setParameter("isDeleted", Boolean.FALSE);
    try{
    return query.setParameter("batchNumber", batchNumber).getSingleResult();
    }catch(NoResultException ex){
        throw new NoResultException("No DonationBatch Exists with ID :"+ batchNumber);
    }
  }
  
  public DonationBatch
	  findDonationBatchByBatchNumberIncludeDeleted(String batchNumber){
	String queryString = "SELECT distinct b FROM DonationBatch b LEFT JOIN FETCH b.donations " +
	 "WHERE b.batchNumber = :batchNumber";
	TypedQuery<DonationBatch> query = em.createQuery(queryString, DonationBatch.class);
	DonationBatch batch = null;
	try{
	batch = query.setParameter("batchNumber", batchNumber).getSingleResult();
	}catch(Exception ex){}
	return batch;
	
  }

  public void addDonationBatch(DonationBatch donationBatch) {
    em.persist(donationBatch);
    em.flush();
    em.refresh(donationBatch);
  }
  
  public DonationBatch updateDonationBatch(DonationBatch donationBatch)throws IllegalArgumentException{
      DonationBatch existingBatch = findDonationBatchById(donationBatch.getId());
      existingBatch.copy(donationBatch);
      existingBatch.setIsClosed(donationBatch.getIsClosed());
      return em.merge(existingBatch);
  }
  

  public List<DonationBatch> findDonationBatches(Boolean isClosed, List<Long> venueIds, Date startDate, Date endDate) {
    String queryStr = "SELECT distinct b from DonationBatch b LEFT JOIN FETCH b.donations WHERE b.isDeleted=:isDeleted ";
    if(!venueIds.isEmpty()){
    	queryStr += "AND b.venue.id IN (:venueIds) ";
    }

    if (startDate != null) {
      queryStr += "AND b.modificationTracker.createdDate >= :startDate ";
    }

    if (endDate != null) {
      queryStr += "AND b.modificationTracker.createdDate <= :endDate ";
    }

    if(isClosed != null){
    	queryStr += "AND b.isClosed=:isClosed ";
    }
    
    TypedQuery<DonationBatch> query = em.createQuery(queryStr, DonationBatch.class);
    query.setParameter("isDeleted", false);
    if (startDate != null) {
      query.setParameter("startDate", startDate);
    }
    if (endDate != null) {
      query.setParameter("endDate", endDate);
    }
    if(!venueIds.isEmpty()){
    	query.setParameter("venueIds", venueIds);
    }
    if(isClosed != null){
    	query.setParameter("isClosed", isClosed);
    }
    
    return query.getResultList();
  }
  
  public List<DonationBatch> findUnassignedDonationBatches() {
    String queryStr = "SELECT distinct b from DonationBatch b LEFT JOIN FETCH b.donations WHERE b.isDeleted=:isDeleted " +
    	"AND b.isClosed=:isClosed " + 
    	"AND b.testBatch=null";

    TypedQuery<DonationBatch> query = em.createQuery(queryStr, DonationBatch.class);
    query.setParameter("isDeleted", false);
    query.setParameter("isClosed", true);
   
    
    return query.getResultList();
  }
  
  public List<Donation> findDonationsInBatch(Integer batchId) {
    DonationBatch donationBatch = findDonationBatchByIdEager(batchId);
    List<Donation> donations = new ArrayList<Donation>();
    for (Donation c : donationBatch.getDonations()) {
    	donations.add(c);
    }
    return donations;
  }
  
  
  public List<DonationBatch> getRecentlyClosedDonationBatches(Integer numOfResults){
      
       String queryStr = "SELECT b FROM DonationBatch b "
               + "WHERE isClosed = true  ORDER BY lastUpdated DESC";
         TypedQuery<DonationBatch> query = em.createQuery(queryStr, DonationBatch.class);
         query.setMaxResults(numOfResults);
         return query.getResultList();
      
  }
  
  public int countOpenDonationBatches() {
      return em.createNamedQuery(
              DonationBatchQueryConstants.NAME_COUNT_DONATION_BATCHES,
              Number.class)
              .setParameter("closed", false)
              .setParameter("deleted", false)
              .getSingleResult()
              .intValue();
  }
}

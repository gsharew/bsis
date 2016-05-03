package repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import model.location.Location;
import model.location.LocationType;

@Repository
@Transactional
public class LocationRepository {
  @PersistenceContext
  private EntityManager em;

  public void saveLocation(Location location) {
    em.persist(location);
    em.flush();
  }

  public List<Location> getLocationsByType(LocationType locationType) {
    List<Location> locations = null;

    TypedQuery<Location> query =
        em.createNamedQuery(LocationNamedQueryConstants.NAME_GET_LOCATIONS_BY_TYPE, Location.class);
    query.setParameter("isDeleted", false);
    boolean isVenue = false;
    boolean isUsageSite = false;
    boolean isProcessingSite = false;

    if (locationType.equals(LocationType.VENUE)) {
      isVenue = true;
    } else if (locationType.equals(LocationType.USAGE_SITE)) {
      isUsageSite = true;
    } else if (locationType.equals(LocationType.PROCESSING_SITE)) {
      isProcessingSite = true;
    }

    query.setParameter("isVenue", isVenue);
    query.setParameter("isUsageSite", isUsageSite);
    query.setParameter("isProcessingSite", isProcessingSite);
    locations = query.getResultList();

    return locations;
  }

  public List<Location> getAllLocations() {
    TypedQuery<Location> query =
        em.createNamedQuery(LocationNamedQueryConstants.NAME_GET_ALL_LOCATIONS, Location.class);
    return query.getResultList();
  }

  public Location getLocation(Long selectedLocationId) {
    TypedQuery<Location> query = em.createQuery(
        "SELECT l FROM Location l where l.id= :locationId",
        Location.class);
    query.setParameter("locationId", selectedLocationId);
    return query.getSingleResult();
  }

  public Location updateLocation(Long locationId, Location location) {
    Location existingLocation = em.find(Location.class, locationId);
    existingLocation.copy(location);
    em.merge(existingLocation);
    em.flush();
    return existingLocation;
  }

  public void deleteLocation(Long locationId) {
    Location existingLocation = em.find(Location.class, locationId);
    existingLocation.setIsDeleted(Boolean.TRUE);
    em.merge(existingLocation);
    em.flush();
  }

  public List<String> getAllUsageSitesAsString() {
    List<Location> locations = getLocationsByType(LocationType.USAGE_SITE);
    List<String> locationNames = new ArrayList<String>();
    for (Location l : locations) {
      if (l.getIsUsageSite())
        locationNames.add(l.getName());
    }
    return locationNames;
  }

  public Location findLocationByName(String locationName) throws NoResultException, NonUniqueResultException {
    TypedQuery<Location> query = em.createQuery(
        "SELECT l FROM Location l where l.name= :locationName",
        Location.class);
    query.setParameter("locationName", locationName);
    Location location = null;
    try {
      location = query.getSingleResult();
    } catch (NoResultException ex) {
    }
    return location;
  }
  
  public boolean verifyLocationExists(Long id) {
    Long count = em.createNamedQuery(LocationNamedQueryConstants.NAME_COUNT_LOCATION_WITH_ID, Long.class)
        .setParameter("id", id)
        .getSingleResult();
    if (count == 1) {
      return true;
    }
    return false;
  }
}

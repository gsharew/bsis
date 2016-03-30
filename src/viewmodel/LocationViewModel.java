package viewmodel;

import model.location.Location;

public class LocationViewModel {

  private Location location;

  public LocationViewModel(Location location) {
    this.location = location;
  }

  public Long getId() {
    return location.getId();
  }

  public String getName() {
    return location.getName();
  }

  public boolean getIsDeleted() {
    return location.getIsDeleted();
  }

  public boolean getIsUsageSite() {
    return location.getIsUsageSite();
  }

  public boolean getIsMobileSite() {
    return location.getIsMobileSite();
  }

  public boolean getIsVenue() {
    return location.getIsVenue();
  }
}

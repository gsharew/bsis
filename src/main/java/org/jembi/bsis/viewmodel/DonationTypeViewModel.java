package org.jembi.bsis.viewmodel;

public class DonationTypeViewModel extends BaseViewModel {

  private String type;
  private Boolean isDeleted;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Boolean getIsDeleted() {
    return isDeleted;
  }

  public void setIsDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
  }
}
package org.jembi.bsis.backingform;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.jembi.bsis.model.counselling.CounsellingStatus;
import org.jembi.bsis.utils.DateTimeSerialiser;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class PostDonationCounsellingBackingForm {

  @NotNull
  private Long id;

  private CounsellingStatus counsellingStatus;

  private Date counsellingDate;
  private String notes;
  private boolean flaggedForCounselling;
  private Boolean referred;

  public long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public CounsellingStatus getCounsellingStatus() {
    return counsellingStatus;
  }

  public void setCounsellingStatus(Integer counsellingStatusId) {
    if (counsellingStatusId == null) {
      return;
    }
    this.counsellingStatus = CounsellingStatus.findById(counsellingStatusId);
  }

  public Date getCounsellingDate() {
    return counsellingDate;
  }

  @JsonSerialize(using = DateTimeSerialiser.class)
  public void setCounsellingDate(Date counsellingDate) {
    this.counsellingDate = counsellingDate;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public void setFlaggedForCounselling(boolean flaggedForCounselling) {
    this.flaggedForCounselling = flaggedForCounselling;
  }

  public boolean getFlaggedForCounselling() {
    return flaggedForCounselling;
  }

  public Boolean isReferred() {
    return referred;
  }

  public void setReferred(Boolean referred) {
    this.referred = referred;
  }

}

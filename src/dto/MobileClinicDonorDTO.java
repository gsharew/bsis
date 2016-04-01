package dto;

import java.util.Date;

import model.donor.DonorStatus;
import model.location.Location;
import model.util.Gender;

public class MobileClinicDonorDTO {

  private Long id;
  private String donorNumber;
  private String firstName;
  private String lastName;
  private Gender gender;
  private String bloodAbo;
  private String bloodRh;
  private DonorStatus donorStatus;
  private Date birthDate;
  private Location venue;
  private Boolean isDeleted;
  
  public MobileClinicDonorDTO() {
    super();
  }
  
  public MobileClinicDonorDTO(Long id, String donorNumber, String firstName, String lastName, Gender gender, String bloodAbo,
      String bloodRh, DonorStatus donorStatus, Date birthDate, Location venue, Boolean isDeleted) {
    super();
    this.id = id;
    this.donorNumber = donorNumber;
    this.firstName = firstName;
    this.lastName = lastName;
    this.gender = gender;
    this.bloodAbo = bloodAbo;
    this.bloodRh = bloodRh;
    this.donorStatus = donorStatus;
    this.birthDate = birthDate;
    this.venue = venue;
    this.isDeleted = isDeleted;
  }
  
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getDonorNumber() {
    return donorNumber;
  }

  public void setDonorNumber(String donorNumber) {
    this.donorNumber = donorNumber;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public String getBloodAbo() {
    return bloodAbo;
  }

  public void setBloodAbo(String bloodAbo) {
    this.bloodAbo = bloodAbo;
  }

  public String getBloodRh() {
    return bloodRh;
  }

  public void setBloodRh(String bloodRh) {
    this.bloodRh = bloodRh;
  }

  public DonorStatus getDonorStatus() {
    return donorStatus;
  }

  public void setDonorStatus(DonorStatus donorStatus) {
    this.donorStatus = donorStatus;
  }

  public Date getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(Date birthDate) {
    this.birthDate = birthDate;
  }

  public Location getVenue() {
    return venue;
  }

  public void setVenue(Location venue) {
    this.venue = venue;
  }

  public Boolean getIsDeleted() {
    return isDeleted;
  }

  public void setIsDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
  }
}

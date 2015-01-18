package viewmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Set;
import model.bloodtesting.BloodTest;
import model.worksheet.WorksheetType;
import scala.actors.threadpool.Arrays;

public class BloodTestViewModel {

  @JsonIgnore
  private BloodTest rawBloodTest;

  public BloodTestViewModel(BloodTest rawBloodTest) {
    this.rawBloodTest = rawBloodTest;
  }

  public BloodTest getRawBloodTest() {
    return rawBloodTest;
  }

  public void setRawBloodTest(BloodTest rawBloodTest) {
    this.rawBloodTest = rawBloodTest;
  }

  public Integer getId() {
    return rawBloodTest.getId();
  }

  public String getTestNameShort() {
    return rawBloodTest.getTestNameShort();
  }

  public String getTestName() {
    return rawBloodTest.getTestName();
  }

  public List<String> getValidResults() {
      return Arrays.asList(rawBloodTest.getValidResults().split(","));
  }

  public String getCategory() {
    String category = "";
    if (rawBloodTest.getCategory() == null)
      return "";
    switch (rawBloodTest.getCategory()) {
    case BLOODTYPING: category = "BLOODTYPING";
                      break;
    case TTI: category = "TTI";
              break;
    }
    return category;
  }

  public String getNegativeResults() {
    return rawBloodTest.getNegativeResults();
  }
  
  public String getPositiveResults(){
      return rawBloodTest.getPositiveResults();
  }

  public Boolean getIsActive() {
    return rawBloodTest.getIsActive();
  }

  public Integer getRankInCategory() {
    return rawBloodTest.getRankInCategory();
  }

  @JsonIgnore
  public Set<WorksheetType> getWorksheetTypes() {
    return rawBloodTest.getWorksheetTypes();
  }
}

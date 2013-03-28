package controller.bloodtyping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.bloodtyping.BloodTypingTest;
import model.bloodtyping.BloodTypingTestType;
import model.collectedsample.CollectedSample;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import repository.CollectedSampleRepository;
import repository.GenericConfigRepository;
import repository.bloodtyping.BloodTypingRepository;
import viewmodel.BloodTypingTestViewModel;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import controller.UtilController;

@Controller
public class BloodTypingController {

  @Autowired
  private UtilController utilController;

  @Autowired
  private CollectedSampleRepository collectedSampleRepository;

  @Autowired
  private GenericConfigRepository genericConfigRepository;

  @Autowired
  private BloodTypingRepository bloodTypingRepository;

  public BloodTypingController() {
  }

  public static String getUrl(HttpServletRequest req) {
    String reqUrl = req.getRequestURL().toString();
    String queryString = req.getQueryString();   // d=789
    if (queryString != null) {
        reqUrl += "?"+queryString;
    }
    return reqUrl;
  }

  @RequestMapping(value="/bloodTypingWorksheetGenerator", method=RequestMethod.GET)
  public ModelAndView getBloodTypingWorksheet(HttpServletRequest request) {
    ModelAndView mv = new ModelAndView("bloodtyping/bloodTypingWorksheetForm");

    Map<String, Object> tips = new HashMap<String, Object>();
    utilController.addTipsToModel(tips, "bloodtyping.plate.step1");
    mv.addObject("tips", tips);
    mv.addObject("plate", bloodTypingRepository.getPlate("bloodtyping"));
    mv.addObject("refreshUrl", "bloodTypingWorksheetGenerator.html");

    return mv;
  }

  @RequestMapping(value="/addCollectionsToBloodTypingPlate")
  public ModelAndView addCollectionsToBloodTypingPlate(HttpServletRequest request,
          HttpServletResponse response,
          @RequestParam(value="collectionNumbers[]") List<String> collectionNumbers) {

    Map<Integer, CollectedSample> collections = collectedSampleRepository.verifyCollectionNumbers(collectionNumbers);

    int numErrors = 0;
    int numValid = 0;
    for (CollectedSample c : collections.values()) {
      if (c == null)
        numErrors++;
      else
        numValid++;
    }

    ModelAndView mv = new ModelAndView();
    mv.addObject("collections", collections);
    mv.addObject("collectionNumbers", StringUtils.join(collectionNumbers,","));
    mv.addObject("plate", bloodTypingRepository.getPlate("bloodtyping"));

    Map<String, Object> tips = new HashMap<String, Object>();
    if (numErrors > 0 || numValid == 0) {
      mv.addObject("success", false);
      mv.setViewName("bloodtyping/bloodTypingWorksheetForm");
      mv.addObject("refreshUrl", "bloodTypingWorksheetGenerator.html");
      utilController.addTipsToModel(tips, "bloodtyping.plate.step1");
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      mv.addObject("errorMessage", "Please fix the errors noted. Some collections do not exist.");
    } else {
      mv.addObject("success", true);
      mv.setViewName("bloodtyping/bloodTypingWells");
      mv.addObject("refreshUrl", getUrl(request));
      mv.addObject("changeCollectionsUrl", "bloodTypingWorksheetGenerator.html");
      utilController.addTipsToModel(tips, "bloodtyping.plate.step2");
      mv.addObject("bloodTestsOnPlate", getBloodTestsOnPlate());
      mv.addObject("bloodTypingConfig", genericConfigRepository.getConfigProperties("bloodTyping"));
    }

    mv.addObject("tips", tips);
    return mv;
  }

  public List<BloodTypingTestViewModel> getBloodTestsOnPlate() {
    List<BloodTypingTestViewModel> tests = new ArrayList<BloodTypingTestViewModel>();
    for (BloodTypingTest rawBloodTest : bloodTypingRepository.getBloodTypingTestsOfType(BloodTypingTestType.BASIC)) {
      tests.add(new BloodTypingTestViewModel(rawBloodTest));
    }
    return tests;
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value="/saveBloodTypingTests", method=RequestMethod.POST)
  public ModelAndView saveBloodTypingTests(HttpServletRequest request,
      HttpServletResponse response, @RequestParam(value="bloodTypingTests") String bloodTypingTests,
      @RequestParam(value="collectionNumbers[]") List<String> collectionNumbers,
      @RequestParam(value="refreshUrl") String refreshUrl) {

    ModelAndView mv = new ModelAndView();
    Map<Integer, CollectedSample> collections = collectedSampleRepository.verifyCollectionNumbers(collectionNumbers);
    mv.addObject("collectionNumbers", StringUtils.join(collectionNumbers, ","));
    mv.addObject("collections", collections);

    Map<Long, Map<Long, String>> bloodTypingTestResults = parseBloodTypingTestResults(bloodTypingTests);

    mv.addObject("bloodTypingTestResults", bloodTypingTestResults);
    boolean success = true;
    Map<String, Object> results = null;
    try {
      results = bloodTypingRepository.saveBloodTypingResults(bloodTypingTestResults);
    } catch (Exception ex) {
      ex.printStackTrace();
      success = false;
    }
    Map<Long, Map<Long, String>> errorMap = (Map<Long, Map<Long, String>>) results.get("errors");
    if (errorMap != null && !errorMap.isEmpty())
      success = false;
    Map<String, Object> tips = new HashMap<String, Object>();
    System.out.println(errorMap);
    if (success) {
      List<BloodTypingTest> advancedTests = bloodTypingRepository.getBloodTypingTestsOfType(BloodTypingTestType.ADVANCED);
      Map<String, BloodTypingTest> advancedTestsMap = new HashMap<String, BloodTypingTest>();
      for (BloodTypingTest advancedTest : advancedTests) {
        advancedTestsMap.put(advancedTest.getId().toString(), advancedTest);
      }
      mv.addObject("advancedBloodTypingTests", advancedTestsMap);
      mv.addObject("collectionFields", utilController.getFormFieldsForForm("collectedSample"));
      mv.addObject("collections", results.get("collections"));
      mv.addObject("collectionIds", StringUtils.join(collections.keySet(), ","));
      mv.addObject("bloodTypingOutput", results.get("bloodTypingResults"));
      mv.addObject("success", success);
      mv.setViewName("bloodtyping/bloodTypingWellsSuccess");
    }
    else {
      // errors found
      mv.addObject("plate", bloodTypingRepository.getPlate("bloodtyping"));
      mv.addObject("errorMap", errorMap);
      mv.addObject("bloodTypingTests", bloodTypingTests);
      mv.addObject("success", success);
      mv.addObject("refreshUrl", refreshUrl);
      mv.addObject("changeCollectionsUrl", "bloodTypingWorksheetGenerator.html");
      utilController.addTipsToModel(tips, "bloodtyping.plate.step2");
      mv.addObject("bloodTestsOnPlate", getBloodTestsOnPlate());
      mv.addObject("bloodTypingConfig", genericConfigRepository.getConfigProperties("bloodTyping"));
      mv.addObject("errorMessage", "There were errors adding tests. Please verify the results in the wells highlighted in red.");      
      mv.setViewName("bloodtyping/bloodTypingWellsError");
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    return mv;
  }

  private Map<Long, Map<Long, String>> parseBloodTypingTestResults(
      String bloodTypingTests) {
    ObjectMapper mapper = new ObjectMapper();
    System.out.println(bloodTypingTests);
    Map<Long, Map<Long, String>> bloodTypingTestMap = new HashMap<Long, Map<Long,String>>();
    try {
      @SuppressWarnings("unchecked")
      Map<String, Map<String, String>> generatedMap = mapper.readValue(bloodTypingTests, HashMap.class);
      for (String collectionId : generatedMap.keySet()) {
        Map<Long, String> testResults = new HashMap<Long, String>();
        bloodTypingTestMap.put(Long.parseLong(collectionId), testResults);
        for (String testId : generatedMap.get(collectionId).keySet()) {
          testResults.put(Long.parseLong(testId), generatedMap.get(collectionId).get(testId));
        }
      } 
    } catch (JsonParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JsonMappingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println(bloodTypingTestMap);
    return bloodTypingTestMap;
  }

  @RequestMapping(value="/getBloodTypingStatusForCollections", method=RequestMethod.GET)
  public ModelAndView getBloodTypingStatusForCollections(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        @RequestParam(value="collectionIds") String collectionIdsParam
                        ) {
    ModelAndView mv = new ModelAndView();
    String[] collectionIds = collectionIdsParam.split(",");
    Map<String, Object> results = bloodTypingRepository.getBloodTypingTestStatus(Arrays.asList(collectionIds));
    List<BloodTypingTest> advancedTests = bloodTypingRepository.getBloodTypingTestsOfType(BloodTypingTestType.ADVANCED);
    Map<String, BloodTypingTest> advancedTestsMap = new HashMap<String, BloodTypingTest>();
    for (BloodTypingTest advancedTest : advancedTests) {
      advancedTestsMap.put(advancedTest.getId().toString(), advancedTest);
    }
    mv.addObject("advancedBloodTypingTests", advancedTestsMap);
    mv.addObject("collectionFields", utilController.getFormFieldsForForm("collectedSample"));
    mv.addObject("collections", results.get("collections"));
    mv.addObject("bloodTypingOutput", results.get("bloodTypingResults"));
    mv.addObject("success", true);
    mv.setViewName("bloodtyping/bloodTypingResultsForCollections");
    return mv;
  }

}

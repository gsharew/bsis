package org.jembi.bsis.model.componentmovement;

public enum ComponentStatusChangeReasonType {
  UNSAFE_PARENT, TEST_RESULTS, INVALID_WEIGHT, TEST_RESULTS_CONTAINS_PLASMA,
  EXCEEDS_MAX_BLEED_TIME, EXCEEDS_MAX_TIME_SINCE_DONATION;
  
  /**
   * Check if the specified ComponentStatusChangeReasonType may be deleted and rolled back.
   * Only ComponentStatusChangeReasonTypes that do not permanently affect the ComponentStatus
   * can be rolled back. For example if a Component has an unsafe test result, it must remain
   * in ComponentStatus.UNSAFE. 
   * 
   * @param status ComponentStatusChangeReasonType to check
   * @return true if the status can be safely rolled back and deleted
   */
  public static boolean canBeRolledBack(ComponentStatusChangeReasonType status) {
    if (status == null || status == INVALID_WEIGHT) {
      return true;
    }
    return false;
  }
}

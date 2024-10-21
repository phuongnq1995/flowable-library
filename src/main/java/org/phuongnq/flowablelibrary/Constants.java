package org.phuongnq.flowablelibrary;

public class Constants {
  public static final String VALIDATION_PROCESS = "validationProcess";

  public interface CandidateGroup {
    String VERIFICATION = "verifyGroups";
    String VALIDATION = "validateGroups";
  }

  public interface Condition {
    String VERIFICATION_STATUS = "verificationStatus";
    String VALIDATION_STATUS = "validationStatus";
  }

  public interface VerificationStatus {
    String OK = "OK";
    String NOK = "NOK";
  }

  public interface ValidationStatus {
    String OK = "OK";
    String NOK = "NOK";
    String POK = "POK";
  }
}

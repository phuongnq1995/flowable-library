package org.phuongnq.flowablelibrary;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class ValidationSucceedServiceTask implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) {
    System.out.println("Validation succeed for " + execution.getId());
    execution.getVariables()
        .forEach((s, o) -> System.out.printf("Key %s: %s%n", s, o));
  }
}

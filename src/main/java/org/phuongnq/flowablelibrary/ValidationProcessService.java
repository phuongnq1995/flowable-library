package org.phuongnq.flowablelibrary;

import static org.phuongnq.flowablelibrary.Constants.CandidateGroup;
import static org.phuongnq.flowablelibrary.Constants.Condition;
import static org.phuongnq.flowablelibrary.Constants.VALIDATION_PROCESS;
import static org.phuongnq.flowablelibrary.Constants.ValidationStatus;
import static org.phuongnq.flowablelibrary.Constants.VerificationStatus;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

public class ValidationProcessService {

  private final ProcessEngine processEngine;

  public ValidationProcessService(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  public String startProcess(String businessKey, Map<String, Object> data,
      Collection<String> verifyRoles, Collection<String> validateRoles) {
    Map<String, Object> variables = new HashMap<>();
    variables.putAll(data);
    variables.put(CandidateGroup.VERIFICATION, verifyRoles);
    variables.put(CandidateGroup.VALIDATION, validateRoles);

    RuntimeService runtimeService = processEngine.getRuntimeService();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(VALIDATION_PROCESS,
        businessKey, variables);

    return processInstance.getId();
  }

  public List<Task> getTasks(String role) {
    TaskService taskService = processEngine.getTaskService();
    return taskService.createTaskQuery().taskCandidateGroup(role).list();
  }

  public List<Task> getTasks(String role, int first, int max) {
    TaskService taskService = processEngine.getTaskService();
    return taskService.createTaskQuery().taskCandidateGroup(role).listPage(first, max);
  }

  public Map<String, Object> getTaskVariable(String taskId) {
    TaskService taskService = processEngine.getTaskService();
    return taskService.getVariables(taskId);
  }

  public void approveVerification(String taskId) {
    completeTask(taskId, Condition.VERIFICATION_STATUS, VerificationStatus.OK);
  }

  public void rejectVerification(String taskId) {
    completeTask(taskId, Condition.VERIFICATION_STATUS, VerificationStatus.NOK);
  }

  public void approveValidation(String taskId) {
    completeTask(taskId, Condition.VALIDATION_STATUS, ValidationStatus.OK);
  }

  public void rejectValidation(String taskId) {
    completeTask(taskId, Condition.VALIDATION_STATUS, ValidationStatus.NOK);
  }

  public void returnValidation(String taskId) {
    completeTask(taskId, Condition.VALIDATION_STATUS, ValidationStatus.POK);
  }

  public ProcessInstance getProcessInstance(String businessKey) {
    RuntimeService runtimeService = processEngine.getRuntimeService();
    return runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey)
        .singleResult();
  }

  public HistoricProcessInstance getHistory(String processId) {
    HistoryService historyService = processEngine.getHistoryService();
    return historyService.createHistoricProcessInstanceQuery().processInstanceId(processId)
        .singleResult();
  }

  private void completeTask(String taskId, String conditionName, String conditionValue) {
    TaskService taskService = processEngine.getTaskService();
    Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
    Map<String, Object> variables = task.getProcessVariables();
    variables.put(conditionName, conditionValue);
    taskService.complete(taskId, variables);
  }
}

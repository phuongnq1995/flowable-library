package org.phuongnq.flowablelibrary;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

/**
 * Hello world!
 */
public class App {

  private static ValidationProcessService validationProcessService;

  public static void main(String[] args) throws InterruptedException {
    System.out.println("Starting deploy BPMN processes...");

    ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration().setJdbcUrl(
            "jdbc:postgresql://localhost:5432/flowable").setJdbcUsername("postgres").setJdbcPassword("postgres")
        .setJdbcDriver("org.postgresql.Driver")
        .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

    ProcessEngine processEngine = cfg.buildProcessEngine();

    RepositoryService repositoryService = processEngine.getRepositoryService();
    Deployment deployment = repositoryService.createDeployment()
        .addClasspathResource("validation-process.bpmn20.xml").deploy();

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .deploymentId(deployment.getId()).singleResult();

    System.out.println("Deployed process: " + processDefinition.getName());

    validationProcessService = new ValidationProcessService(processEngine);

    simulator();
  }

  private static void simulator() throws InterruptedException {
    String businessKey = String.valueOf(new Random().nextInt(5));

    Scanner scanner = new Scanner(System.in);
    System.out.println("Enter pin:");
    String pin = scanner.nextLine();
    System.out.println("Verify role?");
    String verifyRole = scanner.nextLine();
    System.out.println("Validate role?");
    String validateRole = scanner.nextLine();

    Map<String, Object> data = new HashMap<>();
    data.put("pin", pin);

    String processId = validationProcessService.startProcess(businessKey, data, Collections.singleton(verifyRole),
        Collections.singleton(validateRole));

    Thread.sleep(5000);

    Task task = solveTask(scanner, verifyRole);

    Map<String, Object> processVariables = validationProcessService.getTaskVariable(task.getId());
    System.out.printf("Pin = %s, Do you want to approve this?%n", processVariables.get("pin"));
    boolean approved = scanner.nextLine().equalsIgnoreCase("y");
    if (approved) {
      validationProcessService.approveVerification(task.getId());
    } else {

      validationProcessService.rejectVerification(task.getId());
    }

    Thread.sleep(5000);

    Task validationTask = solveTask(scanner, validateRole);

    Map<String, Object> processVariables1 = validationProcessService.getTaskVariable(validationTask.getId());
    System.out.printf("Pin = %s, Do you want to approve this? OK, NOK, POK%n",
        processVariables1.get("pin"));
    String approved1 = scanner.nextLine();
    switch (approved1) {
      case "OK":
        validationProcessService.approveValidation(validationTask.getId());
        break;
      case "NOK":
        validationProcessService.rejectValidation(validationTask.getId());
        break;
      case "POK":
        validationProcessService.returnValidation(validationTask.getId());
        break;
      default:
        break;
    }

    System.out.println(validationProcessService.getProcessInstance(businessKey));
    System.out.println(validationProcessService.getHistory(processId));
    System.out.println();

    Thread.sleep(5000);
  }

  private static Task solveTask(Scanner scanner, String role) {
    System.out.printf("Login as %s role,%n", role);
    List<Task> tasks = validationProcessService.getTasks(role);
    System.out.println("You have " + tasks.size() + " tasks:");
    for (int i = 0; i < tasks.size(); i++) {
      System.out.println((i + 1) + ") " + tasks.get(i).getName());
    }
    System.out.println("Which task would you like to complete?");
    int taskIndex1 = Integer.valueOf(scanner.nextLine());

    return tasks.get(taskIndex1 - 1);
  }
}

package com.example;

import javax.persistence.*;
import java.util.concurrent.TimeUnit;

public class OptimisticLockingExample {

    public static void main(String[] args) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("example-pu");
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        EntityManager entityManager1 = entityManagerFactory.createEntityManager();
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
		
		  entityManager.getTransaction().begin();
		  
		  Employee insertEmployee = new Employee();
	          insertEmployee.setName("John Doe");
		  insertEmployee.setSalary(50000.0);
	          insertEmployee.setVersion(1);
		  entityManager.persist(insertEmployee); // Insert the employee into the

		  entityManager.getTransaction().commit();
		  
		  entityManager.close();
		 

        Long employeeId = 1L;

        Runnable updateTask = () -> {
            entityManager1.getTransaction().begin();
            Employee employee = entityManager1.find(Employee.class, employeeId);
            System.out.println("Thread 1: Employee fetched with version " + employee.getVersion());

            employee.setSalary(employee.getSalary() + 1000);
            entityManager1.getTransaction().commit();
            System.out.println("Thread 1: Employee updated with new salary");
        };

        Runnable updateTask2 = () -> {
            entityManager2.getTransaction().begin();
            Employee employee = entityManager2.find(Employee.class, employeeId);
            System.out.println("Thread 2: Employee fetched with version " + employee.getVersion());

            employee.setSalary(employee.getSalary() + 500);
            entityManager2.getTransaction().commit();
            System.out.println("Thread 2: Employee updated with new salary");
        };

        Thread thread1 = new Thread(updateTask);
        Thread thread2 = new Thread(updateTask2);

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        entityManager1.close();
        entityManager2.close();
        entityManagerFactory.close();
    }
}

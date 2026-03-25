/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package motorphpayrollsystem;

/*
 MotorPH Payroll System
 This program allows employees to view their details
 and payroll staff to compute payroll based on attendance.

 Features:
 - Employee login
 - Payroll staff login
 - Payroll calculation per cutoff
 - Government deductions (SSS, PhilHealth, PagIBIG, Tax)
*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;


public class MotorPHPayrollSystem {

    static final String EMP_FILE = "employee.csv";
    static final String ATT_FILE = "employee-attendance.csv";
    
    // Lists to store employee and attendance data in memory
    static java.util.List<String[]> employeeList = new java.util.ArrayList<>();
    static java.util.List<String[]> attendanceList = new java.util.ArrayList<>();
    
    // PayrollData class stores all payroll information for one employee
    static class PayrollData {
        String empNo;
        String name;
        String monthName;
        
        double cutoff1Hours, cutoff2Hours;
        double cutoff1Gross, cutoff2Gross;
        double  totalMonthlyGross;
        
        double sss, philhealth, pagibig, tax;
        double totalDeductions;
        double netSalary;
    }
    
    public static void main(String[] args) {
        
        try (Scanner sc = new Scanner(System.in)) {

            System.out.print("Enter username: ");
            String username = sc.nextLine();

            System.out.print("Enter password: ");
            String password = sc.nextLine();
            
            // Basic login validation
            if ((username.equals("employee") || username.equals("payroll_staff"))
               && password.equals("12345")) {

                System.out.println("Login successful!");
                loadData();// Load employee and attendance CSVs into memory
            } else {
                System.out.println("Incorrect username and/or password.");
                return;
            }

            if (username.equals("employee")) {
                employeeMenu(sc);
            } else {
                payrollMenu(sc);
            }
        }
    }
        // Load CSV files into memory once to avoid multiple reads
        static void loadData() {
            employeeList.clear();
            attendanceList.clear();

        // Load employees    
        try (BufferedReader br = new BufferedReader(new FileReader(EMP_FILE))) {
            br.readLine(); // skip header
            String line;
        while ((line = br.readLine()) != null) {
            employeeList.add(parseCSVLine(line));
            }
        } catch (IOException e) {
        System.out.println("Error loading employee data.");
    }

        // Load attendance
        try (BufferedReader br = new BufferedReader(new FileReader(ATT_FILE))) {
            br.readLine(); // skip header
            String line;
        while ((line = br.readLine()) != null) {
            attendanceList.add(parseCSVLine(line));
            }
        } catch (IOException e) {
        System.out.println("Error loading attendance data.");
        }
    }
        // Employee menu: allows employee to check their details
        static void employeeMenu(Scanner sc) {
            
                int option;
                
                do {
                    System.out.println("\n===== EMPLOYEE MENU =====");
                    System.out.println("1. Check Employee Details");
                    System.out.println("2. Exit");
                    System.out.print("Enter option: ");
                try{    
                    option = Integer.parseInt(sc.nextLine());
                } catch (NumberFormatException e) {
                    option = 0;
                }
                    switch (option) {
                        case 1 -> {
                            System.out.print("Enter Employee Number: ");
                            String empNo = sc.nextLine();
                            displayEmployee(empNo);
                        }
                        case 2 -> System.out.println("Exiting system...");
                        default -> System.out.println("Invalid option.");
                    }

                } while (option != 2);

            }
    
        // Payroll staff menu: allows processing of all months automatically
        static void payrollMenu(Scanner sc) {
            int option;
            
            do {
            System.out.println("\n===== PAYROLL STAFF MENU =====");
            System.out.println("1. One Employee (All Months)");
            System.out.println("2. All Employees (All Months)");
            System.out.println("3. Exit");
            System.out.print("Enter option: ");

            try {
                option = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                option = 0;
            }

            switch (option) {
                case 1 -> {
                    System.out.print("Enter Employee Number: ");
                    String empNo = sc.nextLine();
                    // Automatically process all months from June to December
                    for (int month = 6; month <= 12; month++) {
                        processPayroll(empNo, month);
                    }
                }

                case 2 -> {
                    // Process all employees for all months
                    for (int month = 6; month <= 12; month++) {
                        processAllPayroll(month);
                    }
                }

                case 3 -> System.out.println("Exiting system...");
                default -> System.out.println("Invalid option.");
            }

        } while (option != 3);
    }

    // Display employee details based on employee number
    static void displayEmployee(String empNo) {

         for (String[] data : employeeList) {

            if (clean(data[0]).trim().equalsIgnoreCase(empNo.trim())) {

            System.out.println("\n===== EMPLOYEE DETAILS =====");
            System.out.println("Employee #: " + empNo);
            System.out.println("Name: " + clean(data[1]) + " " + clean(data[2]));
            System.out.println("Birthday: " + clean(data[3]));
            return;
            }
        }

            System.out.println("Employee number does not exist.");
    }
    
    //Processes payroll for a single employee
    //Calculates hours worked, gross salary, deductions, and net salary
    static void processPayroll(String empNo, int month) {
        
        PayrollData d = computePayroll(empNo, month);
        
        if (d == null) {
            System.out.println("Employee not found.");
            return;
        }
        
        displayPayroll(d);
    }
    // Process payroll for all employees for a given month
    static void processAllPayroll(int month) {
        for (String[] data : employeeList) {
            processPayroll(clean(data[0]), month);
        }
    }
    

    // Compute payroll for one employee for a given month
    static PayrollData computePayroll(String empNo, int month) {
        String[] emp = getEmployeeData(empNo);
        if (emp == null) return null;

        PayrollData data = new PayrollData();

        data.empNo = empNo;
        data.name = emp[0] + " " + emp[1];

        double rate;
                
        try {
            rate = Double.parseDouble(
                clean(emp[2]).replace(",", "")
            );
        } catch (NumberFormatException e) {
            System.out.println("Invalid salary format for employee: " + empNo);
            return null;
        }
        // Calculate hours worked for both cutoffs
        data.cutoff1Hours = calculateHours(empNo, 1, month);
        data.cutoff2Hours = calculateHours(empNo, 2, month);
        
        // Compute gross pay for each cutoff
        data.cutoff1Gross = data.cutoff1Hours * rate;
        data.cutoff2Gross = data.cutoff2Hours * rate;
        data.totalMonthlyGross = data.cutoff1Gross + data.cutoff2Gross;
        
        // Government deductions
        data.sss = calculateSSS(data.totalMonthlyGross);
        data.philhealth = calculatePhilHealth(data.totalMonthlyGross);
        data.pagibig = calculatePagibig(data.totalMonthlyGross);

        double basicDeductions = data.sss + data.philhealth + data.pagibig;
        double taxableIncome = data.totalMonthlyGross - basicDeductions;
        
        // Tax applied to second cutoff only
        data.tax = calculateTax(taxableIncome);
        data.totalDeductions = basicDeductions + data.tax;
        
        // Net salary after deductions
        data.netSalary = data.totalMonthlyGross - data.totalDeductions;
        
        // Store month name as string
        String m = Month.of(month).toString();
        data.monthName = m.substring(0,1) + m.substring(1).toLowerCase();

        return data;
    }
    
    // Display payroll information
    static void displayPayroll(PayrollData d) {
        
        int lastDay = Month.valueOf(d.monthName.toUpperCase()).length(false);

        System.out.println("\n===============================");
        System.out.println("Payroll - " + d.monthName);
        System.out.println("===============================");

        System.out.println("Employee #: " + d.empNo);
        System.out.println("Name: " + d.name);
        
        // First cutoff
        System.out.println("\nCutoff: " + d.monthName + " 1 to 15");
        System.out.println("Hours: " + d.cutoff1Hours);
        System.out.println("Gross: " + d.cutoff1Gross);
        
        // Second cutoff
        System.out.println("\nCutoff: " + d.monthName + " 16 to " + lastDay);
        System.out.println("Hours: " + d.cutoff2Hours);
        System.out.println("Gross: " + d.cutoff2Gross);
        
        // Deductions displayed only after second cutoff
        System.out.println("\nDeductions:");
        System.out.println("SSS: " + d.sss);
        System.out.println("PhilHealth: " + d.philhealth);
        System.out.println("Pag-IBIG: " + d.pagibig);
        System.out.println("Tax: " +  d.tax);
        System.out.println("Total: " + d.totalDeductions);

        System.out.println("\nGross: " + d.totalMonthlyGross);
        System.out.println("Net Salary: " + d.netSalary);
    }
    
    // Retrieve employee data from memory list
    static String[] getEmployeeData(String empNo) {
        
        for (String[] data : employeeList) {

                if (clean(data[0]).trim().equalsIgnoreCase(empNo.trim())) {
                    return new String[]{
                        clean(data[1]).trim(), // First name
                        clean(data[2]).trim(), // Last name
                        clean(data[18]).replace(",", "") // Rate
                    };
                }
            }

        return null;
    }
    
    // Calculate total hours worked for a cutoff
    static double calculateHours(String empNo, int cutoff, int month) {
        
        double totalHours = 0;

        DateTimeFormatter tf = DateTimeFormatter.ofPattern("H:mm");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        LocalTime start = LocalTime.of(8, 0); // Official start time
        LocalTime grace = LocalTime.of(8, 10); // Grace period ends at 8:10
        LocalTime end = LocalTime.of(17, 0); // Official end time

        for (String[] d : attendanceList) {

            if (d.length < 6) continue;
            if (!clean(d[0]).trim().equalsIgnoreCase(empNo)) continue;

            LocalDate date = LocalDate.parse(clean(d[3]), df);
            if (date.getMonthValue() != month) continue;

            int day = date.getDayOfMonth();
            if (cutoff == 1 && day > 15) continue;
            if (cutoff == 2 && day <= 15) continue;

            LocalTime in = LocalTime.parse(clean(d[4]), tf);
            LocalTime out = LocalTime.parse(clean(d[5]), tf);

            // Grace period logic: early clock-ins up to 8:10 are counted as 8:00
            if (!in.isAfter(grace)) {
                in = start;
            }
            
            // Limit clock-out time to official end time
            if (out.isAfter(end)) out = end;
            
            // Compute total hours worked, subtract 1 hour for lunch if applicable
            double hours = Duration.between(in, out).toMinutes() / 60.0;
            if (hours > 1) {
                hours -= 1; // Deduct lunch
            } else {
                hours = 0; // Less than 1 hour worked is ignored
        }

            totalHours += hours;
        }
        return totalHours;
    }
    
    // SSS deduction based on monthly gross
     static double calculateSSS(double salary) {
        return (salary > 24750) ? 1125 : 500;
    }
     
    // PhilHealth contribution: 3% of salary, capped between 300-1800, divided by 2
    static double calculatePhilHealth(double salary) {
        double p = salary * 0.03;
        p = Math.max(300, Math.min(p, 1800));
        return p / 2;
    }
    
    // Pag-IBIG contribution: 1% if salary <= 1500, else 2%, capped at 100
    static double calculatePagibig(double salary) {
        double c = (salary <= 1500) ? salary * 0.01 : salary * 0.02;
        return Math.min(c, 100);
    }
    
    // Tax calculation based on graduated brackets
    static double calculateTax(double t) {

        if (t <= 20832) return 0;

        else if (t < 33333)
            return (t - 20832) * 0.20;

        else if (t < 66667)
            return 2500 + (t - 33333) * 0.25;

        else if (t < 166667)
            return 10833 + (t - 66667) * 0.30;

        else if (t < 666667)
            return 40833.33 + (t - 166667) * 0.32;

        else
            return 200833.33 + (t - 666667) * 0.35;
        
    }
    
    // Utility: split CSV line accounting for quoted commas
    static String[] parseCSVLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }
    
    // Remove extra quotes and whitespace
    static String clean(String value) {
        return value.replace("\"", "").trim();
    }
}        

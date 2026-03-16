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
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;


public class MotorPHPayrollSystem {

    static final String EMP_FILE = "employee.csv";
    static final String ATT_FILE = "employee-attendance.csv";
    
    public static void main(String[] args) {
        
        try (Scanner sc = new Scanner(System.in)) {

            System.out.print("Enter username: ");
            String username = sc.nextLine();

            System.out.print("Enter password: ");
            String password = sc.nextLine();
            
            // Validate login credentials for employee or payroll staff
            if ((username.equals("employee") || username.equals("payroll_staff"))
               && password.equals("12345")) {

                    System.out.println("Login successful!");

            } else {

                    System.out.println("Incorrect username and/or password.");
                    return;
            }

            if (username.equals("employee")) {
                
                int option;
                
                //Employee menu allows staff to view their personal details
                do {
                    System.out.println("\n===== EMPLOYEE MENU =====");
                    System.out.println("1. Check Employee Details");
                    System.out.println("2. Exit");
                    System.out.print("Enter option: ");

                    try {
                        option = Integer.parseInt(sc.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Employee number does not exist");
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

            } else {

                int option;
                
                //Payroll staff menu allows payroll processing
                //for one employee or all employees for a selected month
                do {
                    System.out.println("\n===== PAYROLL STAFF MENU =====");
                    System.out.println("1. One Employee");
                    System.out.println("2. All Employees");
                    System.out.println("3. Exit");
                    System.out.print("Enter option: ");

                    try {
                        option = Integer.parseInt(sc.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a number.");
                        option = 0;
                    }

                    switch (option) {

                        case 1 -> {
                            System.out.print("Enter Employee Number: ");
                                String empNo = sc.nextLine();

                                int month = selectMonth(sc);

                                processPayroll(empNo, month);// payroll for one employee
                        }

                        case 2 -> {
                                int month = selectMonth(sc);
                                processAllPayroll(month);
                            }// payroll for all employees

                        case 3 -> System.out.println("Exiting system...");

                        default -> System.out.println("Invalid option.");
                    }

                } while (option != 3);
                
            }
        }
    }
    
    static int selectMonth(Scanner sc) {
        
            System.out.println("\nSelect Month:");
            System.out.println("1. June");
            System.out.println("2. July");
            System.out.println("3. August");
            System.out.println("4. September");
            System.out.println("5. October");
            System.out.println("6. November");
            System.out.println("7. December");
            System.out.print("Choose month: ");

        int monthChoice = Integer.parseInt(sc.nextLine());

        //Convert menu selection to actual month number
        return monthChoice + 5;
}

    
    
    
    // Reads employee.csv and displays employee details
    static void displayEmployee(String empNo) {

         boolean found = false;

    try (BufferedReader br = new BufferedReader(new FileReader(EMP_FILE))) {

        br.readLine();
        String line;

        while ((line = br.readLine()) != null) {

            if (line.trim().isEmpty()) continue;

            String[] data = line.split(",", -1);

            if (data[0].trim().equals(empNo.trim())) {
                String lastName = data[1].trim();
                String firstName = data[2].trim();
                String birthday = data[3].trim();
               

                System.out.println("\n===== EMPLOYEE DETAILS =====");
                System.out.println("Employee Number: " + empNo);
                System.out.println("Employee Name: " + lastName + " " + firstName);
                System.out.println("Birthday: " + birthday);

                found = true;
                break;
                
            }
            
          }

        } catch (IOException e) {
            System.out.println("Error reading employee file.");
            return;
        }

        if (!found) {
            System.out.println("Employee number does not exist.");
        }
    }

    //Processes payroll for a single employee
    //Calculates hours worked, gross salary, deductions, and net salary
    static void processPayroll(String empNo, int month) {
        
        String[] empData = getEmployeeData(empNo);
        if (empData == null) {
            System.out.println("Employee not found.");
            return;
        }

        String lastName = empData[0];
        String firstName = empData[1];
        double hourlyRate = Double.parseDouble(empData[2]);

        int lastDay = java.time.Month.of(month).length(false);
        String monthName = java.time.Month.of(month).toString();
        monthName = monthName.substring(0,1) + monthName.substring(1).toLowerCase();

        double hours1 = calculateHours(empNo, 1, month);
        double gross1 = hours1 * hourlyRate;

        double hours2 = calculateHours(empNo, 2, month);
        double gross2 = hours2 * hourlyRate;

        double monthlyGross = gross1 + gross2;
        double sss = calculateSSS(monthlyGross);
        double philhealth = calculatePhilHealth(monthlyGross);
        double pagibig = calculatePagibig(monthlyGross);

        double basicDeductions = sss + philhealth + pagibig;
        double taxableIncome = monthlyGross - basicDeductions;
        double tax = calculateTax(taxableIncome);
        double totalDeductions = basicDeductions + tax;
        double netSalary = monthlyGross - totalDeductions;

        // PRINT CUT OFF 1
        System.out.println("\nEmployee #: " + empNo);
        System.out.println("Employee Name: " + lastName + " " + firstName);
        System.out.println("\nCutoff Date: " + monthName + " 1 to " + monthName + " 15");
        System.out.println("Total Hours Worked: " + String.format("%.2f", hours1));
        System.out.println("Gross Salary: " + String.format("%.2f", gross1));
        System.out.println("Net Salary: " + String.format("%.2f", gross1));

        // PRINT CUT OFF 2
        System.out.println("\nCutoff Date: " + monthName + " 16 to " + monthName + " " + lastDay);
        System.out.println("Total Hours Worked: " + String.format("%.2f", hours2));
        System.out.println("Gross Salary: " + String.format("%.2f", gross2));

        // PRINT DEDUCTIONS
        System.out.println("\nEach Deduction");
        System.out.println("SSS: " + String.format("%.2f", sss));
        System.out.println("PhilHealth: " + String.format("%.2f", philhealth));
        System.out.println("Pag-IBIG: " + String.format("%.2f", pagibig));
        System.out.println("Tax: " + String.format("%.2f", tax));
        System.out.println("Total Deductions: " + String.format("%.2f", totalDeductions));
        System.out.println("Net Salary: " + String.format("%.2f", netSalary));

        // MONTHLY SUMMARY
        System.out.println("\n----- Monthly Summary -----");
        System.out.println("Total Hours Worked: " + String.format("%.2f", hours1 + hours2));
        System.out.println("Gross Salary: " + String.format("%.2f", monthlyGross));
        System.out.println("Net Salary: " + String.format("%.2f", netSalary));
        System.out.println("---------------------------");
}

    
    // Processes payroll for every employee in the employee.csv file
    // Reuses the parocessPayroll() method
    static void processAllPayroll(int month) {

    try (BufferedReader br = new BufferedReader(new FileReader(EMP_FILE))) {

        br.readLine();
        String line;

        while ((line = br.readLine()) != null) {

            if (line.trim().isEmpty()) continue;

            String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            if (data.length <= 18) continue;

            String empNo = data[0];
            processPayroll(empNo, month); // reuse single employee function
        }

    } catch (IOException e) {
        System.out.println("Error reading employee file.");
    }
}

    // HELPER: get employee data [lastName, firstName, hourlyRate]
    static String[] getEmployeeData(String empNo) {

    try (BufferedReader br = new BufferedReader(new FileReader(EMP_FILE))) {
        
        br.readLine();
        String line;

        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            if (data.length > 18 && data[0].trim().equals(empNo.trim())) {
                String lastName = data[1].trim();
                String firstName = data[2].trim();
                String hourlyRate = data[18].replace("\"", "").replace(",", "").trim();
                return new String[]{lastName, firstName, hourlyRate};
            }
        }

    } catch (IOException e) {
        System.out.println("Error reading employee file.");
    }

    return null;
}
    

    // Calculate total working hours from attendance file
    // Applies grace period, work limits, and lunch break deduction
    static double calculateHours(String empNo, int cutoff, int month) {

        double totalHours = 0;

        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        
        LocalTime startLimit = LocalTime.of(8, 0);
        LocalTime graceLimit = LocalTime.of(8, 10);
        LocalTime endLimit = LocalTime.of(17, 0);

        try (BufferedReader br = new BufferedReader(new FileReader(ATT_FILE))) {

            br.readLine();
            String line;

            while ((line = br.readLine()) != null) {

            String[] data = line.split(",", -1);
            if (data.length < 6) continue;
            if (!data[0].trim().equals(empNo)) continue;

            LocalDate date = LocalDate.parse(data[3].trim(), dateFormat);
            int day = date.getDayOfMonth();
            if (cutoff == 1 && day > 15) continue;
            if (cutoff == 2 && day <= 15) continue;
            if (date.getMonthValue() != month) continue;

            LocalTime timeIn = LocalTime.parse(data[4].trim(), timeFormat);
            LocalTime timeOut = LocalTime.parse(data[5].trim(), timeFormat);

            // Apply start limit and grace period
            if (timeIn.isBefore(startLimit)) {
                timeIn = startLimit;
            } else if (timeIn.isAfter(graceLimit)) {
                
                long extraMinutes = Duration.between(graceLimit, timeIn).toMinutes();
                timeIn = startLimit.plusMinutes((int) extraMinutes);
            } else {
                
                timeIn = startLimit;
            }
            
            if (timeOut.isAfter(endLimit)) timeOut = endLimit;

            // Compute worked hours
            double hours = Duration.between(timeIn, timeOut).toMinutes() / 60.0;

            // Deduct 1 hour for lunch break
            hours -= 1;
            if (hours < 0) hours = 0;

            totalHours += hours;
        }

    } catch (IOException e) {
        System.out.println("Error reading attendance file.");
    }

    return totalHours;

    }     

    // Calculate SSS contribution based on salary bracket
    static double calculateSSS(double salary) {

    if (salary <= 3250) return 135.00;
        else if (salary <= 3750) return 157.50;
        else if (salary <= 4250) return 180.00;
        else if (salary <= 4750) return 202.50;
        else if (salary <= 5250) return 225.00;
        else if (salary <= 5750) return 247.50;
        else if (salary <= 6250) return 270.00;
        else if (salary <= 6750) return 292.50;
        else if (salary <= 7250) return 315.00;
        else if (salary <= 7750) return 337.50;
        else if (salary <= 8250) return 360.00;
        else if (salary <= 8750) return 382.50;
        else if (salary <= 9250) return 405.00;
        else if (salary <= 9750) return 427.50;
        else if (salary <= 10250) return 450.00;
        else if (salary <= 10750) return 472.50;
        else if (salary <= 11250) return 495.00;
        else if (salary <= 11750) return 517.50;
        else if (salary <= 12250) return 540.00;
        else if (salary <= 12750) return 562.50;
        else if (salary <= 13250) return 585.00;
        else if (salary <= 13750) return 607.50;
        else if (salary <= 14250) return 630.00;
        else if (salary <= 14750) return 652.50;
        else if (salary <= 15250) return 675.00;
        else if (salary <= 15750) return 697.50;
        else if (salary <= 16250) return 720.00;
        else if (salary <= 16750) return 742.50;
        else if (salary <= 17250) return 765.00;
        else if (salary <= 17750) return 787.50;
        else if (salary <= 18250) return 810.00;
        else if (salary <= 18750) return 832.50;
        else if (salary <= 19250) return 855.00;
        else if (salary <= 19750) return 877.50;
        else if (salary <= 20250) return 900.00;
        else if (salary <= 20750) return 922.50;
        else if (salary <= 21250) return 945.00;
        else if (salary <= 21750) return 967.50;
        else if (salary <= 22250) return 990.00;
        else if (salary <= 22750) return 1012.50;
        else if (salary <= 23250) return 1035.00;
        else if (salary <= 23750) return 1057.50;
        else if (salary <= 24250) return 1080.00;
        else if (salary <= 24750) return 1102.50;
        else return 1125.00;
    }

    // Calculate PhilHealth contribution (3% premium split between employer and employee)
    static double calculatePhilHealth(double monthlySalary) {

    double premium = monthlySalary * 0.03;

            if (premium < 300) {
                premium = 300;
    } else if  (premium > 1800) {
                premium = 1800;
    }

    double employeeShare = premium / 2;

    return employeeShare;
    
    }

    // Calculate Pag-IBIG contribution with maximum cap of 100
    static double calculatePagibig(double monthlySalary) {

    double contribution;

    if (monthlySalary <= 1500) {
        contribution = monthlySalary * 0.01;
    } else {
        
            contribution = monthlySalary * 0.02;
    }

        if (contribution > 100) {
            contribution = 100;
    }

        return contribution;
    
    }

    // Calculate withholding tax using TRAIN Law tax brackets
    static double calculateTax(double taxableIncome) {

    double tax;

    if (taxableIncome <= 20832) {
        tax = 0;

    } else if (taxableIncome < 33333) {
        tax = (taxableIncome - 20833) * 0.20;

    } else if (taxableIncome < 66667) {
        tax = 2500 + (taxableIncome - 33333) * 0.25;

    } else if (taxableIncome < 166667) {
        tax = 10833 + (taxableIncome - 66667) * 0.30;

    } else if (taxableIncome < 666667) {
        tax = 40833.33 + (taxableIncome - 166667) * 0.32;

    } else {
        tax = 200833.33 + (taxableIncome - 666667) * 0.35;
    }

    return tax;
    }
    
}

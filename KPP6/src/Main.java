import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

class Employee implements Serializable {
    String passportSeries;
    String passportNumber;
    BigDecimal salary;
    List<Characteristic> characteristics;

    public Employee(String passportSeries, String passportNumber, BigDecimal salary) {
        this.passportSeries = passportSeries;
        this.passportNumber = passportNumber;
        this.salary = salary;
        this.characteristics = new ArrayList<>();
    }

    public void addCharacteristic(String property, double rating) {
        characteristics.add(new Characteristic(property, rating));
    }

    public List<Characteristic> getCharacteristics() {
        return characteristics;
    }

    @Override
    public String toString() {
        return "Passport: " + passportSeries + "-" + passportNumber + ", Salary: " + salary;
    }
}

class Characteristic implements Serializable {
    private String property;
    private double rating;

    public Characteristic(String property, double rating) {
        this.property = property;
        this.rating = rating;
    }

    public String getProperty() {
        return property;
    }

    public double getRating() {
        return rating;
    }
}

class EmployeeContainer<T extends Employee> implements Iterable<T>, Serializable {
    List<T> employees = new ArrayList<>();

    public List<T> sortEmployeesByPassport() {
        List<T> sortedList = new ArrayList<>(employees);
        sortedList.sort(Comparator.comparing(e -> e.passportSeries + e.passportNumber));
        return sortedList;
    }

    public List<T> sortEmployeesBySalary() {
        List<T> sortedList = new ArrayList<>(employees);
        sortedList.sort(Comparator.comparing(e -> e.salary));
        return sortedList;
    }

    public void addEmployee(T employee) {
        employees.add(employee);
    }

    public void serialize(String fileName) {
        try (FileOutputStream fileOut = new FileOutputStream(fileName);
             ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
            objectOut.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T extends Employee> EmployeeContainer<T> deserialize(String fileName) {
        try (FileInputStream fileIn = new FileInputStream(fileName);
             ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {
            return (EmployeeContainer<T>) objectIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return employees.iterator();
    }

    public void removeEmployee(String passportSeries, String passportNumber) {
        employees.removeIf(e -> e.passportSeries.equals(passportSeries) && e.passportNumber.equals(passportNumber));
    }

    public List<T> searchEmployeesByPassport(String passportSeries, String passportNumber) {
        return employees.stream()
                .filter(e -> e.passportSeries.equals(passportSeries) && e.passportNumber.equals(passportNumber))
                .collect(Collectors.toList());
    }

    public List<T> searchEmployeesBySalary(BigDecimal minSalary, BigDecimal maxSalary) {
        return employees.stream()
                .filter(e -> e.salary.compareTo(minSalary) >= 0 && e.salary.compareTo(maxSalary) <= 0)
                .collect(Collectors.toList());
    }
}

public class Main {
    public static void main(String[] args) {
        EmployeeContainer<Employee> container = new EmployeeContainer<>();
        Scanner scanner = new Scanner(System.in);

        boolean isAutoMode = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("auto")) {
                isAutoMode = true;
                break;
            }
        }

        if (isAutoMode) {
            autoAddAndSaveData(container);
        } else {
            while (true) {
                System.out.println("Choose an action:");
                System.out.println("1. Display employees");
                System.out.println("2. Add employee");
                System.out.println("3. Sort employees by salary");
                System.out.println("4. Serialize or deserialize container");
                System.out.println("5. Remove employee by passport");
                System.out.println("6. Search employee by passport");
                System.out.println("7. Exit");

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        displayEmployees(container);
                        break;
                    case "2":
                        addEmployee(container, scanner);
                        break;
                    case "3":
                        displaySortedEmployees(container.sortEmployeesBySalary());
                        break;
                    case "4":
                        System.out.print("Enter the file name for serialization or deserialization: ");
                        String fileName = scanner.nextLine();
                        if (new File(fileName).exists()) {
                            container = EmployeeContainer.deserialize(fileName);
                            System.out.println("Data loaded from file '" + fileName + "'.");
                        } else {
                            container.serialize(fileName);
                            System.out.println("Data saved to file '" + fileName + "'.");
                        }
                        break;
                    case "5":
                        System.out.print("Enter passport series to remove: ");
                        String removeSeries = scanner.nextLine();
                        System.out.print("Enter passport number to remove: ");
                        String removeNumber = scanner.nextLine();
                        container.removeEmployee(removeSeries, removeNumber);
                        System.out.println("Employee with passport series " + removeSeries + " and passport number " + removeNumber + " removed.");
                        break;
                    case "6":
                        System.out.print("Enter passport series to search: ");
                        String searchSeries = scanner.nextLine();
                        System.out.print("Enter passport number to search: ");
                        String searchNumber = scanner.nextLine();
                        List<Employee> searchResult = container.searchEmployeesByPassport(searchSeries, searchNumber);
                        if (searchResult.isEmpty()) {
                            System.out.println("No employees found with passport series " + searchSeries + " and passport number " + searchNumber);
                        } else {
                            System.out.println("Employees with passport series " + searchSeries + " and passport number " + searchNumber + ":");
                            EmployeeContainer<Employee> searchResultContainer = new EmployeeContainer<>();
                            searchResultContainer.employees.addAll(searchResult);
                            displayEmployees(searchResultContainer);
                        }
                        break;
                    case "7":
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        break;
                }
            }
        }
    }

    public static void autoAddAndSaveData(EmployeeContainer<Employee> container) {
        Employee newEmployee = new Employee("XYZ", "98765", new BigDecimal("60000.00"));
        newEmployee.addCharacteristic("Experience", 5.5);
        container.addEmployee(newEmployee);
        container.serialize("test");
        System.out.println("Data added and saved to 'test' file.");
    }

    private static void addEmployee(EmployeeContainer<Employee> container, Scanner scanner) {
        System.out.print("Enter passport series: ");
        String passportSeries = scanner.nextLine();
        System.out.print("Enter passport number: ");
        String passportNumber = scanner.nextLine();
        System.out.print("Enter salary: ");
        BigDecimal salary = new BigDecimal(scanner.nextLine());
        Employee newEmployee = new Employee(passportSeries, passportNumber, salary);

        while (true) {
            System.out.print("Add a characteristic (Y/N): ");
            String choice = scanner.nextLine().trim().toLowerCase();

            if (choice.equals("y")) {
                System.out.print("Enter characteristic property: ");
                String property = scanner.nextLine();
                System.out.print("Enter characteristic rating: ");
                double rating = Double.parseDouble(scanner.nextLine());
                newEmployee.addCharacteristic(property, rating);
                break;
            } else if (choice.equals("n")) {
                break;
            } else {
                System.out.println("Invalid choice. Please enter Y or N.");
            }
        }

        container.addEmployee(newEmployee);
        System.out.println("Employee added.");
    }

    private static void displayEmployees(EmployeeContainer<Employee> container) {
        for (Employee employee : container) {
            System.out.println(employee);
            List<Characteristic> characteristics = employee.getCharacteristics();
            for (Characteristic characteristic : characteristics) {
                System.out.println("Characteristic: " + characteristic.getProperty() + ", Rating: " + characteristic.getRating());
            }
        }
    }

    private static void displaySortedEmployees(List<Employee> employees) {
        EmployeeContainer<Employee> sortedContainer = new EmployeeContainer<>();
        sortedContainer.employees.addAll(employees);
        displayEmployees(sortedContainer);
    }
}


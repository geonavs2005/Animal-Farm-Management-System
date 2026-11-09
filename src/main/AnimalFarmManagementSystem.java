import java.util.Scanner;
import model.User;
import service.UserService;

public class AnimalFarmManagementSystem {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        UserService userService = new UserService();

        boolean running = true;

        System.out.println("========================================");
        System.out.println("      ANIMAL FARM MANAGEMENT SYSTEM");
        System.out.println("========================================");

        while (running) {

            System.out.println("\n------------- MAIN MENU -------------");
            System.out.println("1. User Registration");
            System.out.println("2. User Login");
            System.out.println("3. Admin Login");
            System.out.println("4. Exit");
            System.out.println("-------------------------------------");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {

                // USER REGISTRATION
                case "1":

                    System.out.println("\n--- USER REGISTRATION ---");

                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();

                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();

                    User user = new User(username, password, "USER");

                    if (userService.register(user)) {
                        System.out.println("\nRegistration successful!");
                        System.out.println("You can now login.");
                    } else {
                        System.out.println("\nRegistration failed!");
                        System.out.println(
                            "Username cannot be empty and password must contain at least 4 characters."
                        );
                    }

                    break;

                // USER LOGIN
                case "2":

                    System.out.println("\n--- USER LOGIN ---");

                    System.out.print("Enter username: ");
                    String loginUsername = scanner.nextLine();

                    System.out.print("Enter password: ");
                    String loginPassword = scanner.nextLine();

                    if (userService.login(loginUsername, loginPassword)) {

                        String role = userService.getUserRole(loginUsername);

                        if (role.equalsIgnoreCase("USER")) {

                            System.out.println("\nLogin successful!");
                            System.out.println("Welcome, " + loginUsername + "!");

                            boolean userDashboard = true;

                            while (userDashboard) {

                                System.out.println("\n=================================");
                                System.out.println("         USER DASHBOARD");
                                System.out.println("=================================");
                                System.out.println("1. View Animals");
                                System.out.println("2. View Feed Information");
                                System.out.println("3. View Health Records");
                                System.out.println("4. Logout");
                                System.out.println("=================================");

                                System.out.print("Enter your choice: ");
                                String userChoice = scanner.nextLine();

                                switch (userChoice) {

                                    case "1":
                                        System.out.println("\n--- ANIMALS ---");
                                        System.out.println(
                                            "Animal information will be displayed here."
                                        );
                                        break;

                                    case "2":
                                        System.out.println("\n--- FEED INFORMATION ---");
                                        System.out.println(
                                            "Feed information will be displayed here."
                                        );
                                        break;

                                    case "3":
                                        System.out.println("\n--- HEALTH RECORDS ---");
                                        System.out.println(
                                            "Health records will be displayed here."
                                        );
                                        break;

                                    case "4":
                                        System.out.println("\nLogging out...");
                                        userDashboard = false;
                                        break;

                                    default:
                                        System.out.println(
                                            "\nInvalid choice. Please select 1-4."
                                        );
                                }
                            }

                        } else {
                            System.out.println(
                                "\nPlease use Admin Login for the admin account."
                            );
                        }

                    } else {
                        System.out.println("\nInvalid username or password.");
                    }

                    break;

                // ADMIN LOGIN
                case "3":

                    System.out.println("\n--- ADMIN LOGIN ---");

                    System.out.print("Enter admin username: ");
                    String adminUsername = scanner.nextLine();

                    System.out.print("Enter admin password: ");
                    String adminPassword = scanner.nextLine();

                    if (userService.login(adminUsername, adminPassword)
                            && userService.getUserRole(adminUsername)
                                    .equalsIgnoreCase("ADMIN")) {

                        System.out.println("\nAdmin login successful!");
                        System.out.println("Welcome, Administrator!");

                        // ADMIN DASHBOARD
                        boolean adminDashboard = true;

                        while (adminDashboard) {

                            System.out.println("\n=================================");
                            System.out.println("        ADMIN DASHBOARD");
                            System.out.println("=================================");
                            System.out.println("1. Manage Animals");
                            System.out.println("2. Manage Feed");
                            System.out.println("3. Manage Health Records");
                            System.out.println("4. View Users");
                            System.out.println("5. Logout");
                            System.out.println("=================================");

                            System.out.print("Enter your choice: ");
                            String adminChoice = scanner.nextLine();

                            switch (adminChoice) {

                                case "1":
                                    System.out.println("\n--- MANAGE ANIMALS ---");
                                    System.out.println(
                                        "Animal management will be added here."
                                    );
                                    break;

                                case "2":
                                    System.out.println("\n--- MANAGE FEED ---");
                                    System.out.println(
                                        "Feed management will be added here."
                                    );
                                    break;

                                case "3":
                                    System.out.println("\n--- MANAGE HEALTH RECORDS ---");
                                    System.out.println(
                                        "Health record management will be added here."
                                    );
                                    break;

                                case "4":
                                    System.out.println("\n--- USERS ---");
                                    System.out.println(
                                        "User information will be displayed here."
                                    );
                                    break;

                                case "5":
                                    System.out.println("\nAdmin logging out...");
                                    adminDashboard = false;
                                    break;

                                default:
                                    System.out.println(
                                        "\nInvalid choice. Please select 1-5."
                                    );
                            }
                        }

                    } else {
                        System.out.println(
                            "\nInvalid admin username or password."
                        );
                    }

                    break;

                // EXIT
                case "4":

                    System.out.println(
                        "\nThank you for using Animal Farm Management System!"
                    );

                    running = false;
                    break;

                default:

                    System.out.println(
                        "\nInvalid choice. Please select 1-4."
                    );
            }
        }

        scanner.close();
    }
}
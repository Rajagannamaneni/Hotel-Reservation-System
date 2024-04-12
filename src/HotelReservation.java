import java.sql.*;
import java.util.Scanner;

public class HotelReservation {
    private static final String url ="jdbc:mysql://localhost:3306/hoteldb";
    private static final String userName = "root";
    private static final String password ="root";

    public static void main(String[] args) throws ClassNotFoundException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            Connection connection = DriverManager.getConnection(url, userName, password);
            while (true) {
                System.out.println();
                System.out.println("HOTEL MANAGEMENT SYSTEM:");
                Scanner scanner = new Scanner(System.in);
                System.out.println("1.Reserve a room");
                System.out.println("2.View Reservations");
                System.out.println("3.Get Room Number");
                System.out.println("4.Update Reservations");
                System.out.println("5.Delete Reservations");
                System.out.println("0.Exit");
                System.out.println("Choose an option :");
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        reserveRoom(connection, scanner);
                        break;

                    case 2:
                        viewReservations(connection);
                        break;

                    case 3:
                        getRoomNumber(connection, scanner);
                        break;

                    case 4:
                        updateReservations(connection, scanner);
                        break;

                    case 5:
                        deleteReservations(connection, scanner);
                        break;

                    case 0:
                        exit();
                        scanner.close();
                        return;

                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void reserveRoom(Connection connection, Scanner scanner) throws SQLException {
        try {
            System.out.println("Enter guest name:");
            String guestName = scanner.next();
            scanner.nextLine();
            System.out.println("Enter Room Number:");
            int roomNumber = scanner.nextInt();
            System.out.println("Enter Contact Number:");
            String contactNumber = scanner.next();

            String sql = "insert into reservation(guest_name, room_number, contact_number) values('" + guestName + "', " + roomNumber + ", '" + contactNumber + "')";

            try (Statement statement = connection.createStatement()) {
                int affectedRows = statement.executeUpdate(sql);
                if (affectedRows > 0) {
                    System.out.println("Reservation Successful");
                } else {
                    System.out.println("Reservation Failed");
                }
            } catch (SQLException e) {
                e.printStackTrace();

            }

        } finally {
            scanner.nextLine(); // consume newline
        }
    }

    private static void viewReservations(Connection connection) throws SQLException {
        String sql = "select reservation_id, guest_name, room_number, contact_number, reservation_date from reservation";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            System.out.println("+------------------+---------------------+----------------+---------------------+----------------------+");
            System.out.println("| Reservation_Id  | Guest_Name          | Room_Number    | Contact_Number      | Reservation_date     |");
            System.out.println("+------------------+---------------------+----------------+---------------------+----------------------+");

            while (resultSet.next()) {
                int reservation_id = resultSet.getInt("reservation_id");
                String guestName = resultSet.getString("guest_name");
                int room_number = resultSet.getInt("room_number");
                String contactNumber = resultSet.getString("contact_number");
                String reservation_date = resultSet.getTimestamp("reservation_date").toString();
                System.out.printf("| %-15d | %-19s | %-14d | %-19s | %-20s |\n", reservation_id, guestName, room_number, contactNumber, reservation_date);
            }
            System.out.println("+------------------+---------------------+----------------+---------------------+----------------------+");
        }
    }

    private static void getRoomNumber(Connection connection, Scanner scanner) throws SQLException {
        try {
            System.out.println("Enter Reservation_id:");
            int reservationId = scanner.nextInt();
            scanner.nextLine();
            System.out.println("Enter guest Name:");
            String guestName = scanner.nextLine();

            String sql = "select room_Number from reservation where reservation_id = " + reservationId + " and guest_name = '" + guestName + "'";

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                if (resultSet.next()) {
                    int roomNumber = resultSet.getInt("room_number");
                    System.out.println("Room number for the ReservationId " + reservationId + " and guest name " + guestName + " is: " + roomNumber);
                } else {
                    System.out.println("Reservation is not found for the given id and guest name");
                }
            }

        } finally {
            scanner.nextLine(); // consume newline
        }
    }

    public static void updateReservations(Connection connection, Scanner scanner) {
        try {
            System.out.println("Enter the reservation_Id");
            int reservationId = scanner.nextInt();
            scanner.nextLine();

            if (!reservationExits(connection, reservationId)) {
                System.out.println("Reservation not found for the given id");
                return;
            }
            System.out.println("Enter the new guest name:");
            String guestName = scanner.nextLine();
            System.out.println("Enter new room number:");
            int roomNumber = scanner.nextInt();
            scanner.nextLine();
            System.out.println("Enter the new contact number:");
            String contactNumber = scanner.next();

            String sql = "update reservation set guest_name = '" + guestName + "', room_number = " + roomNumber + ", contact_number = '" + contactNumber + "' where reservation_id = " + reservationId + "";

            try (Statement statement = connection.createStatement()) {
                int affectedRows = statement.executeUpdate(sql);

                if (affectedRows > 0) {
                    System.out.println("Reservation updated successfully");
                } else {
                    System.out.println("Update reservation failed");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            scanner.nextLine(); // consume newline
        }
    }

    private static void deleteReservations(Connection connection, Scanner scanner) throws SQLException {
        try {
            System.out.println("Enter the reservation Id to delete:");
            int reservation_id = scanner.nextInt();
            scanner.nextLine();

            if (!reservationExits(connection, reservation_id)) {
                System.out.println("Reservation not found for the given id");
                return;
            }

            String sql = "delete from reservation where reservation_id = " + reservation_id;
            try {
                Statement statement = connection.createStatement();
                int affectedRows = statement.executeUpdate(sql);
                if (affectedRows > 0) {
                    System.out.println("Reservation deleted successfully");
                } else {
                    System.out.println("Reservation deletion failed");
                }

            }catch(SQLException e){
                e.printStackTrace();
            }

        }finally {
            scanner.nextLine(); // consume newline
        }
    }


    private static boolean reservationExits(Connection connection, int reservationId) {
        try {
            String sql = "select reservation_id from reservation where reservation_id = " + reservationId;

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {
                return resultSet.next(); // if there i5s a result the reservation exists;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false; // handle the database errors as needed
        }

    }

    public static void exit() throws InterruptedException {
        System.out.print("Exiting system");
        int i = 5;
        while (i != 0) {
            System.out.print(".");
            Thread.sleep(450);
            i--;
        }
        System.out.println();
        System.out.println("Thank you for using Hotel reservation system!!!");
    }
}

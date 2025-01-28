
package librarymanagementsystem;

import java.sql.*;
import java.util.Scanner;


class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/library";
    private static final String USER = "root";
    private static final String PASSWORD = "Nikki@05";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

class BookDAO {
    public void addBook(String title, String author, String publisher, int year, String isbn, int copies) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO Books (title, author, publisher, year_published, isbn, available_copies) VALUES (?, ?, ?, ?, ?, ?)");) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, publisher);
            ps.setInt(4, year);
            ps.setString(5, isbn);
            ps.setInt(6, copies);
            ps.executeUpdate();
        }
    }

    public void updateBook(int bookId, String title, String author, String publisher, int year, String isbn, int copies) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE Books SET title=?, author=?, publisher=?, year_published=?, isbn=?, available_copies=? WHERE book_id=?");) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, publisher);
            ps.setInt(4, year);
            ps.setString(5, isbn);
            ps.setInt(6, copies);
            ps.setInt(7, bookId);
            ps.executeUpdate();
        }
    }

    public void deleteBook(int bookId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM Books WHERE book_id=?");) {
            ps.setInt(1, bookId);
            ps.executeUpdate();
        }
    }

    public void queryBooks(String query) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM Books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ?");) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ps.setString(3, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getInt("book_id") + " - " + rs.getString("title") + " by " + rs.getString("author"));
            }
        }
    }
}

class MemberDAO {
    public void addMember(String name, String email, String phone, String membershipDate) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO Members (name, email, phone, membership_date) VALUES (?, ?, ?, ?)");) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setDate(4, Date.valueOf(membershipDate));
            ps.executeUpdate();
        }
    }

    public void updateMember(int memberId, String name, String email, String phone) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE Members SET name=?, email=?, phone=? WHERE member_id=?");) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setInt(4, memberId);
            ps.executeUpdate();
        }
    }

    public void deleteMember(int memberId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM Members WHERE member_id=?");) {
            ps.setInt(1, memberId);
            ps.executeUpdate();
        }
    }

    public void queryMembers(String query) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM Members WHERE name LIKE ? OR email LIKE ?");) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getInt("member_id") + " - " + rs.getString("name") + " (" + rs.getString("email") + ")");
            }
        }
    }
}

class TransactionDAO {
    public void borrowBook(int bookId, int memberId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO Transactions (book_id, member_id, borrow_date, status) VALUES (?, ?, CURDATE(), 'borrowed')");) {
            ps.setInt(1, bookId);
            ps.setInt(2, memberId);
            ps.executeUpdate();

            try (PreparedStatement updateBook = conn.prepareStatement("UPDATE Books SET available_copies = available_copies - 1 WHERE book_id=?");) {
                updateBook.setInt(1, bookId);
                updateBook.executeUpdate();
            }
        }
    }

    public void returnBook(int transactionId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE Transactions SET return_date=CURDATE(), status='returned' WHERE transaction_id=?");) {
            ps.setInt(1, transactionId);
            ps.executeUpdate();

            try (PreparedStatement updateBook = conn.prepareStatement("UPDATE Books SET available_copies = available_copies + 1 WHERE book_id=(SELECT book_id FROM Transactions WHERE transaction_id=?)");) {
                updateBook.setInt(1, transactionId);
                updateBook.executeUpdate();
            }
        }
    }

    public void queryTransactions(String query) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM Transactions WHERE member_id IN (SELECT member_id FROM Members WHERE name LIKE ?) OR book_id IN (SELECT book_id FROM Books WHERE title LIKE ?)");) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getInt("transaction_id") + " - Book ID: " + rs.getInt("book_id") + ", Member ID: " + rs.getInt("member_id") + ", Status: " + rs.getString("status"));
            }
        }
    }
}

public class LibraryManagementSystem {

    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        BookDAO bookDAO = new BookDAO();
        MemberDAO memberDAO = new MemberDAO();
        TransactionDAO transactionDAO = new TransactionDAO();

        while (true) {
            System.out.println("\nLibrary Management System");
            System.out.println("1. Manage Books");
            System.out.println("2. Manage Members");
            System.out.println("3. Manage Transactions");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            try {
                switch (choice) {
                    case 1:
                        System.out.println("1. Add Book\n2. Update Book\n3. Delete Book\n4. Query Books");
                        int bookChoice = scanner.nextInt();
                        scanner.nextLine();
                        switch (bookChoice) {
                            case 1:
                                System.out.print("Enter title: ");
                                String title = scanner.nextLine();
                                System.out.print("Enter author: ");
                                String author = scanner.nextLine();
                                System.out.print("Enter publisher: ");
                                String publisher = scanner.nextLine();
                                System.out.print("Enter year published: ");
                                int year = scanner.nextInt();
                                scanner.nextLine();
                                System.out.print("Enter ISBN: ");
                                String isbn = scanner.nextLine();
                                System.out.print("Enter available copies: ");
                                int copies = scanner.nextInt();
                                bookDAO.addBook(title, author, publisher, year, isbn, copies);
                                break;
                            case 2:
                                System.out.print("Enter book ID to update: ");
                                int bookId = scanner.nextInt();
                                scanner.nextLine();
                                System.out.print("Enter updated title: ");
                                title = scanner.nextLine();
                                System.out.print("Enter updated author: ");
                                author = scanner.nextLine();
                                System.out.print("Enter updated publisher: ");
                                publisher = scanner.nextLine();
                                System.out.print("Enter updated year published: ");
                                year = scanner.nextInt();
                                scanner.nextLine();
                                System.out.print("Enter updated ISBN: ");
                                isbn = scanner.nextLine();
                                System.out.print("Enter updated available copies: ");
                                copies = scanner.nextInt();
                                bookDAO.updateBook(bookId, title, author, publisher, year, isbn, copies);
                                break;
                            case 3:
                                System.out.print("Enter book ID to delete: ");
                                bookId = scanner.nextInt();
                                bookDAO.deleteBook(bookId);
                                break;
                            case 4:
                                System.out.print("Enter search query: ");
                                String query = scanner.nextLine();
                                bookDAO.queryBooks(query);
                                break;
                        }
                        break;

                    case 2:
                        System.out.println("1. Add Member\n2. Update Member\n3. Delete Member\n4. Query Members");
                        int memberChoice = scanner.nextInt();
                        scanner.nextLine();
                        switch (memberChoice) {
                            case 1:
                                System.out.print("Enter name: ");
                                String name = scanner.nextLine();
                                System.out.print("Enter email: ");
                                String email = scanner.nextLine();
                                System.out.print("Enter phone: ");
                                String phone = scanner.nextLine();
                                System.out.print("Enter membership date (YYYY-MM-DD): ");
                                String membershipDate = scanner.nextLine();
                                memberDAO.addMember(name, email, phone, membershipDate);
                                break;
                            case 2:
                                System.out.print("Enter member ID to update: ");
                                int memberId = scanner.nextInt();
                                scanner.nextLine();
                                System.out.print("Enter updated name: ");
                                name = scanner.nextLine();
                                System.out.print("Enter updated email: ");
                                email = scanner.nextLine();
                                System.out.print("Enter updated phone: ");
                                phone = scanner.nextLine();
                                memberDAO.updateMember(memberId, name, email, phone);
                                break;
                            case 3:
                                System.out.print("Enter member ID to delete: ");
                                memberId = scanner.nextInt();
                                memberDAO.deleteMember(memberId);
                                break;
                            case 4:
                                System.out.print("Enter search query: ");
                                String query = scanner.nextLine();
                                memberDAO.queryMembers(query);
                                break;
                        }
                        break;

                    case 3:
                        System.out.println("1. Borrow Book\n2. Return Book\n3. Query Transactions");
                        int transactionChoice = scanner.nextInt();
                        scanner.nextLine();
                        switch (transactionChoice) {
                            case 1:
                                System.out.print("Enter book ID to borrow: ");
                                int bookId = scanner.nextInt();
                                System.out.print("Enter member ID: ");
                                int memberId = scanner.nextInt();
                                transactionDAO.borrowBook(bookId, memberId);
                                break;
                            case 2:
                                System.out.print("Enter transaction ID to return: ");
                                int transactionId = scanner.nextInt();
                                transactionDAO.returnBook(transactionId);
                                break;
                            case 3:
                                System.out.print("Enter search query: ");
                                String query = scanner.nextLine();
                                transactionDAO.queryTransactions(query);
                                break;
                        }
                        break;

                    case 4:
                        System.out.println("Exiting...");
                        scanner.close();
                        return;

                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
    
}

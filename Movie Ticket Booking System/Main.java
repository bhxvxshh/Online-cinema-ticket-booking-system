import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.sql.*;

class Movie {
    private String name;
    private List<String> theaters; // Change to a list of theaters
    private List<String> seats;

    public Movie(String name, List<String> theaters, List<String> seats) {
        this.name = name;
        this.theaters = theaters;
        this.seats = seats;
    }

    public String getName() {
        return name;
    }

    public List<String> getTheaters() {
        return theaters;
    }

    public List<String> getSeats() {
        return seats;
    }
}

class User {
    private int id;
    private String username;
    private String password;
    private List<String> tickets;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.tickets = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getTickets() {
        return tickets;
    }
    public int getId() {
        return id;
    }

    // New method to save user to the database
    public void saveToDatabase() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/MovieBooking", "root", "b123")) {
            String query = "INSERT INTO users (username, password) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, username);
                statement.setString(2, password);
                statement.executeUpdate();

                ResultSet resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) {
                    id = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // New method to load user from the database
    public static User loadFromDatabase(String username) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/MovieBooking", "root", "b123")) {
            String query = "SELECT * FROM users WHERE username=?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    User user = new User(resultSet.getString("username"), resultSet.getString("password"));
                    user.id = resultSet.getInt("id");
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void bookTicket(String ticketDetails) {
        tickets.add(ticketDetails);
        saveTicketToDatabase(ticketDetails);
    }

    public void saveTicketToDatabase(String ticketDetails) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/MovieBooking", "root", "b123")) {
            String query = "INSERT INTO user_tickets (user_id, ticket_details) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, id);
                statement.setString(2, ticketDetails);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadTicketsFromDatabase() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/MovieBooking", "root", "b123")) {
            String query = "SELECT ticket_details FROM user_tickets WHERE user_id=?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, id);
                ResultSet resultSet = statement.executeQuery();

                tickets = new ArrayList<>();
                while (resultSet.next()) {
                    tickets.add(resultSet.getString("ticket_details"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTicketFromDatabase(String ticketDetails) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/MovieBooking", "root", "b123")) {
            String query = "DELETE FROM user_tickets WHERE user_id=? AND ticket_details=?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, id);
                statement.setString(2, ticketDetails);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cancelTicket(String ticketDetails) {
        tickets.remove(ticketDetails);
        deleteTicketFromDatabase(ticketDetails);
    }

}

class MovieTicketBookingSystem {
    private List<Movie> movies;
    private List<User> users;
    private DefaultListModel<String> movieListModel;
    private JList<String> movieList;
    private JComboBox<String> theaterComboBox;
    private JComboBox<String> seatComboBox;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private DefaultListModel<String> myTicketsListModel;
    private JList<String> myTicketsList;
    private User currentUser;

    public MovieTicketBookingSystem() {
        movies = new ArrayList<>();
        movies.add(new Movie("Barbie", List.of("Theater A", "Theater B"), List.of("A1", "A2", "A3","B1")));
        movies.add(new Movie("Batman Rises", List.of("Theater B", "Theater C"), List.of("B1", "B2", "B3")));
        // movies.add(new Movie("DARK KNIGHT RISES", List.of("SATHYAM"), List.of("A1", "A2", "A3")));
        // movies.add(new Movie("OPPENHEIMER", "INOX", List.of("B1", "B2", "B3")));
        // movies.add(new Movie("BATMAN RETURNS", "SATHYAM", List.of("B1", "B2", "B3")));
        // movies.add(new Movie("INSIDIOUS", "PVR VR", List.of("B1", "B2", "B3")));
        // movies.add(new Movie("BARBIE", "AGS VIVIRA", List.of("B1", "B2", "B3")));
        // movies.add(new Movie("THE MARVELS", "LUXE", List.of("B1", "B2", "B3")));
        // movies.add(new Movie("LEO", "PALAZZO", List.of("B1", "B2", "B3")));


        users = new ArrayList<>();
        users.add(new User("user1", "password1"));
        users.add(new User("user2", "password2"));

        movieListModel = new DefaultListModel<>();
        for (Movie movie : movies) {
            movieListModel.addElement(movie.getName());
        }

        createAndShowGUI();
    }

    public void createAndShowGUI() {
        JFrame loginFrame = new JFrame("Login or Create Account");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(300, 150);
        loginFrame.setLayout(new FlowLayout());

        JButton loginButton = new JButton("Login");
        JButton createAccountButton = new JButton("Create Account");

        loginFrame.add(loginButton);
        loginFrame.add(createAccountButton);

        loginButton.addActionListener(e -> {
            loginFrame.dispose(); // Close the login window
            showLoginWindow();
        });

        createAccountButton.addActionListener(e -> {
            loginFrame.dispose(); // Close the login window
            showCreateAccountWindow();
        });

        loginFrame.setVisible(true);
    }

    public void showLoginWindow() {
        JFrame loginWindow = new JFrame("Login");
        loginWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginWindow.setSize(400, 150);
        loginWindow.setLayout(new GridLayout(4, 2));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);

        JButton loginButton = new JButton("Login");

        loginWindow.add(usernameLabel);
        loginWindow.add(usernameField);
        loginWindow.add(passwordLabel);
        loginWindow.add(passwordField);
        loginWindow.add(new JLabel("")); // Empty label for spacing
        loginWindow.add(loginButton);

        loginButton.addActionListener(e -> {
            String enteredUsername = usernameField.getText();
            String enteredPassword = new String(passwordField.getPassword());
            currentUser = authenticateUser(enteredUsername, enteredPassword);
            if (currentUser != null) {
                currentUser.loadTicketsFromDatabase(); // Load tickets from the database
                updateMyTicketsListModel();
                loginWindow.dispose(); // Close the login window
                showBookingWindow();
            } else {
                JOptionPane.showMessageDialog(null, "Invalid username or password.");
            }
        });

        JButton backButton = new JButton("Back");
        loginWindow.add(backButton);

        backButton.addActionListener(e -> {
            loginWindow.dispose(); // Close the login window
            createAndShowGUI(); // Show the initial login or create account window
        });

        loginWindow.setVisible(true);
    }

    private void updateMyTicketsListModel() {
        if(myTicketsListModel == null) return;
        myTicketsListModel.clear();
        if (currentUser != null && currentUser.getTickets() != null) {
            for (String ticket : currentUser.getTickets()) {
                String[] lines = ticket.split("\\r?\\n");

            // Add each line to the model
                for (String line : lines) {
                    myTicketsListModel.addElement(line);
                }
            }
        }
    }


    public void showCreateAccountWindow() {
        JFrame createAccountWindow = new JFrame("Create Account");
        createAccountWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createAccountWindow.setSize(300, 150);
        createAccountWindow.setLayout(new GridLayout(4, 2));

        JLabel newUsernameLabel = new JLabel("New Username:");
        JTextField newUsernameField = new JTextField(20);
        JLabel newPasswordLabel = new JLabel("New Password:");
        JPasswordField newPasswordField = new JPasswordField(20);

        JButton createAccountButton = new JButton("Create Account");

        createAccountWindow.add(newUsernameLabel);
        createAccountWindow.add(newUsernameField);
        createAccountWindow.add(newPasswordLabel);
        createAccountWindow.add(newPasswordField);
        createAccountWindow.add(new JLabel("")); // Empty label for spacing
        createAccountWindow.add(createAccountButton);

        createAccountButton.addActionListener(e -> {
            String newUsername = newUsernameField.getText();
            String newPassword = new String(newPasswordField.getPassword());
            if (newUsername.isEmpty() || newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Username and password cannot be empty.");
            } else if (userExists(newUsername)) {

                JOptionPane.showMessageDialog(null, "Username already exists. Please choose a different username.");
            } else {
                users.add(new User(newUsername, newPassword));
                
                //DATABASE COMMAND
                User newUser = new User(newUsername, newPassword);
                newUser.saveToDatabase();

                JOptionPane.showMessageDialog(null, "Account created successfully!");
                createAccountWindow.dispose(); // Close the create account window
                showLoginWindow();
            }
        });

        JButton backButton = new JButton("Back");
        createAccountWindow.add(backButton);

        backButton.addActionListener(e -> {
            createAccountWindow.dispose(); // Close the create account window
            createAndShowGUI(); // Show the initial login or create account window
        });

        createAccountWindow.setVisible(true);
    }

    public void showBookingWindow() {
        JFrame bookingFrame = new JFrame("Movie Ticket Booking System");
        bookingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        bookingFrame.setSize(600, 400);
        bookingFrame.setLayout(new GridLayout(7, 2));

    

        JLabel movieLabel = new JLabel("Select a movie:");
        bookingFrame.add(movieLabel);

        movieList = new JList<>(movieListModel);
        bookingFrame.add(new JScrollPane(movieList));

        JLabel theaterLabel = new JLabel("Select a theater:");
        bookingFrame.add(theaterLabel);

        theaterComboBox = new JComboBox<>();
        bookingFrame.add(theaterComboBox);

        JLabel seatLabel = new JLabel("Select a seat:");
        bookingFrame.add(seatLabel);

        seatComboBox = new JComboBox<>();
        bookingFrame.add(seatComboBox);

        JButton bookButton = new JButton("Book Ticket");
        bookingFrame.add(bookButton);

        myTicketsListModel = new DefaultListModel<>();
        myTicketsList = new JList<>(myTicketsListModel);
        bookingFrame.add(new JScrollPane(myTicketsList));

        updateMyTicketsListModel();

        // bookButton.addActionListener(e -> {
        //     String selectedMovie = movieList.getSelectedValue();
        //     String selectedTheater = (String) theaterComboBox.getSelectedItem();
        //     String selectedSeat = (String) seatComboBox.getSelectedItem();
        //     if (currentUser != null) {
        //         selectedMovie = movieList.getSelectedValue();
        //         selectedTheater = (String) theaterComboBox.getSelectedItem();
        //         selectedSeat = (String) seatComboBox.getSelectedItem();
        //         String ticketDetails = selectedMovie + " at " + selectedTheater + ", Seat: " + selectedSeat;

        //         currentUser.bookTicket(ticketDetails);

        //         //updateMyTicketsListModel();

        //         myTicketsListModel.addElement(ticketDetails);
        //         JOptionPane.showMessageDialog(null, "Ticket booked for " + selectedMovie +
        //                 " at " + selectedTheater + ", Seat: " + selectedSeat);
        //     } else {
        //         JOptionPane.showMessageDialog(null, "Please log in to book a ticket.");
        //     }
        // });

        bookButton.addActionListener(e -> {
            String selectedMovie = movieList.getSelectedValue();
            String selectedTheater = (String) theaterComboBox.getSelectedItem();
            String selectedSeat = (String) seatComboBox.getSelectedItem();
            String selectedTicketDetails = selectedMovie + " at " + selectedTheater + ", Seat: " + selectedSeat;

            // Check if the selected ticket is already booked
            if (currentUser != null && currentUser.getTickets().contains(selectedTicketDetails)) {
                JOptionPane.showMessageDialog(null, "Error: This ticket has already been booked.");
            } else {
                // Book the ticket if it's not already booked
                if (currentUser != null) {
                    currentUser.bookTicket(selectedTicketDetails);
                    updateMyTicketsListModel(); // Update the booked tickets list
                    JOptionPane.showMessageDialog(null, "Ticket booked for " + selectedMovie +
                            " at " + selectedTheater + ", Seat: " + selectedSeat);
                } else {
                    JOptionPane.showMessageDialog(null, "Please log in to book a ticket.");
                }
            }
        });


        movieList.addListSelectionListener(e -> {
            String selectedMovie = movieList.getSelectedValue();
            Movie movie = findMovieByName(selectedMovie);
            if (movie != null) {
                theaterComboBox.removeAllItems();
                for (String theater : movie.getTheaters()) {
                    theaterComboBox.addItem(theater);
                }
                seatComboBox.removeAllItems();
                for (String seat : movie.getSeats()) {
                    seatComboBox.addItem(seat);
                }
            }
        });

        JLabel usernameLabel = new JLabel("Logged in as: " + currentUser.getUsername());
        bookingFrame.add(usernameLabel);

        JButton cancelButton = new JButton("Cancel Ticket");
        bookingFrame.add(cancelButton);

        cancelButton.addActionListener(e -> {
            String selectedTicket = myTicketsList.getSelectedValue();
            if (selectedTicket != null) {
                currentUser.cancelTicket(selectedTicket);
                updateMyTicketsListModel(); // Update the booked tickets list
                myTicketsListModel.removeElement(selectedTicket);
                JOptionPane.showMessageDialog(null, "Ticket canceled: " + selectedTicket);
            } else {
                JOptionPane.showMessageDialog(null, "Please select a ticket to cancel.");
            }
        });

        bookingFrame.setVisible(true);
    }

    public boolean userExists(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }


    // private User authenticateUser(String username, String password) {
    //     for (User user : users) {
    //         if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
    //             return user;
    //         }
    //     }
    //     return null;
    // }

    // private User authenticateUser(String username, String password) {
    //     try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/MovieBooking", "root", "b123")) {
    //         String query = "SELECT * FROM users WHERE username=? AND password=?";
    //         try (PreparedStatement statement = connection.prepareStatement(query)) {
    //             statement.setString(1, username);
    //             statement.setString(2, password);
    //             ResultSet resultSet = statement.executeQuery();

    //             if (resultSet.next()) {
    //                 return new User(resultSet.getString("username"), resultSet.getString("password"));
    //             }
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    //     return null;
    // }

    private User authenticateUser(String username, String password) {
        User user = User.loadFromDatabase(username); // Load user from the database
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }



    private void updateUIAfterLogin() {
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        movieList.setEnabled(true);
        theaterComboBox.setEnabled(true);
        seatComboBox.setEnabled(true);
    }

    private Movie findMovieByName(String name) {
        for (Movie movie : movies) {
            if (movie.getName().equals(name)) {
                return movie;
            }
        }
        return null;
    }


    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            MovieTicketBookingSystem bookingSystem = new MovieTicketBookingSystem();
            //bookingSystem.createAndShowGUI();
        });
    }
}

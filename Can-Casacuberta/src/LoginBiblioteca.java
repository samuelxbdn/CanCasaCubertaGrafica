import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LoginBiblioteca extends JFrame {
    private JTextField Nom, Contrasenya;
    private JButton Login, Signin, Afegir;

    public LoginBiblioteca() {
        setTitle("Biblioteca Can Casacuberta");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel Formulari = new JPanel(new GridLayout(2, 2));
        Formulari.add(new JLabel("Nom:"));
        Nom = new JTextField();
        Formulari.add(Nom);
        Formulari.add(new JLabel("Contrasenya:"));
        Contrasenya = new JPasswordField();
        Formulari.add(Contrasenya);


        Login = new JButton("Accedir");
        Signin = new JButton("Registrar-se");

        JPanel Boto = new JPanel();
        Boto.add(Login);
        Boto.add(Signin);

        setLayout(new BorderLayout());
        add(Formulari, BorderLayout.NORTH);
        add(Boto, BorderLayout.SOUTH);

        Signin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                afegirContacte();
            }
        });

        Login.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ComprobarUsuari(Nom.getText(), Contrasenya.getText());
            }
        });
    }



    private void ComprobarUsuari(String Nom,String Contrasenya) {
        boolean UsuariCorrecte = false;
        int ID_UsuariDB = 0;
        String RolDB = "";
        try (Connection con = DatabaseConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Usuaris")) {
            while (rs.next()) {
                    String nomDB = rs.getString("Nom");
                    String contrasenyaDB = rs.getString("Contrasenya");
                    ID_UsuariDB = rs.getInt("ID_Usuari");
                    RolDB = rs.getString("Rol");
                    if (nomDB.equals(Nom) && contrasenyaDB.equals(Contrasenya)) {
                        UsuariCorrecte = true;
                        ID_UsuariDB = rs.getInt("ID_Usuari");
                        RolDB = rs.getString("Rol");
                    break;
                    }
            }
            if (UsuariCorrecte==true){
                Llibre llib = new Llibre(ID_UsuariDB, RolDB);
                llib.setVisible(true);
                dispose(); 
            }else{
                JOptionPane.showMessageDialog(this, "Usuari Incorrecte.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "No s'ha pogut conectar amb la base de dades.");
        }
    }

    private void afegirContacte() {
    JFrame LayoutRegistre = new JFrame("Registra't");
    LayoutRegistre.setSize(400, 200);
    LayoutRegistre.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    LayoutRegistre.setLocationRelativeTo(null);

    JPanel Registre = new JPanel(new GridLayout(5, 2));
    Registre.add(new JLabel("Nom:"));
    JTextField nom = new JTextField();
    Registre.add(nom);
    Registre.add(new JLabel("Cognoms:"));
    JTextField cognoms = new JTextField();
    Registre.add(cognoms);
    Registre.add(new JLabel("Contrasenya:"));
    JTextField contrasenya = new JPasswordField();
    Registre.add(contrasenya);
    Registre.add(new JLabel("Email:"));
    JTextField email = new JTextField();
    Registre.add(email);
    Registre.add(new JLabel("Telefon:"));
    JTextField telefon = new JTextField();
    Registre.add(telefon);

    Afegir = new JButton("Afegir");
    JPanel Boto = new JPanel();
    Boto.add(Afegir);


    LayoutRegistre.setLayout(new BorderLayout());
    LayoutRegistre.add(Registre, BorderLayout.NORTH);
    LayoutRegistre.add(Boto, BorderLayout.SOUTH);

    Afegir.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        String Nom = nom.getText();
        String Cognoms = cognoms.getText();
        String Contrasenya = contrasenya.getText();
        String Email = email.getText();
        String Telefon = telefon.getText();
        LocalDate data_creacio = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedcreacio = data_creacio.format(formatter);

        String query = "INSERT INTO Usuaris (Nom, Cognoms, Contrasenya, Email, Telefon, Rol, Data_Registre) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, Nom);
            pst.setString(2, Cognoms);
            pst.setString(3, Contrasenya);
            pst.setString(4, Email);
            pst.setString(5, Telefon);
            pst.setString(7, formattedcreacio);
            if(Email.equals("bibliotecaris@cancasacuberta.com")){
                pst.setString(6, "Bibliotecari");
            }else{
                pst.setString(6, "Lector");
            }
            
            pst.executeUpdate();
            LayoutRegistre.dispose();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
            });
            LayoutRegistre.setVisible(true);
        };
    


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LoginBiblioteca().setVisible(true);
            }
        });
    }
}

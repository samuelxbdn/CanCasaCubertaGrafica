import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class Llibre extends JFrame {
    private JTable jTable;
    private DefaultTableModel tableModel;
    private JTextField idLlibreField, titolField, autorField, isbnField, editorialField, anyPublicacioField, categoriaField, estatField;
    private JButton afegirButton, modificarButton, esborrarButton, veureButton, prestecButton, retornarButton;
    private boolean Bibliotecari = true; 

    public Llibre(int ID_Usuari, String Rol) {
        if (Rol.equals("Lector")) {
            Bibliotecari = false;
            String query = "SELECT Multa FROM Prestecs WHERE ID_Usuari = ?";
            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, ID_Usuari); 
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    int pagar = rs.getInt("Multa"); 
                    if(pagar > 0){
                        JOptionPane.showMessageDialog(this, "Tens una multa per tardar en tornar llibres de: " + pagar +"€. Apropa't a la biblioteca per pagar.");

                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "No s'ha pogut conectar amb la base de dades.");
            }
        }


        setTitle("Llistat de la biblioteca");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columnNames = {"ID_Llibre", "Titol", "Autor", "ISBN", "Editorial", "Any_publicacio", "Categoria", "Estat"};
        tableModel = new DefaultTableModel(columnNames, 0);
        jTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(jTable);
        JPanel buttonPanel = new JPanel();

        if(Bibliotecari == true){
            JPanel formPanel = new JPanel(new GridLayout(8, 2));
            formPanel.add(new JLabel("ID_Llibre:"));
            idLlibreField = new JTextField();
            formPanel.add(idLlibreField);
            formPanel.add(new JLabel("Titol:"));
            titolField = new JTextField();
            formPanel.add(titolField);
            formPanel.add(new JLabel("Autor:"));
            autorField = new JTextField();
            formPanel.add(autorField);
            formPanel.add(new JLabel("ISBN:"));
            isbnField = new JTextField();
            formPanel.add(isbnField);
            formPanel.add(new JLabel("Editorial:"));
            editorialField = new JTextField();
            formPanel.add(editorialField);
            formPanel.add(new JLabel("Any_publicacio:"));
            anyPublicacioField = new JTextField();
            formPanel.add(anyPublicacioField);
            formPanel.add(new JLabel("Categoria:"));
            categoriaField = new JTextField();
            formPanel.add(categoriaField);
            formPanel.add(new JLabel("Estat:"));
            estatField = new JTextField();
            formPanel.add(estatField);

            afegirButton = new JButton("Afegir");
            modificarButton = new JButton("Modificar");
            esborrarButton = new JButton("Esborrar");
            veureButton = new JButton("Veure");

            buttonPanel.add(afegirButton);
            buttonPanel.add(modificarButton);
            buttonPanel.add(esborrarButton);
            buttonPanel.add(veureButton);

            setLayout(new BorderLayout());
            add(scrollPane, BorderLayout.CENTER);
            add(formPanel, BorderLayout.NORTH);
            add(buttonPanel, BorderLayout.SOUTH);

            afegirButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    afegirLlibre();
                }
            });

        modificarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                modificarLlibre();
            }
        });

        esborrarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                esborrarLlibre();
            }
        });

        veureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                veureLlibre();
            }
            });

        }else{
            prestecButton = new JButton("Préstec");
            retornarButton = new JButton("Retornar");
            buttonPanel.add(prestecButton);
            buttonPanel.add(retornarButton);

            setLayout(new BorderLayout());
            add(scrollPane, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            prestecButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ferPrestec(ID_Usuari);
                }
            });
            retornarButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    retornarPrestec(ID_Usuari);
                }
            });

        }

        carregarLlibres();
    }

    private void carregarLlibres() {
        try (Connection con = DatabaseConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM biblioteca")) {
            tableModel.setRowCount(0);
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("ID_Llibre"), rs.getString("titol"), rs.getString("autor"),
                        rs.getString("ISBN"), rs.getString("editorial"),
                        rs.getInt("any_publicacio"), rs.getString("categoria"), rs.getString("estat")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "No s'ha pogut conectar amb la base de dades.");
        }
    }

    private void afegirLlibre() {
        String titol = titolField.getText();
        String autor = autorField.getText();
        String ISBN = isbnField.getText();
        String editorial = editorialField.getText();
        int anyPublicacio = Integer.parseInt(anyPublicacioField.getText());
        String categoria = categoriaField.getText();
        String estat = estatField.getText();

        String query = "INSERT INTO biblioteca (titol, autor, ISBN, editorial, any_publicacio, categoria, Estat) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, titol);
            pst.setString(2, autor);
            pst.setString(3, ISBN);
            pst.setString(4, editorial);
            pst.setInt(5, anyPublicacio);
            pst.setString(6, categoria);
            pst.setString(7, estat);

            pst.executeUpdate();
            carregarLlibres();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "No s'ha pogut conectar amb la base de dades.");
        }
    }

    private void modificarLlibre() {
        int linia = jTable.getSelectedRow();
        if (linia >= 0) {
            int idLlibre = (int) tableModel.getValueAt(linia, 0);
            String titol = titolField.getText();
            String autor = autorField.getText();
            String ISBN = isbnField.getText();
            String editorial = editorialField.getText();
            int anyPublicacio = Integer.parseInt(anyPublicacioField.getText());
            String categoria = categoriaField.getText();
            String estat = estatField.getText();

            String query = "UPDATE biblioteca SET titol = ?, autor = ?, ISBN = ?, editorial = ?, any_publicacio = ?, categoria = ?, Estat = ? WHERE ID_Llibre = ?";
            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(query)) {
                pst.setString(1, titol);
                pst.setString(2, autor);
                pst.setString(3, ISBN);
                pst.setString(4, editorial);
                pst.setInt(5, anyPublicacio);
                pst.setString(6, categoria);
                pst.setString(7, estat);
                pst.setInt(8, idLlibre);

                pst.executeUpdate();
                carregarLlibres();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "No s'ha pogut conectar amb la base de dades.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona un llibre per modificar.");
        }
    }

    private void esborrarLlibre() {
        int linia = jTable.getSelectedRow();
        if (linia >= 0) {
            int idLlibre = (int) tableModel.getValueAt(linia, 0);
            String query = "DELETE FROM biblioteca WHERE ID_Llibre = ?";
            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, idLlibre);
                pst.executeUpdate();
                carregarLlibres();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "No s'ha pogut conectar amb la base de dades.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona un llibre per esborrar.");
        }
    }

    private void veureLlibre() {
        int linia = jTable.getSelectedRow();
        if (linia >= 0) {
            int idLlibre = (int) tableModel.getValueAt(linia, 0);
            String query = "SELECT * FROM biblioteca WHERE ID_Llibre = ?";
            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, idLlibre);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        idLlibreField.setText(String.valueOf(rs.getInt("ID_Llibre")));
                        titolField.setText(rs.getString("titol"));
                        autorField.setText(rs.getString("autor"));
                        isbnField.setText(rs.getString("ISBN"));
                        editorialField.setText(rs.getString("editorial"));
                        anyPublicacioField.setText(String.valueOf(rs.getInt("any_publicacio")));
                        categoriaField.setText(rs.getString("categoria"));
                        estatField.setText(rs.getString("estat"));
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "No s'ha pogut conectar amb la base de dades.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona un llibre per veure.");
        }
    }

    private void ferPrestec(int ID_User) {
        int linia = jTable.getSelectedRow();
        String estat = (String)tableModel.getValueAt(linia,7);
        if (linia >= 0 && estat.equals("disponible")) {
            LocalDate data_prestec = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedprestec = data_prestec.format(formatter);
            LocalDate data_retorn = LocalDate.now().plusDays(14);
            String formattedretorn = data_retorn.format(formatter);
            int id_llibre = (int) tableModel.getValueAt(linia, 0); 
            String query = "INSERT INTO Prestecs (ID_Llibre, ID_Usuari, Data_Prestec, Data_Retorn_Prevista, Estat) VALUES (?, ?, ?, ?, ?)";
            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(query)) {
                pst.setInt(1, id_llibre);
                pst.setInt(2, ID_User);
                pst.setString(3, formattedprestec);
                pst.setString(4, formattedretorn);
                pst.setString(5, "actiu");
                pst.executeUpdate();
                carregarLlibres(); 
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "No s'ha pogut conectar amb la base de dades.");
            } 

            String canvi = "UPDATE biblioteca SET Estat = ? WHERE ID_Llibre = ?";
            try (Connection conupdate = DatabaseConnection.getConnection();
                 PreparedStatement pst = conupdate.prepareStatement(canvi)) {
                pst.setString(1, "prestat");
                pst.setInt(2, id_llibre);
                pst.executeUpdate();
                carregarLlibres(); 

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "No s'ha pogut conectar amb la base de dades.");
            }

            JOptionPane.showMessageDialog(this, "Préstec realitzat amb exit l'has de retornar abans de: "+formattedretorn);


        }else {
            JOptionPane.showMessageDialog(this, "Selecciona un llibre disponible per sol·licitar el préstec.");
        }
    }

    private void retornarPrestec(int ID_User) {
        int linia = jTable.getSelectedRow();
        if (linia >= 0) {
            int id_llibre = (int) tableModel.getValueAt(linia, 0);
            String estat = (String) tableModel.getValueAt(linia, 7);

            if ("prestat".equals(estat)) {
                String query = "SELECT Data_Retorn_Prevista, ID_Usuari FROM Prestecs WHERE ID_Llibre = ? AND Estat = ?";
                try (Connection con = DatabaseConnection.getConnection();
                     PreparedStatement pst = con.prepareStatement(query)) {
                    pst.setInt(1, id_llibre);
                    pst.setString(2, "actiu");
                    try (ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) {
                            String retorn_prevista = rs.getString("Data_Retorn_Prevista");
                            int id_usuari_prestec = rs.getInt("ID_Usuari");

                            if (id_usuari_prestec == ID_User) {
                                LocalDate data_retorn = LocalDate.now();
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                String formattedretorn = data_retorn.format(formatter);

                                String updatePrestec = "UPDATE Prestecs SET Data_Retorn_Real = ?, Estat = ? WHERE ID_Llibre = ? AND ID_Usuari = ?";
                                try (Connection conUpdate = DatabaseConnection.getConnection();
                                     PreparedStatement pstUpdate = conUpdate.prepareStatement(updatePrestec)) {
                                    pstUpdate.setString(1, formattedretorn);
                                    pstUpdate.setString(2, "completa");
                                    pstUpdate.setInt(3, id_llibre);
                                    pstUpdate.setInt(4, ID_User);
                                    pstUpdate.executeUpdate();
                                }


                                String prevista_junt = retorn_prevista.replaceAll("-", "");
                                String retorn_junt = formattedretorn.replaceAll("-", "");
                                int prevista_num = Integer.parseInt(prevista_junt);
                                int retorn_num = Integer.parseInt(retorn_junt);

                                if (retorn_num > prevista_num) {
                                    double multa = (retorn_num - prevista_num) * 0.5;
                                    String updateMulta = "UPDATE Prestecs SET Multa = ? WHERE ID_Llibre = ? AND ID_Usuari = ?";
                                    try (Connection conMulta = DatabaseConnection.getConnection();
                                         PreparedStatement pstMulta = conMulta.prepareStatement(updateMulta)) {
                                        pstMulta.setDouble(1, multa);
                                        pstMulta.setInt(2, id_llibre);
                                        pstMulta.setInt(3, ID_User);
                                        pstMulta.executeUpdate();
                                        JOptionPane.showMessageDialog(this, "Llibre retornat correctament. Tens una multa de: " + multa + "€");
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(this, "Llibre retornat correctament.");
                                }

                                String updateLlibre = "UPDATE biblioteca SET Estat = ? WHERE ID_Llibre = ?";
                                try (Connection conUpdateLlibre = DatabaseConnection.getConnection();
                                     PreparedStatement pstUpdateLlibre = conUpdateLlibre.prepareStatement(updateLlibre)) {
                                    pstUpdateLlibre.setString(1, "disponible");
                                    pstUpdateLlibre.setInt(2, id_llibre);
                                    pstUpdateLlibre.executeUpdate();
                                }

                                carregarLlibres();
                            } else {
                                JOptionPane.showMessageDialog(this, "Aquest llibre no està prestat per tu.");
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "No s'ha trobat cap préstec actiu per a aquest llibre.");
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "No s'ha pogut conectar amb la base de dades.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecciona un llibre prestat per retornar.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona un llibre prestat per retornar.");
        }
    }
}



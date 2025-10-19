/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Dialogue;

/**
 *
 * @author LEGION
 */
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Dialog extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Dialog.class.getName());

    private boolean confirmed = false;
    private String operationType;

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/PBO_Praktikum_5";
    private static final String USER = "postgres";
    private static final String PASS = "0000";
    private Connection conn;

    public static enum Operation {
        INSERT, UPDATE, DELETE, VIEW
    }

    public Dialog() {
        initComponents();
        setupDialogProperties();
        connectToDatabase();
    }

    public Dialog(String operation, String nomorSeri, String jenis, String merek, String nama, String model) {
        initComponents();
        setupDialogProperties();
        connectToDatabase();
        setOperation(operation);
        setFieldValues(nomorSeri, jenis, merek, nama, model);
    }

    public Dialog(String operation, String nomorSeri) {
        this(operation, nomorSeri, null, null, null, null);
    }

    private void connectToDatabase() {
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Koneksi database berhasil dari Dialog!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal terhubung ke database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupDialogProperties() {
        setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        setTitle("Dialog CRUD - Pilih Operasi");
    }

    public void setFieldValues(String nomorSeri, String jenis, String merek, String nama, String model) {
        jTextField1.setText(nomorSeri != null ? nomorSeri : "");
        jTextField2.setText(jenis != null ? jenis : "");
        jTextField3.setText(merek != null ? merek : "");
        jTextField4.setText(nama != null ? nama : "");
        jTextField5.setText(model != null ? model : "");

        updateFieldStates();
    }

    public void setOperation(String operation) {
        this.operationType = operation;
        updateTitle();
        updateFieldStates();
    }

    private void updateTitle() {
        if (operationType != null) {
            setTitle("Dialog CRUD - " + operationType);
            jToggleButton1.setText(operationType.equals("DELETE") ? "Hapus" : "Simpan");
        }
    }

    private void updateFieldStates() {
        boolean enabled = !"DELETE".equals(operationType);

        jTextField1.setEnabled(!"DELETE".equals(operationType) && !"UPDATE".equals(operationType));
        jTextField2.setEnabled(enabled);
        jTextField3.setEnabled(enabled);
        jTextField4.setEnabled(enabled);
        jTextField5.setEnabled(enabled);

        if ("DELETE".equals(operationType)) {
            jLabel1.setText("Nomor Seri yang akan dihapus:");
            jLabel2.setVisible(false);
            jLabel3.setVisible(false);
            jLabel4.setVisible(false);
            jLabel5.setVisible(false);
            jTextField2.setVisible(false);
            jTextField3.setVisible(false);
            jTextField4.setVisible(false);
            jTextField5.setVisible(false);
        } else {
            jLabel1.setText("Masukkan nomor seri");
            jLabel2.setVisible(true);
            jLabel3.setVisible(true);
            jLabel4.setVisible(true);
            jLabel5.setVisible(true);
            jTextField2.setVisible(true);
            jTextField3.setVisible(true);
            jTextField4.setVisible(true);
            jTextField5.setVisible(true);

            // Tambahkan tooltip untuk UPDATE
            if ("UPDATE".equals(operationType)) {
                jTextField1.setToolTipText("Nomor seri tidak dapat diubah (primary key)");
                jTextField5.setToolTipText("Model perangkat dapat diubah asal tidak duplikat");
            }
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getOperationType() {
        return operationType;
    }

    public String getNomorSeri() {
        return jTextField1.getText().trim();
    }

    public String getJenisPerangkat() {
        return jTextField2.getText().trim();
    }

    public String getMerekPerangkat() {
        return jTextField3.getText().trim();
    }

    public String getNamaPerangkat() {
        return jTextField4.getText().trim();
    }

    public String getModelPerangkat() {
        return jTextField5.getText().trim();
    }

    public String[] getInputData() {
        return new String[]{
            getNomorSeri(),
            getJenisPerangkat(),
            getMerekPerangkat(),
            getNamaPerangkat(),
            getModelPerangkat()
        };
    }

    private boolean validateInput() {
        if (operationType == null) {
            JOptionPane.showMessageDialog(this, "Pilih operasi terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (getNomorSeri().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nomor Seri harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            jTextField1.requestFocus();
            return false;
        }

        if (operationType.equals("INSERT") || operationType.equals("UPDATE")) {
            if (getJenisPerangkat().isEmpty() || getMerekPerangkat().isEmpty()
                    || getNamaPerangkat().isEmpty() || getModelPerangkat().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field harus diisi untuk INSERT/UPDATE!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }

        return true;
    }

    private boolean isNomorSeriExists(String nomorSeri) {
        try {
            String query = "SELECT COUNT(*) FROM penjualan_perangkat_elektronik WHERE nomor_seri = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, nomorSeri);
            var rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isPerangkatDuplicate(String merek, String nama, String model) {
        try {
            String query = "SELECT COUNT(*) FROM penjualan_perangkat_elektronik "
                    + "WHERE merek_perangkat = ? AND nama_perangkat = ? AND model_perangkat = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, merek);
            pstmt.setString(2, nama);
            pstmt.setString(3, model);

            var rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isPerangkatDuplicateForUpdate(String merek, String nama, String model, String currentNomorSeri) {
        try {

            String query = "SELECT COUNT(*) FROM penjualan_perangkat_elektronik "
                    + "WHERE merek_perangkat = ? AND nama_perangkat = ? AND model_perangkat = ? "
                    + "AND nomor_seri != ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, merek);
            pstmt.setString(2, nama);
            pstmt.setString(3, model);
            pstmt.setString(4, currentNomorSeri); // Exclude data yang sedang diupdate

            var rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getCurrentModel(String nomorSeri) {
        try {
            String query = "SELECT model_perangkat FROM penjualan_perangkat_elektronik WHERE nomor_seri = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, nomorSeri);

            var rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("model_perangkat");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    private boolean executeInsert() {
        try {

            if (isNomorSeriExists(getNomorSeri())) {
                JOptionPane.showMessageDialog(this, "Nomor seri sudah ada dalam database!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (isPerangkatDuplicate(getMerekPerangkat(), getNamaPerangkat(), getModelPerangkat())) {
                JOptionPane.showMessageDialog(this,
                        "Perangkat dengan spesifikasi ini sudah ada!\n\n"
                        + "Merek: " + getMerekPerangkat() + "\n"
                        + "Nama: " + getNamaPerangkat() + "\n"
                        + "Model: " + getModelPerangkat() + "\n\n"
                        + "Silakan gunakan data yang berbeda.",
                        "Data Duplikat", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            String query = "INSERT INTO penjualan_perangkat_elektronik "
                    + "(nomor_seri, jenis_perangkat, merek_perangkat, nama_perangkat, model_perangkat, warna, tahun_rilis, harga, stok) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, getNomorSeri());
            pstmt.setString(2, getJenisPerangkat());
            pstmt.setString(3, getMerekPerangkat());
            pstmt.setString(4, getNamaPerangkat());
            pstmt.setString(5, getModelPerangkat());
            pstmt.setString(6, "Default");
            pstmt.setInt(7, 2023);
            pstmt.setLong(8, 0);
            pstmt.setInt(9, 0);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Data berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menambahkan data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return false;
    }

    private boolean executeUpdate() {
        try {

            String oldModel = getCurrentModel(getNomorSeri());

            boolean modelChanged = !getModelPerangkat().equals(oldModel);

            if (modelChanged && isPerangkatDuplicateForUpdate(getMerekPerangkat(), getNamaPerangkat(), getModelPerangkat(), getNomorSeri())) {
                JOptionPane.showMessageDialog(this,
                        "Model perangkat '" + getModelPerangkat() + "' sudah digunakan!\n\n"
                        + "Perangkat dengan model ini sudah ada dalam database.\n"
                        + "Silakan gunakan model yang berbeda.",
                        "Model Duplikat", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            if (isPerangkatDuplicateForUpdate(getMerekPerangkat(), getNamaPerangkat(), getModelPerangkat(), getNomorSeri())) {
                JOptionPane.showMessageDialog(this,
                        "Perangkat dengan spesifikasi ini sudah ada!\n\n"
                        + "Merek: " + getMerekPerangkat() + "\n"
                        + "Nama: " + getNamaPerangkat() + "\n"
                        + "Model: " + getModelPerangkat() + "\n\n"
                        + "Silakan gunakan data yang berbeda.",
                        "Data Duplikat", JOptionPane.WARNING_MESSAGE);
                return false;
            }

            String query = "UPDATE penjualan_perangkat_elektronik SET "
                    + "jenis_perangkat = ?, merek_perangkat = ?, nama_perangkat = ?, model_perangkat = ? "
                    + "WHERE nomor_seri = ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, getJenisPerangkat());
            pstmt.setString(2, getMerekPerangkat());
            pstmt.setString(3, getNamaPerangkat());
            pstmt.setString(4, getModelPerangkat());
            pstmt.setString(5, getNomorSeri());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Data berhasil diupdate!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Data tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mengupdate data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    private boolean executeDelete() {
        try {
            if (!isNomorSeriExists(getNomorSeri())) {
                JOptionPane.showMessageDialog(this, "Data dengan nomor seri tersebut tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            String query = "DELETE FROM penjualan_perangkat_elektronik WHERE nomor_seri = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, getNomorSeri());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus data!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    private boolean confirmDelete() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin menghapus data dengan nomor seri: " + getNomorSeri() + "?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }

    public void clearForm() {
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");
        jTextField5.setText("");
        confirmed = false;
    }

    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 153, 110));

        jLabel1.setText("Masukkan nomer seri");

        jLabel2.setText("Masukkan jenis perangkat");

        jLabel3.setText("Masukkan merek perangkat");

        jLabel4.setText("Masukkan nama perangkat");

        jLabel5.setText("Masukkan model perangkat");

        jToggleButton1.setText("Enter");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        jTextField5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jTextField1)
                        .addComponent(jTextField2)
                        .addComponent(jTextField3)
                        .addComponent(jTextField4)
                        .addComponent(jTextField5, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jToggleButton1)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jTextField5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField5ActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        // TODO add your handling code here:
        if (!validateInput()) {
            return;
        }

        boolean success = false;

        switch (operationType) {
            case "INSERT":
                success = executeInsert();
                break;
            case "UPDATE":
                success = executeUpdate();
                break;
            case "DELETE":
                if (confirmDelete()) {
                    success = executeDelete();
                } else {
                    return;
                }
                break;
            default:
                JOptionPane.showMessageDialog(this, "Operasi tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
        }

        if (success) {
            confirmed = true;
            dispose();
        }
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        closeConnection();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JToggleButton jToggleButton1;
    // End of variables declaration//GEN-END:variables
}

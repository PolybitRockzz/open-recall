import data.Info;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class App {

    public static void main(String[] args) throws Exception {
      try {
        App app = new App();

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        if(!SystemTray.isSupported()) {
          JOptionPane.showMessageDialog(
            null,
            "Sorry, looks like this operating system does not support a native\nSystem Tray. Either update your current system, or wait further\ntill we introduce a version of the app that doesn't rely on a native\nSystem Tray.",
            "System Tray Not Supported",
            JOptionPane.INFORMATION_MESSAGE);
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
              Desktop.getDesktop().browse(new URI("https://github.com/PolybitRockzz/open-recall"));
            }
            System.exit(0);
        }

        if(!app.isAppInitialized()) {
          JOptionPane.showMessageDialog(null, "SHA256 public and private keys not found! Looks like this is a\nnew installation, so we will provide you with auto-generated keys.", "Fresh Installation, Perhaps?", JOptionPane.INFORMATION_MESSAGE);
          app.writeNewKeys();
          app.showAndValidateKeys();
        }

        if(app.areKeysHealthy()) {
          System.out.println("Public & private keys are healthy!");
        } else {
          JOptionPane.showMessageDialog(null, "Your public & private keys don't match! We suggest fixing any\npossible file corruption, or resetting the key pair.", "Key Pair Error!", JOptionPane.ERROR_MESSAGE);
          System.exit(0);
        }

        // Run the actual thread
        Recall thread = new Recall();
        thread.start();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    public boolean isAppInitialized() {
      File appDir = new File(Info.getAppdataDir() + "\\OpenRecall");
      if (!appDir.exists()) { appDir.mkdirs(); return false; }
      File publicKeyDir = new File(appDir.getAbsolutePath() + "\\public_sha256.txt");
      if (!publicKeyDir.exists()) {
        try { publicKeyDir.createNewFile(); return false; } catch (IOException e) { e.printStackTrace(); }
      }
      // File privateKeyDir = new File(appDir.getAbsolutePath() + "\\private_sha256.txt");
      // if (!privateKeyDir.exists()) {
      //   try { privateKeyDir.createNewFile(); return false; } catch (IOException e) { e.printStackTrace(); }
      // }
      return true;
    }

    public void writeNewKeys() throws Exception {
      java.security.KeyPair keyPair = Info.generateKeyPair();
      PublicKey publicKey = keyPair.getPublic();
      PrivateKey privateKey = keyPair.getPrivate();
      String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
      String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
      try (FileWriter writer = new FileWriter(Info.getAppdataDir() + "\\OpenRecall\\public_sha256.txt")) {
        writer.write(publicKeyStr);
        writer.close();
      } catch (IOException e) { e.printStackTrace(); }
      try (FileWriter writer = new FileWriter(Info.getAppdataDir() + "\\OpenRecall\\private_sha256.txt")) {
        writer.write(privateKeyStr);
        writer.close();
      } catch (IOException e) { e.printStackTrace(); }
    }

    public void showAndValidateKeys() throws Exception {
      Scanner sc1 = new Scanner(new File(Info.getAppdataDir() + "\\OpenRecall\\public_sha256.txt"));
      String publicHash = sc1.nextLine();
      sc1.close();
      Scanner sc2 = new Scanner(new File(Info.getAppdataDir() + "\\OpenRecall\\private_sha256.txt"));
      String privateHash = sc2.nextLine();
      sc2.close();

      JDialog frame = new JDialog();
      frame.setTitle("Validate Key Pairs");
      frame.setSize(520, 300);
      frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
      frame.setLocationRelativeTo(null);
      frame.getContentPane().setLayout(null);

      JLabel publicLabelTitle = new JLabel();
      publicLabelTitle.setText("Public Hash: ");
      publicLabelTitle.setFont(new Font("Arial", Font.BOLD, 14));
      publicLabelTitle.setBounds(20, 20, 100, 20);
      publicLabelTitle.setAlignmentX(JLabel.CENTER_ALIGNMENT);
      publicLabelTitle.setAlignmentY(JLabel.CENTER_ALIGNMENT);
      frame.getContentPane().add(publicLabelTitle);

      JLabel publicLabel = new JLabel();
      publicLabel.setText(publicHash);
      publicLabel.setFont(new Font("Arial", Font.PLAIN, 14));
      publicLabel.setBounds(20, 50, 460, 20);
      publicLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
      publicLabel.setAlignmentY(JLabel.CENTER_ALIGNMENT);
      frame.getContentPane().add(publicLabel);

      JLabel privateLabelTitle = new JLabel();
      privateLabelTitle.setText("Private Hash: ");
      privateLabelTitle.setFont(new Font("Arial", Font.BOLD, 14));
      privateLabelTitle.setBounds(20, 90, 100, 20);
      privateLabelTitle.setAlignmentX(JLabel.CENTER_ALIGNMENT);
      privateLabelTitle.setAlignmentY(JLabel.CENTER_ALIGNMENT);
      frame.getContentPane().add(privateLabelTitle);

      JLabel privateLabel = new JLabel();
      String censoredText = "";
      for (int i = 0; i < privateHash.length(); i++) { censoredText += "*"; }
      String censoredFinalText = censoredText;
      privateLabel.setText(censoredText);
      privateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
      privateLabel.setBounds(20, 120, 460, 20);
      privateLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
      privateLabel.setAlignmentY(JLabel.CENTER_ALIGNMENT);
      frame.getContentPane().add(privateLabel);

      JButton showPrivateLabel = new JButton();
      showPrivateLabel.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          if(privateLabel.getText().charAt(0) == '*' && privateLabel.getText().charAt(1) == '*' && privateLabel.getText().charAt(2) == '*') {
            privateLabel.setText(privateHash);
          } else {
            privateLabel.setText(censoredFinalText);
          }
        }
        
      });
      showPrivateLabel.setText("Show");
      showPrivateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
      privateLabel.setBounds(20, 120, 460 - (int) showPrivateLabel.getPreferredSize().getWidth(), 20);
      showPrivateLabel.setBounds(480 - (int) showPrivateLabel.getPreferredSize().getWidth(), 120, (int) showPrivateLabel.getPreferredSize().getWidth(), 20);
      showPrivateLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
      showPrivateLabel.setAlignmentY(JLabel.CENTER_ALIGNMENT);
      showPrivateLabel.setFocusable(false);
      frame.getContentPane().add(showPrivateLabel);

      JButton copyPublicKeyButton = new JButton();
      copyPublicKeyButton.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          Info.copyToClipboard(publicHash);
        }
        
      });
      copyPublicKeyButton.setText("Copy Public");
      copyPublicKeyButton.setFont(new Font("Arial", Font.PLAIN, 14));
      copyPublicKeyButton.setBounds(20, 160, 140, 30);
      copyPublicKeyButton.setAlignmentX(JLabel.CENTER_ALIGNMENT);
      copyPublicKeyButton.setAlignmentY(JLabel.CENTER_ALIGNMENT);
      copyPublicKeyButton.setFocusable(false);
      frame.getContentPane().add(copyPublicKeyButton);

      JButton copyPrivateKeyButton = new JButton();
      copyPrivateKeyButton.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          Info.copyToClipboard(privateHash);
        }
        
      });
      copyPrivateKeyButton.setText("Copy Private");
      copyPrivateKeyButton.setFont(new Font("Arial", Font.PLAIN, 14));
      copyPrivateKeyButton.setBounds(180, 160, 140, 30);
      copyPrivateKeyButton.setAlignmentX(JLabel.CENTER_ALIGNMENT);
      copyPrivateKeyButton.setAlignmentY(JLabel.CENTER_ALIGNMENT);
      copyPrivateKeyButton.setFocusable(false);
      frame.getContentPane().add(copyPrivateKeyButton);

      JButton copyBothButton = new JButton();
      copyBothButton.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          Info.copyToClipboard("--- START PUBLIC HASH ---\n" + publicHash + "\n--- END PUBLIC HASH ---\n--- START PRIVATE HASH ---\n" + privateHash + "\n--- END PRIVATE HASH ---");
        }
        
      });
      copyBothButton.setText("Copy Both");
      copyBothButton.setFont(new Font("Arial", Font.PLAIN, 14));
      copyBothButton.setBounds(340, 160, 140, 30);
      copyBothButton.setAlignmentX(JLabel.CENTER_ALIGNMENT);
      copyBothButton.setAlignmentY(JLabel.CENTER_ALIGNMENT);
      copyBothButton.setFocusable(false);
      frame.getContentPane().add(copyBothButton);

      JCheckBox checkBox = new JCheckBox();
      checkBox.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          if(checkBox.isSelected()) {
            File file = new File(Info.getAppdataDir() + "\\OpenRecall\\private_sha256.txt");
            if (!file.exists()) { try { file.createNewFile(); } catch (Exception e1) { e1.printStackTrace(); } }
            try {
              FileWriter writer = new FileWriter(file);
              writer.write(privateHash);
              writer.close();
            } catch (IOException e2) {
              e2.printStackTrace();
            }
          } else {
            File file = new File(Info.getAppdataDir() + "\\OpenRecall\\private_sha256.txt");
            if (file.exists()) { file.delete(); }
          }
        }
        
      });
      checkBox.setText("Save Private Key (i.e; stay logged in)");
      checkBox.setSelected(true);
      checkBox.setFont(new Font("Arial", Font.PLAIN, 14));
      checkBox.setBounds(20, 220, 300, 20);
      checkBox.setAlignmentX(JLabel.CENTER_ALIGNMENT);
      checkBox.setAlignmentY(JLabel.CENTER_ALIGNMENT);
      checkBox.setFocusable(false);
      frame.getContentPane().add(checkBox);

      frame.setVisible(true);
    }

    public boolean areKeysHealthy() throws Exception {
      String demoText = "Recall, but open-source. Fuck Microsoft.";
      Scanner sc1 = new Scanner(new File(Info.getAppdataDir() + "\\OpenRecall\\public_sha256.txt"));
      String publicHash = sc1.nextLine();
      String encryptedMessage = Info.encrypt(demoText, publicHash);
      sc1.close();
      String privateHash;
      File privateKeyFile = new File(Info.getAppdataDir() + "\\OpenRecall\\private_sha256.txt");
      if (privateKeyFile.exists()) {
        Scanner sc2 = new Scanner(privateKeyFile);
        privateHash = sc2.nextLine();
        sc2.close();
      } else {
        privateHash = JOptionPane.showInputDialog(null,
          "Could not find the private key stored locally. Please enter it here.",
          "Enter Private Key",
          JOptionPane.INFORMATION_MESSAGE);
      }
      String decryptedMessage = Info.decrypt(encryptedMessage, privateHash);
      return demoText.equals(decryptedMessage);
    }

}

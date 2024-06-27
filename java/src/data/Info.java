package data;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

import javax.crypto.Cipher;

public class Info {

  // Icon Lock color: #FFDA5E
  
  // Basic Application Information
  private static String NAME = "Open Recall";
  private static String AUTHOR = "Swastik Biswas";
  private static String VERSION = "0.1";
  private static String PHASE = "BETA";
  private static String APPDATA = System.getenv("APPDATA");

  private static boolean ultraSecure = false;

  private static final SimpleDateFormat FORMAT = new SimpleDateFormat();

  public static final int checkGap = 1;

  public static String getName() {
    return NAME;
  }

  public static String getAuthor() {
    return AUTHOR;
  }

  public static String getVersion() {
    return VERSION;
  }

  public static String getPhase() {
    return PHASE;
  }

  public static String getAppdataDir() {
    return APPDATA;
  }

  public static boolean isUltraSecure() {
    return ultraSecure;
  }

  public static void setUltraSecure(boolean x) {
    ultraSecure = x;
  }

  public static String getTime(String pattern) {
    FORMAT.applyPattern(pattern);
    return FORMAT.format(new Date());
  }

  public static java.security.KeyPair generateKeyPair() throws Exception {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    return keyPairGenerator.generateKeyPair();
  }

  public static String encrypt(String message, String  publicKeyString) throws Exception {
    byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString.getBytes());
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
    PublicKey publicKey =  keyFactory.generatePublic(publicKeySpec);

    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);

    return Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes(StandardCharsets.UTF_8)));
  }

  public static String decrypt(String encryptedMessage, String privateKeyString) throws Exception {
    byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString.getBytes());
    PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");

    PrivateKey privateKey= keyFactory.generatePrivate(privateKeySpec);

    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.DECRYPT_MODE, privateKey);
    return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedMessage)), StandardCharsets.UTF_8);
  }

  public static void copyToClipboard(String text) {
    StringSelection stringSelection = new StringSelection(text);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(stringSelection, null);
  }

}

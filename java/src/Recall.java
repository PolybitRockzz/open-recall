import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.image.BufferedImage;
import java.nio.file.Paths;

import data.Info;

public class Recall extends Thread {

  private TrayIcon trayIcon;

  private boolean isExit = false;

  public Recall() {
    setDaemon(true);
    SystemTray tray = SystemTray.getSystemTray();
    String imgPath = Paths.get(".").toAbsolutePath().normalize().toString();
    Image image = Toolkit.getDefaultToolkit().getImage(imgPath + "\\public\\logo.png");
    PopupMenu popup = new PopupMenu();
    trayIcon = new TrayIcon(image, "OpenRecall", popup);
    trayIcon.setImageAutoSize(true);
    trayIcon.setToolTip("OpenRecall");
    try { tray.add(trayIcon); } catch (Exception e) { e.printStackTrace(); }
  }

  @Override
  public void run() {
    try {
      showNotification("OpenRecall Now Running!", "OpenRecall is now running successfully in the background! Less data to Microsoft. :)");
    } catch (Exception e) {
      e.printStackTrace();
    }
    long unixTime = System.currentTimeMillis();
    long unixTarget = unixTime + (Info.checkGap * 1000L);
    BufferedImage previousBufferedImage = null;
    while(isExit == false) {
      unixTime = System.currentTimeMillis();
      if (unixTime < unixTarget) { continue; }
      unixTime = unixTarget;
      unixTarget = unixTime + (Info.checkGap * 1000L);
      String time = Info.getTime("yyyy-MM-dd HH:mm:ss");
      try {
        BufferedImage rawScreenshot = takeScreenshot();
        BufferedImage truncatedScreenshot = resizeToHalf(rawScreenshot);
        previousBufferedImage = previousBufferedImage == null ? truncatedScreenshot : previousBufferedImage;
        System.out.println(time + " --> " + compareImages(truncatedScreenshot, previousBufferedImage));
        previousBufferedImage = truncatedScreenshot;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    try {
      showNotification("OpenRecall Stopped.", "OpenRecall stopped running in the background!");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void showNotification(String title, String description) throws Exception {
    trayIcon.displayMessage(title, description, MessageType.INFO);
  }

  public BufferedImage takeScreenshot() throws Exception {
    Robot robot = new Robot();
    BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
    return screenShot;
  }

  public BufferedImage resizeToHalf(BufferedImage img) throws Exception {
    int w = img.getWidth();
    int h = img.getHeight();
    BufferedImage dimg = new BufferedImage(w/2, h/2, img.getType());
    Graphics2D g = dimg.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.drawImage(img, 0, 0, w/2, h/2, 0, 0, w, h, null);
    g.dispose();
    return dimg;
  }

  public long compareImages(BufferedImage img1, BufferedImage img2) {
    long difference = 0;
    System.out.println("img1: " + img1.getWidth() + "x" + img1.getHeight());
    System.out.println("img2: " + img2.getWidth() + "x" + img2.getHeight());
    if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
      for (int x = 0; x < img1.getWidth(); x++) {
        for (int y = 0; y < img1.getHeight(); y++) {
          if (img1.getRGB(x, y) != img2.getRGB(x, y))
            difference += 1;
        }
      }
    } else { return -1; }
    return difference;
  }

  public void beforeExit() {
    isExit = true;
  }
  
}

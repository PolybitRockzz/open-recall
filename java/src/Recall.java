import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;

import data.Info;

public class Recall extends Thread {

  private TrayIcon trayIcon;

  private boolean isPaused = false;
  private boolean isExit = false;

  public Recall() {
    setDaemon(true);
    SystemTray tray = SystemTray.getSystemTray();
    String imgPath = Paths.get(".").toAbsolutePath().normalize().toString();
    Image image = Toolkit.getDefaultToolkit().getImage(imgPath + "\\public\\logo.png");
    PopupMenu popup = new PopupMenu();
    MenuItem pauseMenu = new MenuItem("Pause");
    pauseMenu.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (isPaused) {
          isPaused = false;
          pauseMenu.setName("Pause");
        } else {
          isPaused = true;
          pauseMenu.setName("Resume");
        }
      }
      
    });
    popup.add(pauseMenu);
    MenuItem quitMenu = new MenuItem("Quit");
    quitMenu.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        beforeExit();
      }
      
    });
    popup.add(quitMenu);
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
      if (isPaused == true) { continue; }
      unixTime = System.currentTimeMillis();
      if (unixTime < unixTarget) { continue; }
      unixTime = unixTarget;
      unixTarget = unixTime + (Info.checkGap * 1000L);
      String time = Info.getTime("yyyy-MM-dd HH-mm-ss");
      try {
        BufferedImage rawScreenshot = takeScreenshot();
        BufferedImage truncatedScreenshot = resizeToHalf(rawScreenshot);
        previousBufferedImage = previousBufferedImage == null ? truncatedScreenshot : previousBufferedImage;
        double changePercentage =  compareImages(truncatedScreenshot, previousBufferedImage);
        if (changePercentage >= 0.30) {
          System.out.println(time + " --> Saving Screenshot (" + new DecimalFormat("00.00").format(changePercentage) + "%)");
          saveScreenshot(rawScreenshot, time);
          previousBufferedImage = truncatedScreenshot;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    try {
      showNotification("OpenRecall Stopped.", "OpenRecall stopped running in the background!");
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.exit(0);
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
    BufferedImage dimg = new BufferedImage(w/4, h/4, img.getType());
    Graphics2D g = dimg.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.drawImage(img, 0, 0, w/4, h/4, 0, 0, w, h, null);
    g.dispose();
    return dimg;
  }

  public double compareImages(BufferedImage img1, BufferedImage img2) {
    long difference = 0;
    if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
      for (int x = 0; x < img1.getWidth(); x++) {
        for (int y = 0; y < img1.getHeight(); y++) {
          if (img1.getRGB(x, y) != img2.getRGB(x, y))
            difference += 1;
        }
      }
    } else { return -1; }
    return difference / (img1.getWidth()*img1.getHeight()*1.00);
  }

  public void saveScreenshot(BufferedImage img, String filename) throws Exception {
    File dir =  new File(Info.getAppdataDir() + "\\OpenRecall\\screenshots");
    if (!dir.exists()) { dir.mkdirs(); }
    File actualFile =  new File(Info.getAppdataDir() + "\\OpenRecall\\screenshots\\" + filename + ".jpg");
    if (!actualFile.exists()) { actualFile.createNewFile(); }
    ImageIO.write(img, "JPG", new File(Info.getAppdataDir() + "\\OpenRecall\\screenshots\\" + filename + ".jpg"));
  }

  public void addCsvEntry() {

  }

  public void beforeExit() {
    System.out.println("Exiting...");
    isExit = true;
  }

  public void beforePause() {
    System.out.println("Paused...");
    isPaused = true;
  }

  public void afterPause() {
    System.out.println("Resumed...");
    isPaused = false;
  }
  
}

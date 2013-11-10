import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;

public class BitmapSaver {
  public BitmapSaver(final Image bitmap, File sf) {
    String path = sf.getPath();
    File outFile = new File(path.replace(".fnt", ".png"));
    try {
      ImageIO.write(SwingFXUtils.fromFXImage(bitmap, null), "png", outFile);
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }
}

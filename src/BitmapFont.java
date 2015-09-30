import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;

public class BitmapFont extends Application {
  @Override
  public void start(Stage stage) {
    URL url = this.getClass().getClassLoader().getResource("BitmapFont.fxml");
    FXMLLoader fxmlLoader = new FXMLLoader(url, null);
    BorderPane root;
    try {
      root = (BorderPane)fxmlLoader.load();
    } catch(IOException ioe) {
      System.out.println("I/O Exception Cought");
      ioe.printStackTrace();
      return;
    }
    Scene scene = new Scene(root, 960, 600);
    stage.setScene(scene);
    ((BitmapFontController)fxmlLoader.getController()).mStage = stage;
    stage.show();
  }
}

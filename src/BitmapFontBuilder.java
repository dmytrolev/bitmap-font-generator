import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Callback;

public class BitmapFontBuilder extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("ListViewSample");

        FontsList fl = new FontsList();
        fl.loadSystemFonts();

        StackPane root = new StackPane();
        root.getChildren().add(fl);
        stage.setScene(new Scene(root, 640, 480));

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

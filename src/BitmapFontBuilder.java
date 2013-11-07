import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.Shadow;
import javafx.geometry.VPos;
import java.util.Set;

public class BitmapFontBuilder extends Application {
  protected FontsList mFontsList;
  protected TextArea mText;
  protected Canvas mCanvas;
  protected String mFont = "Calibri";

  protected boolean mShowBorder = false;
  protected boolean mFill = false;
  protected boolean mStroke = true;
  protected boolean mEffect = false;

  @Override
  public void start(Stage stage) {
    stage.setTitle("Bitmap Font Generator");

    mFontsList = new FontsList();
    mFontsList.loadSystemFonts();
    mFontsList.getSelectionModel().selectedItemProperty()
      .addListener((ov, oldv, newv) -> {
          mFont = (String)newv;
          drawText();
            });

    mText = new TextArea("dima loves alina");
    mText.setPrefWidth(200);
    mText.setPrefHeight(Double.MAX_VALUE);

    mCanvas = new Canvas(320, 480);
    drawText();

    BorderPane root = new BorderPane();
    root.setLeft(new TitledPane("System Fonts", mFontsList));
    root.setRight(new TitledPane("List symbols you wish to export", mText));
    root.setCenter(new TitledPane("Image", mCanvas));
    root.setPrefWidth(Double.MAX_VALUE);
    root.setPrefHeight(Double.MAX_VALUE);

    stage.setScene(new Scene(root, 960, 600));

    stage.show();
  }

  protected void drawText() {
    GraphicsContext gc = mCanvas.getGraphicsContext2D();
    gc.clearRect(0, 0, 320, 240);
    gc.setFill(Color.WHITE);
    gc.setStroke(Color.BLACK);
    gc.fillRect(0,0,320,320);
    gc.strokeRect(0,0,320,320);
    gc.setFill(Color.RED);
    gc.setFont(Font.font(mFont, FontWeight.BLACK, 40d));
    GlyphBank gb = new GlyphBank();
    gb.setFont(mFont);
    gb.extract(mText.getText());
    Set<GlyphBank.Glyph> chars = gb.glyphs();
    double x = 0; double y = 0;
    for(GlyphBank.Glyph cc : chars) {
      if(x + cc.w > 320) {
        x = 0;
        y += cc.h;
      }
      gc.setTextBaseline(VPos.BOTTOM);
      if(mEffect) {
        gc.setEffect(new Shadow(5, Color.BLACK));
        gc.fillText(cc.c, x, y + cc.h);
        gc.setEffect(null);
      }
      if(mFill) {
        gc.fillText(cc.c, x, y + cc.h);
      }
      if(mStroke) {
        gc.setLineWidth(2d);
        gc.setStroke(Color.BLACK);
        gc.strokeText(cc.c, x, y + cc.h);
      }
      if(mShowBorder) {
        gc.setLineWidth(1d);
        gc.strokeRect(x,y,cc.w,cc.h);
      }
      x += cc.w;
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}

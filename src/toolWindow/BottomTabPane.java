package toolWindow;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import openGLWindow.PTMWindow;
import openGLWindow.PTMWindowLRGB;
import openGLWindow.PTMWindowRGB;


/**
 * Created by Jed on 14-Jun-17.
 */
public class BottomTabPane extends TabPane {

    private RTIViewer rtiViewer;
    private Scene parent;
    private GridPane previewGridPane;
    private TextField fileName;
    private TextField imageWidthBox;
    private TextField imageHeightBox;
    private TextField imageFormat;
    private ImageView imagePreview;
    private BorderPane imageBorderPane;
    private Image defaultImage;

    private VBox vBox;
    private StackPane imageContainerPane;
    private Rectangle previewWindowRect;

    public BottomTabPane(RTIViewer rtiViewer, Scene parent){
        super();
        this.rtiViewer = rtiViewer;
        this.parent = parent;

        defaultImage = new Image("file:rsc/images/exeterUniLogo.jpg");

        createComponenets();
        updateSize(rtiViewer.primaryStage.getWidth(), rtiViewer.primaryStage.getHeight());
    }

    private void createComponenets(){
        Tab previewTab = createPreviewTab();

        Tab bookmarksTab = new Tab("Bookmarks");
        bookmarksTab.setClosable(false);

        Tab saveImageTab = new Tab("Save image");
        saveImageTab.setClosable(false);

        getTabs().addAll(previewTab, bookmarksTab, saveImageTab);
    }


    private Tab createPreviewTab(){
        Tab previewTab = new Tab("Preview");
        previewTab.setClosable(false);

        vBox = new VBox();

        previewGridPane = new GridPane();

        Label fileNameLabel = new Label("File:");
        fileName = new TextField("");
        fileName.setEditable(false);

        Label imageWidthLabel = new Label("Width:");
        imageWidthBox = new TextField("");
        imageWidthBox.setEditable(false);

        Label imageHeightLabel = new Label("Height:");
        imageHeightBox = new TextField("");
        imageHeightBox.setEditable(false);

        Label imageFormatLabel = new Label("Format:");
        imageFormat = new TextField("");
        imageFormat.setEditable(false);

        createImagePreview();

        GridPane.setConstraints(fileNameLabel, 0, 0, 1, 1);
        GridPane.setConstraints(fileName, 1, 0, 5, 1);

        GridPane.setConstraints(imageWidthLabel, 0, 1, 1, 1);
        GridPane.setConstraints(imageWidthBox, 1, 1, 1, 1);

        GridPane.setConstraints(imageHeightLabel, 2, 1, 1, 1);
        GridPane.setConstraints(imageHeightBox, 3, 1, 1, 1);

        GridPane.setConstraints(imageFormatLabel, 4, 1, 1, 1);
        GridPane.setConstraints(imageFormat, 5, 1, 1, 1);

        previewGridPane.getChildren().addAll(fileNameLabel, fileName, imageWidthLabel, imageWidthBox,
                imageHeightLabel, imageHeightBox, imageFormatLabel, imageFormat);

        previewGridPane.setAlignment(Pos.TOP_CENTER);
        previewGridPane.setVgap(5);
        previewGridPane.setHgap(5);

        vBox.getChildren().addAll(previewGridPane, imageBorderPane);
        vBox.setMargin(previewGridPane, new Insets(5, 0, 5, 0));
        previewTab.setContent(vBox);

        return previewTab;
    }


    private void createImagePreview(){
        imageBorderPane = new BorderPane();

        imagePreview = new ImageView();
        imagePreview.setPreserveRatio(true);

        previewWindowRect = new Rectangle(0, 0, 30 ,30);
        previewWindowRect.setFill(Color.TRANSPARENT);
        previewWindowRect.setStrokeWidth(2);

        imageContainerPane = new StackPane(imagePreview, previewWindowRect);
        imageContainerPane.setMinWidth(0);
        imageContainerPane.setMinHeight(0);

        imageBorderPane.setCenter(imageContainerPane);
        imageBorderPane.setMinWidth(0);
        imageBorderPane.setMinHeight(0);

        imagePreview.fitWidthProperty().bind(imageBorderPane.widthProperty());
        imagePreview.fitHeightProperty().bind(imageBorderPane.heightProperty());

        imageContainerPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double normX = (2 * (event.getX() - (imageContainerPane.getWidth() / 2)) / imagePreview.getBoundsInParent().getWidth());
                double normY = (-2 * (event.getY() - (imageContainerPane.getHeight() / 2)) / imagePreview.getBoundsInParent().getHeight());
                System.out.println(normX + ", " + normY);
            }
        });

        setDefaultImage();
    }


    public void setFileText(String text){
        fileName.setText(text);
    }

    public void setWidthText(String text){
        imageWidthBox.setText(text);
    }

    public void setHeightText(String text){
        imageHeightBox.setText(text);
    }

    public void setFormatText(String text){
        imageFormat.setText(text);
    }

    public void setPreviewImage(Image image){
        imagePreview.setImage(image);
        updateSize(rtiViewer.primaryStage.getWidth(), rtiViewer.primaryStage.getHeight());
        imageBorderPane.setStyle("-fx-background-color: #000000;");
        previewWindowRect.setStroke(Color.RED);
    }

    public void setDefaultImage(){
        imagePreview.setImage(defaultImage);
        updateSize(rtiViewer.primaryStage.getWidth(), rtiViewer.primaryStage.getHeight());
        imageBorderPane.setStyle("-fx-background-color: #ffffff;");
        previewWindowRect.setStroke(Color.TRANSPARENT);
    }

    public void updateSize(double width, double height){
        setPrefWidth(width - 20);

        setPrefHeight(height - (getLayoutY() + 45));

        vBox.setPrefWidth(width - 20);
        previewGridPane.setPrefWidth(width - 20);

        imageWidthBox.setPrefWidth(width / 6);
        imageHeightBox.setPrefWidth(width / 6);
        imageFormat.setPrefWidth(width / 6);

        imageBorderPane.setPrefHeight(getPrefHeight() - 45);
    }


    public void updateSelectedWindow(PTMWindow ptmWindow){
        setFileText(ptmWindow.ptmObject.getFileName());
        setWidthText(String.valueOf(ptmWindow.ptmObject.getWidth()));
        setHeightText(String.valueOf(ptmWindow.ptmObject.getHeight()));

        if(ptmWindow instanceof PTMWindowRGB){
            setFormatText("PTM RGB");
        }else if(ptmWindow instanceof PTMWindowLRGB){
            setFormatText("PTM LRGB");
        }

        setPreviewImage(ptmWindow.ptmObject.previewImage);
    }


    public void updateViewportRect(float x, float y, float imageScale){
        imagePreview.setImage(imagePreview.getImage());
        previewWindowRect.setWidth(imagePreview.getBoundsInParent().getWidth() / imageScale);
        previewWindowRect.setHeight(imagePreview.getBoundsInParent().getHeight() / imageScale);
        previewWindowRect.setTranslateX((x / imageScale) * imagePreview.getBoundsInParent().getWidth() / 2);
        previewWindowRect.setTranslateY((-y / imageScale) * imagePreview.getBoundsInParent().getHeight() / 2);
    }
}
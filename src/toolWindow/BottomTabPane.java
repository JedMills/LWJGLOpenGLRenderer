package toolWindow;

import bookmarks.Bookmark;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import openGLWindow.RTIWindow;
import openGLWindow.RTIWindowHSH;
import openGLWindow.RTIWindowLRGB;
import openGLWindow.RTIWindowRGB;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Jed on 14-Jun-17.
 */
public class BottomTabPane extends TabPane {

    private RTIViewer rtiViewer;
    private RTIWindow currentRTIWindow;
    private Scene parent;
    private GridPane previewGridPane;
    private TextField fileName;
    private TextField imageWidthBox;
    private TextField imageHeightBox;
    private TextField imageFormat;
    private ImageView imagePreview;
    private BorderPane imageBorderPane;

    private VBox previewVBox;
    private StackPane imageContainerPane;
    private Rectangle previewWindowRect;
    private float previewRectScale = 1.0f;

    private ComboBox<String> bookmarkComboBox;
    private Button bookmarkAdd;
    private Button bookmarkDel;

    private ListView<Bookmark.Note> notesList;
    private Button notesEdit;
    private Button notesAdd;
    private Button notesDel;
    private Button updateBookmark;

    private Label fileNameLabel;
    private Label imageWidthLabel;
    private Label imageHeightLabel;
    private Label imageFormatLabel;

    private Label saveAsLabel;
    private Label saveChannelsLabel;
    RadioButton redChannelButton;
    RadioButton greenChannelButton;
    RadioButton blueChannelButton;
    private Label saveFormatLabel;
    ComboBox<String> imageFormats;
    private Button saveButton;


    public BottomTabPane(RTIViewer rtiViewer, Scene parent){
        super();
        this.rtiViewer = rtiViewer;
        this.parent = parent;

        BottomTabPaneListener.init(this);
        createComponents();
        setId("bottomTabPane");

        setMinWidth(0);
        setMinHeight(0);
        setMaxHeight(Double.MAX_VALUE);


        widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setWidth((Double)newValue);
                updateViewportOnSeperateThread();
            }
        });
    }


    private void updateViewportOnSeperateThread(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(RTIViewer.selectedWindow != null) {
                    updateViewportRect( RTIViewer.selectedWindow.getViewportX(),
                            RTIViewer.selectedWindow.getViewportY(),
                            RTIViewer.selectedWindow.getImageScale());
                }
            }
        });
    }

    private void createComponents(){
        Tab bookmarksTab = createBookmarksTab();
        Tab previewTab = createPreviewTab();
        Tab saveImageTab = createSaveImageTab();

        getTabs().addAll(previewTab, bookmarksTab, saveImageTab);
    }


    private Tab createPreviewTab(){
        Tab previewTab = new Tab("Preview");
        previewTab.setClosable(false);

        previewVBox = new VBox();

        previewGridPane = new GridPane();

        fileNameLabel = new Label("File:");
        fileName = new TextField("");
        fileName.setEditable(false);

        imageWidthLabel = new Label("Width:");
        imageWidthBox = new TextField("");
        imageWidthBox.setEditable(false);

        imageHeightLabel = new Label("Height:");
        imageHeightBox = new TextField("");
        imageHeightBox.setEditable(false);

        imageFormatLabel = new Label("Format:");
        imageFormat = new TextField("");
        imageFormat.setEditable(false);

        previewGridPane.setPadding(new Insets(0, 3, 0 , 3));

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

        previewVBox.getChildren().addAll(previewGridPane, imageBorderPane);
        previewVBox.setMargin(imageBorderPane, new Insets(5, 5, 5, 5));
        previewVBox.setMargin(previewGridPane, new Insets(5, 0, 5, 0));
        previewTab.setContent(previewVBox);

        return previewTab;
    }


    private void createImagePreview(){
        imageBorderPane = new BorderPane();

        previewWindowRect = new Rectangle(0, 0, 0 ,0);
        previewWindowRect.setFill(Color.TRANSPARENT);
        previewWindowRect.setStrokeWidth(2);

        imagePreview = new ImageView();
        imagePreview.setPreserveRatio(true);

        imageContainerPane = new StackPane(imagePreview, previewWindowRect);
        imageContainerPane.setMinWidth(0);
        imageContainerPane.setMinHeight(0);

        imageBorderPane.setCenter(imageContainerPane);
        imageBorderPane.setMinWidth(0);
        imageBorderPane.setMinHeight(0);
        imageBorderPane.prefWidthProperty().bind(RTIViewer.primaryStage.widthProperty());
        imageBorderPane.prefHeightProperty().bind(previewVBox.heightProperty());

        previewWindowRect.setDisable(true);

        imagePreview.fitHeightProperty().bind(imageBorderPane.heightProperty());
        imagePreview.fitWidthProperty().bind(imageBorderPane.widthProperty());
        imagePreview.setSmooth(true);
        setDefaultImage();

        imagePreview.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                previewImageClicked(event.getX(), event.getY());
            }
        });

        imagePreview.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                previewImageClicked(event.getX(), event.getY());
            }
        });

        imagePreview.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                //scroll up positive, scroll down negative
                previewRectScale += 0.01 * event.getDeltaY();
                if(previewRectScale > 10){previewRectScale = 10f;}
                else if(previewRectScale < 1){previewRectScale = 1;}

                if(RTIViewer.selectedWindow != null) {
                    RTIViewer.selectedWindow.updateViewportFromPreview(previewRectScale);
                }
            }
        });
    }

    private void previewImageClicked(double x, double y){
        float normX = (float)((x / imagePreview.getBoundsInParent().getWidth()) - 0.5) * 2;
        float normY = (float)-(((y / imagePreview.getBoundsInParent().getHeight()) - 0.5) * 2);

        RTIViewer.selectedWindow.updateViewportFromPreview(normX, normY, previewRectScale);
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
        imagePreview.setImage(null);
        updateSize(rtiViewer.primaryStage.getWidth(), rtiViewer.primaryStage.getHeight());
        imageBorderPane.setStyle("-fx-background-color: #ffffff;");
        previewWindowRect.setStroke(Color.TRANSPARENT);
    }

    public void updateSize(double width, double height){
        imageWidthBox.setPrefWidth(width / 6);
        imageHeightBox.setPrefWidth(width / 6);
        imageFormat.setPrefWidth(width / 6);

        bookmarkComboBox.setPrefWidth(width / 2);
        notesList.setPrefWidth(width / 1.5);

        updateBounds();
        setFonts(width, height);
    }


    private void setFonts(double width, double height){
        if(width < 335){
            setComponentLabels(12, 14);
        }else if(width < 450){
            setComponentLabels(14, 18);
        }else{
            setComponentLabels(16, 21);
        }
    }

    private void setComponentLabels(double previewFontSize, double saveFontSize){
        for(Label label : new Label[]{saveAsLabel, saveChannelsLabel, saveChannelsLabel,
                                        saveFormatLabel}){
            if(label != null) {label.setFont(Font.font(saveFontSize));}
        }

        for(Label label : new Label[]{fileNameLabel, imageWidthLabel, imageHeightLabel,
                imageFormatLabel}){
            if(label != null){label.setFont(Font.font(previewFontSize));}
        }

        for(RadioButton button : new RadioButton[]{redChannelButton, greenChannelButton,
                                                    blueChannelButton}){
            if(button != null){button.setFont(Font.font(saveFontSize));}
        }

        if(imageFormats != null){imageFormats.getEditor().setFont(Font.font(saveFontSize));}
        if(saveButton != null){saveButton.setFont(Font.font(saveFontSize));}
    }


    public void updateSelectedWindow(RTIWindow rtiWindow){
        if(rtiWindow == currentRTIWindow){return;}
        currentRTIWindow = rtiWindow;

        setFileText(rtiWindow.rtiObject.getFilePath());
        setWidthText(String.valueOf(rtiWindow.rtiObject.getWidth()));
        setHeightText(String.valueOf(rtiWindow.rtiObject.getHeight()));
        setBookmarks(rtiWindow.rtiObject.getBookmarks());

        if(rtiWindow instanceof RTIWindowRGB){
            setFormatText("PTM RGB");
        }else if(rtiWindow instanceof RTIWindowLRGB){
            setFormatText("PTM LRGB");
        }else if(rtiWindow instanceof RTIWindowHSH){
            setFormatText("HSH");
        }

        setPreviewImage(rtiWindow.rtiObject.previewImage);

    }


    public void updateViewportRect(float x, float y, float imageScale){
        previewRectScale = imageScale;
        previewWindowRect.setWidth(imagePreview.getBoundsInParent().getWidth() / imageScale);
        previewWindowRect.setHeight(imagePreview.getBoundsInParent().getHeight() / imageScale);

        double mappedX = (x / imageScale) * imagePreview.getBoundsInParent().getWidth() /2;
        double mappedY = -(y / imageScale) * imagePreview.getBoundsInParent().getHeight() / 2;


        previewWindowRect.setTranslateX(mappedX);
        previewWindowRect.setTranslateY(mappedY);
    }




    private Tab createBookmarksTab(){
        Tab tab = new Tab("Bookmarks");
        tab.setClosable(false);

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(5, 5, 5, 5));
        vBox.setFillWidth(true);

        GridPane bookmarkPane = new GridPane();
        bookmarkPane.setAlignment(Pos.CENTER);
        bookmarkPane.setHgap(5);
        bookmarkPane.setId("bottomBookmarkPane");
        bookmarkPane.setPadding(new Insets(5, 5, 5, 5));

        Label bookmarkLabel = new Label("Bookmark:");
        GridPane.setConstraints(bookmarkLabel, 0, 0, 1, 1);
        bookmarkComboBox = new ComboBox<>();
        bookmarkComboBox.setId("bookmarkComboBox");
        bookmarkComboBox.setOnAction(BottomTabPaneListener.getInstance());
        GridPane.setConstraints(bookmarkComboBox, 1, 0, 1, 1);

        bookmarkAdd = new Button("Add");
        bookmarkAdd.setId("addBookmarkButton");
        bookmarkAdd.setOnAction(BottomTabPaneListener.getInstance());
        GridPane.setConstraints(bookmarkAdd, 2, 0, 1, 1);


        bookmarkDel = new Button("Del");
        bookmarkDel.setId("deleteBookmarkButton");
        bookmarkDel.setOnAction(BottomTabPaneListener.getInstance());
        GridPane.setConstraints(bookmarkDel, 3, 0, 1, 1);

        bookmarkPane.getChildren().addAll(bookmarkLabel, bookmarkComboBox, bookmarkAdd, bookmarkDel);
        vBox.setMargin(bookmarkPane, new Insets(10, 0, 5, 0));


        GridPane bookmarkListPane = new GridPane();
        bookmarkListPane.setAlignment(Pos.CENTER);
        bookmarkListPane.setHgap(5);
        bookmarkListPane.setVgap(5);

        Label notesLabel = new Label("Notes:");
        GridPane.setConstraints(notesLabel, 0, 0, 1, 1);

        notesList = new ListView<Bookmark.Note>();

        notesList.setCellFactory(new Callback<ListView<Bookmark.Note>, ListCell<Bookmark.Note>>() {
            @Override
            public ListCell<Bookmark.Note> call(ListView<Bookmark.Note> param) {
                ListCell<Bookmark.Note> cell = new ListCell<Bookmark.Note>(){
                    @Override
                    protected void updateItem(Bookmark.Note item, boolean empty) {
                        super.updateItem(item, empty);
                        if(item != null){
                            setText(item.getSubject());

                            Tooltip tooltip = new Tooltip();
                            String tooltipText = item.getSubject() + System.lineSeparator() + item.getAuthor() +
                                    System.lineSeparator() + item.getTimeStamp() + System.lineSeparator() +
                                    System.lineSeparator() + item.getComment();
                            tooltip.setText(tooltipText);
                            tooltip.setPrefWidth(200);
                            tooltip.setPrefHeight(200);
                            tooltip.setWrapText(true);
                            tooltip.setTextAlignment(TextAlignment.JUSTIFY);

                            setTooltip(tooltip);
                        }else{
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        });

        notesList.setId("notesList");
        notesList.setMinHeight(0);
        GridPane.setConstraints(notesList, 0, 1, 2, 3);

        notesEdit = new Button("Edit");
        notesEdit.setId("editNote");
        notesEdit.setOnAction(BottomTabPaneListener.getInstance());
        GridPane.setConstraints(notesEdit, 2, 1, 1, 1);

        notesAdd = new Button("Add");
        notesAdd.setId("addNote");
        notesAdd.setOnAction(BottomTabPaneListener.getInstance());
        GridPane.setConstraints(notesAdd, 2, 2, 1, 1);

        notesDel = new Button("Del");
        notesDel.setId("delNote");
        notesDel.setOnAction(BottomTabPaneListener.getInstance());
        GridPane.setConstraints(notesDel, 2, 3, 1, 1);

        Label updateBookmarkLabel = new Label("Light, Zoom, Pan & Rendering:");
        GridPane.setConstraints(updateBookmarkLabel, 0, 4, 2, 1);
        updateBookmark = new Button("Update");
        updateBookmark.setId("updateBookmark");
        updateBookmark.setOnAction(BottomTabPaneListener.getInstance());
        GridPane.setConstraints(updateBookmark, 2, 4, 1, 1);

        bookmarkListPane.getChildren().addAll(  notesLabel, notesList,
                                                notesEdit, notesAdd, notesDel,
                                                updateBookmarkLabel, updateBookmark);
        bookmarkListPane.setId("bookmarkListPane");

        bookmarkListPane.setPadding(new Insets(5, 5, 5, 5));
        bookmarkListPane.setMinHeight(0);
        notesList.setMinHeight(0);
        notesEdit.setMinHeight(0);
        notesAdd.setMinHeight(0);
        notesDel.setMinHeight(0);
        updateBookmark.setMinHeight(0);

        vBox.setMargin(bookmarkListPane, new Insets(5, 0, 0, 0));

        vBox.getChildren().addAll(bookmarkPane, bookmarkListPane);
        tab.setContent(vBox);
        return tab;
    }


    private Tab createSaveImageTab(){
        Tab imageTab = new Tab("Save");
        imageTab.setClosable(false);

        VBox vBox = new VBox();

        GridPane gridPane = new GridPane();
        saveAsLabel = new Label("Save as image:");
        GridPane.setConstraints(saveAsLabel, 0, 0, 1, 1);

        VBox vBox1 = new VBox();

        saveChannelsLabel = new Label("Save colour channels:");
        redChannelButton = new RadioButton("Red");
        greenChannelButton = new RadioButton("Green");
        blueChannelButton = new RadioButton("Blue");
        vBox1.getChildren().addAll(saveChannelsLabel, redChannelButton, greenChannelButton, blueChannelButton);
        vBox1.setSpacing(5);
        GridPane.setConstraints(vBox1, 0, 1, 1, 2);
        vBox1.setFillWidth(true);
        vBox1.setPadding(new Insets(5, 5, 5, 5));
        vBox1.setId("bottomTabPaneColourChannelsPane");

        VBox vBox2 = new VBox();
        vBox2.setFillWidth(true);

        saveFormatLabel = new Label("Save as format:");
        imageFormats = new ComboBox<>(FXCollections.observableArrayList(
                    "jpg",
                            "png"
        ));
        imageFormats.getSelectionModel().select(0);

        vBox2.getChildren().addAll(saveFormatLabel, imageFormats);
        vBox2.setId("bottomTabPaneFormatPane");

        vBox2.setPadding(new Insets(5, 5, 5, 5));
        vBox.setSpacing(5);
        GridPane.setConstraints(vBox2, 1, 1, 1, 1);

        saveButton = new Button("Save as...");
        saveButton.setId("saveAs");
        saveButton.setOnAction(BottomTabPaneListener.getInstance());
        GridPane.setConstraints(saveButton, 1, 2, 1, 1);
        saveButton.setAlignment(Pos.CENTER);

        gridPane.setHgap(5);
        gridPane.setVgap(5);

        gridPane.getChildren().addAll(saveAsLabel, vBox1, vBox2, saveButton);
        vBox.getChildren().add(gridPane);
        vBox.setAlignment(Pos.CENTER);

        gridPane.setAlignment(Pos.TOP_CENTER);


        imageTab.setContent(vBox);
        return imageTab;
    }


    public void setBookmarks(ArrayList<Bookmark> bookmarks){
        bookmarkComboBox.getItems().clear();
        notesList.getItems().clear();

        if(bookmarks == null){return;}

        for(Bookmark bookmark : bookmarks){
            bookmarkComboBox.getItems().add(bookmark.getName());
        }
    }

    public void showNotes(String bookmarkName){
        notesList.getItems().clear();

        ArrayList<Bookmark> bookmarks = currentRTIWindow.rtiObject.getBookmarks();

        for(Bookmark bookmark : bookmarks){
            if(bookmark.getName().equals(bookmarkName)){
                for(Bookmark.Note note : bookmark.getNotes()){
                    notesList.getItems().add(note);
                }
                break;
            }
        }
    }


    public void setNoFocusedWindow(){
        setFileText("");
        setWidthText("");
        setHeightText("");
        setFormatText("");
        setDefaultImage();

        setBookmarks(null);
    }


    public List<String> getCurrentBookmarkNames(){
        return bookmarkComboBox.getItems();
    }

    public ComboBox<String> getBookmarkComboBox() {
        return bookmarkComboBox;
    }


    public void setSelectedBookmark(String bookmarkName){
        bookmarkComboBox.getSelectionModel().select(bookmarkName);
    }

    public ListView<Bookmark.Note> getNotesList() {
        return notesList;
    }
}

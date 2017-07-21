package imageCreation;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import ptmCreation.PTMObjectLRGB;
import ptmCreation.PTMObjectRGB;
import ptmCreation.RTIObject;
import ptmCreation.RTIObjectHSH;
import toolWindow.RTIViewer;
import utils.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Jed on 03-Jul-17.
 */
public class ImageCreator {

    public static void saveImage(RTIObject rtiObject, float lightX, float lightY, RTIViewer.ShaderProgram shaderProgram,
                                    boolean red, boolean green, boolean blue, String format, File destination,
                                    float[] shaderParams, boolean isGreyscale){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                WritableImage createdImage;

                if(rtiObject instanceof PTMObjectRGB){
                    createdImage = ImageCreatorPTM_RGB.createImage(rtiObject, lightX, lightY,
                                                                    shaderProgram, red, green, blue, shaderParams);
                }else if(rtiObject instanceof PTMObjectLRGB){
                    createdImage = ImageCreatorPTM_LRGB.createImage(rtiObject, lightX, lightY,
                            shaderProgram, red, green, blue, shaderParams);
                }else if(rtiObject instanceof RTIObjectHSH){
                    createdImage = ImageCreatorHSH.createImage(rtiObject, lightX, lightY,
                                                                shaderProgram, red, green, blue, shaderParams);
                }else{
                    return;
                }

                try{
                    saveImage(createdImage, format, destination, isGreyscale);
                }catch (IOException e){
                    e.printStackTrace();

                }
            }
        });

        thread.start();

    }



    private static void saveImage(WritableImage writableImage, String format,
                                  File destination, boolean isGreyScale) throws IOException{
        BufferedImage image = SwingFXUtils.fromFXImage(writableImage, null);

        BufferedImage newImg = new BufferedImage(image.getWidth(),
                                            image.getHeight(), BufferedImage.TYPE_INT_RGB);

        int rgb;
        for(int x = 0; x < image.getWidth(); x++){
            for(int y = 0; y < image.getHeight(); y++){
                rgb = image.getRGB(x, y);

                if(isGreyScale){rgb = convertToGreyscale(rgb);}

                newImg.setRGB(x, y, rgb);
            }
        }
        ImageIO.write(newImg, format.toUpperCase(), destination);

    }




    private static int convertToGreyscale(int rgb){
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = (rgb & 0xFF);

        int grayLevel = (r + g + b) / 3;
        int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel;

        return gray;
    }





    public static WritableImage createNormalsImage(RTIObject rtiObject, boolean red, boolean green, boolean blue){
        WritableImage writableImage = new WritableImage(rtiObject.getWidth(), rtiObject.getHeight());

        int position, r, g, b;
        for(int x = 0; x < rtiObject.getWidth(); x++) {
            for (int y = 0; y < rtiObject.getHeight(); y++) {
                position = ((y * rtiObject.getWidth()) + x) * 3;

                if(red){
                    r = Utils.convertNormalCoordToColour(rtiObject.getNormals().get(position));
                }else{r = 0;}

                if(green){
                    g = Utils.convertNormalCoordToColour(rtiObject.getNormals().get(position + 1));
                }else{g = 0;}

                if(blue){
                    b = Utils.convertNormalCoordToColour(rtiObject.getNormals().get(position + 2));
                }else{b = 0;}

                writableImage.getPixelWriter().setColor(x, y, javafx.scene.paint.Color.rgb(r, g, b));
            }
        }

        return writableImage;
    }


    public static float[] calcYUV(float r, float g, float b){
        float y = r * 0.299f + g * 0.587f + b * 0.144f;
        float u = r * -0.14713f + g * -0.28886f + b * 0.436f;
        float v = r * 0.615f + g * -0.51499f + b * -0.10001f;

        return new float[]{y, u, v};
    }

    public static float[] getRGB(float lum, float u, float v){
        float r = lum + v * 1.13983f;
        float g = lum + u * -0.39465f + v * -0.5806f;
        float b = lum + u * 2.03211f;

        return new float[]{r, g, b};
    }
}

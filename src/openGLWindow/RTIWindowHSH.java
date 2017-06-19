package openGLWindow;

import org.lwjgl.BufferUtils;
import ptmCreation.PTMObject;
import ptmCreation.PTMObjectHSH;
import toolWindow.RTIViewer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;

/**
 * Created by Jed on 18-Jun-17.
 */
public class RTIWindowHSH extends RTIWindow {

    private PTMObjectHSH ptmObjectHSH;
    private int basisTerms;

    private IntBuffer dataTexture;
    private int dataTextureRef;

    private int redCoeffs1Ref;
    private int redCoeffs2Ref;
    private int redCoeffs3Ref;

    private int greenCoeffs1Ref;
    private int greenCoeffs2Ref;
    private int greenCoeffs3Ref;

    private int blueCoeffs1Ref;
    private int blueCoeffs2Ref;
    private int blueCoeffs3Ref;

    public RTIWindowHSH(PTMObjectHSH ptmObject) {
        super(ptmObject);
        ptmObjectHSH = ptmObject;
        basisTerms = ptmObject.getBasisTerms();
        dataTexture = BufferUtils.createIntBuffer(3);
        dataTexture.put(0, basisTerms);
    }

    @Override
    protected void createShaders() throws Exception {
        createShader(RTIViewer.ShaderProgram.DEFAULT, "src/shaders/defaultVertexShader.glsl",
                "src/shaders/hshShaders/defaultFragmentShaderHSH.glsl");

    }

    @Override
    protected void bindSpecificShaderTextures(int programID) {
        dataTextureRef = glGetUniformLocation(programID, "dataTexture");
        normalsRef = glGetUniformLocation(programID, "normals");

        redCoeffs1Ref = glGetUniformLocation(programID, "redCoeffs1");
        greenCoeffs1Ref = glGetUniformLocation(programID, "greenCoeffs1");
        blueCoeffs1Ref = glGetUniformLocation(programID, "blueCoeffs1");

        redCoeffs2Ref = glGetUniformLocation(programID, "redCoeffs2");
        greenCoeffs2Ref = glGetUniformLocation(programID, "greenCoeffs2");
        blueCoeffs2Ref = glGetUniformLocation(programID, "blueCoeffs2");

         redCoeffs3Ref = glGetUniformLocation(programID, "redCoeffs3");
        greenCoeffs3Ref = glGetUniformLocation(programID, "greenCoeffs3");
        blueCoeffs3Ref = glGetUniformLocation(programID, "blueCoeffs3");
    }

    @Override
    protected void bindShaderVals() {
        glUniform1f(shaderWidth, imageWidth);
        glUniform1f(shaderHeight, imageHeight);
        glUniform1f(imageScaleRef, imageScale);
        glUniform1f(shaderViewportX, viewportX);
        glUniform1f(shaderViewportY, viewportY);

        glUniform1i(dataTextureRef, 0);
        setShaderTexture(0, dataTexture, 1, 1);


        glUniform1i(normalsRef, 1);
        setNormalsTexture(1, ptmObjectHSH.getNormals());


        glUniform1i(redCoeffs1Ref, 2);
        glUniform1i(greenCoeffs1Ref, 3);
        glUniform1i(blueCoeffs1Ref, 4);

        setNormalsTexture(2, ptmObjectHSH.getRedVals1());
        setNormalsTexture(3, ptmObjectHSH.getGreenVals1());
        setNormalsTexture(4, ptmObjectHSH.getBlueVals1());


        glUniform1i(redCoeffs2Ref, 5);
        glUniform1i(greenCoeffs2Ref, 6);
        glUniform1i(blueCoeffs2Ref, 7);

        if(basisTerms > 3){
            setNormalsTexture(5, ptmObjectHSH.getRedVals2());
            setNormalsTexture(6, ptmObjectHSH.getGreenVals2());
            setNormalsTexture(7, ptmObjectHSH.getBlueVals2());
        }

        glUniform1i(redCoeffs3Ref, 8);
        glUniform1i(greenCoeffs3Ref, 9);
        glUniform1i(blueCoeffs3Ref, 10);

        if(basisTerms > 6){
            setNormalsTexture(8, ptmObjectHSH.getRedVals3());
            setNormalsTexture(9, ptmObjectHSH.getGreenVals3());
            setNormalsTexture(10, ptmObjectHSH.getBlueVals3());
        }
    }
}
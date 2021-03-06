#version 330
//FRAGMENT_SHADER

//x and y position of the light, normalised between -1.0 and +!.0
uniform float lightX;
uniform float lightY;

//height and width of the ptm to render
uniform float imageHeight;
uniform float imageWidth;

//the diffuse gain arg that the user canchange with the slider
uniform float diffGain;

//textures containing the first 3 and last 3 luminance coeffs, and the rgb coeffs
uniform isampler2D lumCoeffs1;
uniform isampler2D lumCoeffs2;
uniform isampler2D rgbCoeffs;

//tecture containing the normal vector for each pixel
uniform sampler2D normals;

//coordinate on textures with the pan from the vertex shader
in vec2 texCoordV;

//colour to write to the pixel this shader is being executed for
out vec4 colorOut;

//the values for the min and max gain are fixed
float minGain = 1.0;
float maxGain = 10.0;


//convert openGL coords with (0, 0) at the center to coords with (0, 0) in the top left
vec2 convertCoords(vec2 coords){
    return vec2((coords.x + 1) / 2, (1 - coords.y) / 2);
}

//scale the coords which are 0.0 - 1.0 to 0 - imageHeight and 0 - imageWidth
vec2 convertToPTMCoords(vec2 coords){
    return vec2(coords.x * imageWidth,
                coords.y * imageHeight);
}

//the standard PTM equation, see the user guide for a link the the PTM paper
float applyPTM(float a0, float a1, float a2, float a3, float a4, float a5){
    float i = (a0 * lightX * lightX) + (a1 * lightY * lightY)
            + (a2 * lightX * lightY) + (a3 * lightX)
            + (a4 * lightY) + a5;

    if(i < 0){i = 0;}
    else if(i > 255){i =  255;}
    i = i / 255;

    return i;
}



//the diffuse gain equation, see the user guide for a link to the PTM papaer
float applyDiffuseGain(ivec4 coeffs1, ivec4 coeffs2, vec3 normal, float modGain){
    float a0 = modGain * coeffs1.x;
    float a1 = modGain * coeffs1.y;
    float a2 = modGain * coeffs1.z;
    float a3t = ((coeffs1.x << 1) * normal.x) + (coeffs1.z * normal.y);
    float a3 = ((1.0 - modGain) * a3t) + coeffs2.x;
    float a4t = ((coeffs1.y << 1) * normal.y) + (coeffs1.z * normal.x);
    float a4 = ((1.0 - modGain) * a4t) + coeffs2.y;
    float a5 = (1.0 - modGain) * (coeffs1.x*normal.x*normal.x + coeffs1.y*normal.y*normal.y +
                               coeffs1.z*normal.x*normal.y)
               + (coeffs2.x - a3) * normal.x
               + (coeffs2.y - a4) * normal.y
               + coeffs2.z;

    return applyPTM(a0, a1, a2, a3, a4, a5);
}



void main() {
    //convert coords so top left is (0, 0)
    vec2 coords = convertCoords(texCoordV);

    //map coords from 0.0 - 1.0 to real coords in texture
    ivec2 ptmCoords = ivec2(convertToPTMCoords(coords));

    //aget the lum and rgb coeffs for this pixel
    ivec4 lumVals1 = texelFetch(lumCoeffs1, ptmCoords, 0);
    ivec4 lumVals2 = texelFetch(lumCoeffs2, ptmCoords, 0);
    ivec4 rgbVals =  texelFetch(rgbCoeffs, ptmCoords, 0);

    //get the normal vector for the pixel this shader is being execute for
    vec3 normal = texelFetch(normals, ptmCoords, 0).xyz;

    ///get the enhanced lum from the diff gain equation
    float lum = applyDiffuseGain(lumVals1, lumVals2, normal, diffGain) / 255.0;

    //the colour to be shown for this pixel on the screen, the 1 is the a of rgba (the transparency, 1 = opaque)
    colorOut = vec4(rgbVals.x * lum, rgbVals.y * lum, rgbVals.z * lum, 1);
}

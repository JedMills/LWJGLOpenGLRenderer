#version 420 core
//#extension GL_EXT_gpu_shader4 : enable

uniform float lightX;
uniform float lightY;
uniform int imageHeight;
uniform int imageWidth;

uniform float gain;
uniform float minGain;
uniform float maxGain;

uniform isampler2D rVals1;
uniform isampler2D rVals2;
uniform isampler2D gVals1;
uniform isampler2D gVals2;
uniform isampler2D bVals1;
uniform isampler2D bVals2;
uniform sampler2D normals;

in vec2 texCoordV;
out vec4 colorOut;

vec2 convertCoords(vec2 coords){
    return vec2((coords.x + 1) / 2, (1 - coords.y) / 2);
}


vec2 convertToPTMCoords(vec2 coords){
    return vec2(coords.x * imageWidth,
                coords.y * imageHeight);
}


float applyPTM(float a0, float a1, float a2, float a3, float a4, float a5){
    float i = (a0 * lightX * lightX) + (a1 * lightY * lightY)
            + (a2 * lightX * lightY) + (a3 * lightX)
            + (a4 * lightY) + a5;

    if(i < 0){i = 0;}
    else if(i > 255){i =  255;}
    i = i / 255;

    return i;
}


float applyDiffuseGain(ivec4 coeffs1, ivec4 coeffs2, vec4 normal){
    float a0 = gain * coeffs1.x;
    float a1 = gain * coeffs1.y;
    float a2 = gain * coeffs1.z;
    float a3t = ((coeffs1.x << 1) * normal.x) + (coeffs1.z * normal.y);
    float a3 = ((1.0 - gain) * a3t) + coeffs2.x;
    float a4t = ((coeffs1.y << 1) * normal.y) + (coeffs1.z * normal.x);
    float a4 = ((1.0 - gain) * a4t) + coeffs2.y;
    float a5 = (1.0 - gain) * (coeffs1.x*normal.x*normal.x + coeffs1.y*normal.y*normal.y +
                               coeffs1.z*normal.x*normal.y)
               + (coeffs2.x - a3) * normal.x
               + (coeffs2.y - a4) * normal.y
               + coeffs2.z;

    return applyPTM(a0, a1, a2, a3, a4, a5);
}



void main() {
    vec2 coords = convertCoords(texCoordV);

    vec2 ptmCoords = convertToPTMCoords(coords);

    ivec4 redCoeffs1 = texelFetch(rVals1, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 redCoeffs2 = texelFetch(rVals2, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 greenCoeffs1 = texelFetch(gVals1, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 greenCoeffs2 = texelFetch(gVals2, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 blueCoeffs1 = texelFetch(bVals1, ivec2(ptmCoords.x, ptmCoords.y), 0);
    ivec4 blueCoeffs2 = texelFetch(bVals2, ivec2(ptmCoords.x, ptmCoords.y), 0);
    vec4 normal = texelFetch(normals, ivec2(ptmCoords.x, ptmCoords.y), 0);

    float red = applyDiffuseGain(redCoeffs1, redCoeffs2, normal);
    float green = applyDiffuseGain(greenCoeffs1, greenCoeffs2, normal);
    float blue = applyDiffuseGain(blueCoeffs1, blueCoeffs2, normal);

    colorOut = vec4(red, green, blue, 1);

}
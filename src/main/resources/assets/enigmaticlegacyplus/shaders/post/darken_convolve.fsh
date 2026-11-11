#version 330

// Enigmatic Legacy+ "darken" effect, first pass: a colour matrix, then a saturation step whose luma weights (Gray)
// can be scaled down. Same maths as the 1.21.1 minecraft color_convolve program the effect used; that program's
// Gray and Saturation are constants in 26.1.2, and the effect changes both every frame.
// The values in post_effect/darken.json are only placeholders: DarkenEffect.java rewrites this block every frame.
// Saturation comes first on purpose: the game's std140 writer pads every vec3 to 16 bytes, so a float placed after
// a vec3 would not be where GLSL expects it.

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform DarkenConfig {
    float Saturation;
    vec3 RedMatrix;
    vec3 GreenMatrix;
    vec3 BlueMatrix;
    vec3 Gray;
};

out vec4 fragColor;

void main() {
    vec4 InTexel = texture(InSampler, texCoord);

    // Color Matrix
    float RedValue = dot(InTexel.rgb, RedMatrix);
    float GreenValue = dot(InTexel.rgb, GreenMatrix);
    float BlueValue = dot(InTexel.rgb, BlueMatrix);
    vec3 OutColor = vec3(RedValue, GreenValue, BlueValue);

    // Saturation
    float Luma = dot(OutColor, Gray);
    vec3 Chroma = OutColor - Luma;
    OutColor = (Chroma * Saturation) + Luma;

    fragColor = vec4(OutColor, 1.0);
}

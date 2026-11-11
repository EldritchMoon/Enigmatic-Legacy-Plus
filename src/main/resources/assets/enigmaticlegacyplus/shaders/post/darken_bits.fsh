#version 330

// Enigmatic Legacy+ "darken" effect, second pass: the minecraft "bits" program with its saturation as a value that
// can change (a constant 1.5 in 26.1.2). The values in post_effect/darken.json are only placeholders:
// DarkenEffect.java rewrites this block every frame.

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform DarkenBitsConfig {
    float Resolution;
    float MosaicSize;
    float Saturation;
};

out vec4 fragColor;

void main() {
    vec2 mosaicInSize = InSize / MosaicSize;
    vec2 fractPix = fract(texCoord * mosaicInSize) / mosaicInSize;

    vec4 baseTexel = texture(InSampler, texCoord - fractPix);

    vec3 fractTexel = baseTexel.rgb - fract(baseTexel.rgb * Resolution) / Resolution;
    float luma = dot(fractTexel, vec3(0.3, 0.59, 0.11));
    vec3 chroma = (fractTexel - luma) * Saturation;

    fragColor = vec4(luma + chroma, 1.0);
}

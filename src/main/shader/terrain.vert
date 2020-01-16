#version 450

#define MATERIAL_COUNT 64
#define MAX_INSTANCE_COUNT 256


layout (constant_id = 0) const int pipelineIndex = 0;

layout (std430, push_constant) uniform PushConstants {
    mat4 cameraMatrix;
    vec4 cameraCoord;
    ivec2 cursorCoord;
    uint part;
} constants;

struct Material {
    vec4 color;
    float ambient;
    float diffuse;
    float specular;
    int shininess;
};

layout(set = 0, binding = 0) uniform Materials {
	Material[MATERIAL_COUNT] datas;
} materials;

struct Light {
   vec4 direction;
   vec4 lightColor;
   float ambientStrength;
   float specularStrength;
};

layout(std140, set = 0, binding = 1) uniform LightBuffer {
    Light light;
} lightBuffer;


layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 inNormal;
layout(location = 2) in uint inColorIndex;
layout(location = 3) in vec4 inLayerColor;


layout(location = 0) flat out vec3 materialColor;

layout(location = 1) flat out float ambient;
layout(location = 2) flat out float diffuse;
layout(location = 3) flat out float specular;

layout(location = 4) flat out vec3 layerColor;


void main()
{
    gl_Position = constants.cameraMatrix * vec4(inPosition, 1.0);
    Material material = materials.datas[inColorIndex];

    vec3 lightDir;
    if(lightBuffer.light.direction.w == 1.0)
    {
    	// Directional light
    	lightDir = -lightBuffer.light.direction.xyz;
    }
    else
    {
    	lightDir = normalize(lightBuffer.light.direction.xyz - inPosition);
    }

    ambient = lightBuffer.light.ambientStrength + material.ambient;
    diffuse = max(dot(inNormal, lightDir), 0.0) + material.diffuse;

    // specular
    vec3 reflectDir = reflect(-lightDir, inNormal);
    vec3 viewDir = normalize(constants.cameraCoord.xyz - inPosition);
    float spec = pow(max(dot(viewDir, normalize(reflectDir)), 0), float(material.shininess));
    specular = (material.specular + lightBuffer.light.specularStrength) * spec;

    materialColor = material.color.xyz;
    // fragPos = modelPosition.xyz;
    layerColor = inLayerColor.xyz;
}

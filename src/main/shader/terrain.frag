#version 450

layout (constant_id = 0) const int pipelineIndex = 0;

layout (std430, push_constant) uniform PushConstants {
    mat4 cameraMatrix;
    vec4 cameraCoord;
    ivec2 cursorCoord;
    uint drawcall;
} constants;

struct Light {
   vec4 direction;
   vec4 lightColor;
   float ambientStrength;
   float specularStrength;
};


layout(std140, set = 0, binding = 1) uniform LightBuffer {
    Light light;
} lightBuffer;

struct PickingInfo {
    uint pipeline;
    uint triangle;
    uint instance;
    uint drawcall;
    uint layer;
};

layout(set = 0, binding = 2) buffer MousePicking {
    PickingInfo layer[5];
} mousePicking;


layout(location = 0) flat in vec3 materialColor;

layout(location = 1) flat in float ambient;
layout(location = 2) flat in float diffuse;
layout(location = 3) flat in float specular;

layout(location = 4) flat in vec3 layerColor;

layout(location = 0) out vec4 outColor;


void main()
{
	float trgAmbient = ambient;
	float trgDiffuse = diffuse;
	float trgSpecular = specular;

    vec3 lightColor = lightBuffer.light.lightColor.xyz;
    vec4 color = vec4(materialColor, 0.0);

	if (ivec2(gl_FragCoord.xy) == constants.cursorCoord)
	{
		mousePicking.layer[2].triangle = gl_PrimitiveID;
		mousePicking.layer[2].instance = -1;
		mousePicking.layer[2].pipeline = pipelineIndex;
		mousePicking.layer[2].drawcall = constants.drawcall;
	}

	if (mousePicking.layer[1].triangle == gl_PrimitiveID
		&& mousePicking.layer[1].pipeline == pipelineIndex
		&& mousePicking.layer[1].drawcall == constants.drawcall
		&& mousePicking.layer[1].layer == 2)
	{
		// Selection
		color += vec4(0.0, 0.3, 0.0, color.w);
		trgAmbient = 1;
		trgDiffuse = 0.5;
		trgSpecular = 0.5;
	}

	// Result
	outColor = vec4(((trgAmbient + trgDiffuse + trgSpecular) * lightColor) * color.xyz, 0.0);

	if (layerColor != vec3(0, 0, 0))
	{
		outColor = mix(outColor, vec4(layerColor, 0), 0.5);
	}
}

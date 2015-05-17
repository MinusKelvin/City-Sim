#version 330 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec4 color;

uniform mat4 proj;

out float alpha;
out vec2 pos;

void main() {
	gl_Position = proj * vec4(position, 1.0);
	pos = position.xy;
	alpha = color.r;
}
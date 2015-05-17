#version 330 core

in vec2 texcoord;

uniform sampler2D tex;
uniform sampler2D alpha;

out vec4 color;

void main() {
	color = texture(tex, texcoord);
	color.a *= texture(alpha, texcoord).r;
}
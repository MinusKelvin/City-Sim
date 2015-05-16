#version 330 core

in vec2 texcoord;

uniform sampler2D tex;
uniform vec2 ppos;
uniform vec2 trans;

out vec4 color;

void main() {
	color = texture(tex, texcoord);
	color.a = length(ppos-trans);
}
#version 330 core

in float alpha;
in vec2 pos;

uniform vec2 ppos;

out vec4 color;

void main() {
	color.gb = vec2(0.0);
	color.a = 1.0;
	color.r = alpha + max((length(pos-ppos)-10.0)/20.0, 0.0);
}
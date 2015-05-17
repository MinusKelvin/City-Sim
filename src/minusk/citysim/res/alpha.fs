#version 330 core

in vec2 texcoord;

uniform sampler2DMS tex;
uniform sampler2D alpha;
uniform int samples;

out vec4 color;

void main() {
	vec4 c = vec4(0.0);
	for (int i = 0; i < samples; i++)
		c += texelFetch(tex, ivec2(texcoord), i);
	color = c / float(samples);
	color.a *= texelFetch(alpha, ivec2(texcoord),0).r;
}
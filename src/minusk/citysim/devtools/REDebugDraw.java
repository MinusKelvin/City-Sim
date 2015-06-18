package minusk.citysim.devtools;

import minusk.render.graphics.Camera;
import minusk.render.graphics.draw.ColorDrawPass;
import minusk.render.graphics.filters.BlendFunc;

import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.particle.ParticleColor;

public class REDebugDraw extends DebugDraw {
	private ColorDrawPass draw;

	@Override
	public void drawPoint(Vec2 argPoint, float argRadiusOnScreen, Color3f argColor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawSolidPolygon(Vec2[] vertices, int vertexCount, Color3f color) {
		int c = 127 << 24 | ((int)(color.z*255)) << 16 | ((int)(color.y*255)) << 8 | ((int)(color.x*255));
		for (int i = 2; i < vertexCount; i++) {
			draw.drawTriangle(vertices[0].x, vertices[0].y, c, vertices[i-1].x, vertices[i-1].y, c, vertices[i].x, vertices[i].y, c, 10);
		}
	}

	@Override
	public void drawCircle(Vec2 center, float radius, Color3f color) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawSolidCircle(Vec2 center, float radius, Vec2 axis,
			Color3f color) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawSegment(Vec2 p1, Vec2 p2, Color3f color) {
		int c = 127 << 24 | ((int)(color.z*255)) << 16 | ((int)(color.y*255)) << 8 | ((int)(color.x*255));
		draw.drawLine(p1.x, p1.y, p2.x, p2.y, 0.05f, 10, c);
	}

	@Override
	public void drawTransform(Transform xf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawString(float x, float y, String s, Color3f color) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawParticles(Vec2[] centers, float radius,
			ParticleColor[] colors, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawParticlesWireframe(Vec2[] centers, float radius,
			ParticleColor[] colors, int count) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void flush() {
		draw.end();
		draw.begin();
	}
	
	public void initalize(Camera camera) {
		draw = new ColorDrawPass();
		draw.camera = camera;
		draw.begin();
		draw.setBlendFunc(BlendFunc.TRANSPARENCY);
	}
}

package minusk.citysim.world;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import java.util.HashMap;

import minusk.citysim.Main;
import minusk.citysim.entities.friendly.Player;
import minusk.render.core.Input;
import minusk.render.interfaces.Renderable;
import minusk.render.interfaces.Updateable;

public class Map implements Renderable, Updateable {
	public final Player player = new Player();
	
//	private final Point3I cache = new Point3I();
	
	private HashMap<Point3I, Chunk> map = new HashMap<>();
	private MapRenderer renderer;
	
	public Map() {
		renderer = new MapRenderer();
		map.put(new Point3I(0,0,0), new Chunk(0,0));
	}
	
	@Override
	public void update() {
		Input input = Main.game.getInput();
		int movement = input.isKeyDown(GLFW_KEY_LEFT_SHIFT)?2:input.isKeyDown(GLFW_KEY_LEFT_CONTROL)?1:0;
		
		if (input.isKeyDown(GLFW_KEY_W)) {
			renderer.camera.transY -= (movement==2?0.046f:movement==1?0.11f:0.023f);
		}
		if (input.isKeyDown(GLFW_KEY_S)) {
			renderer.camera.transY += (movement==2?0.046f:movement==1?0.11f:0.023f);
		}
		if (input.isKeyDown(GLFW_KEY_A)) {
			renderer.camera.transX += (movement==2?0.046f:movement==1?0.11f:0.023f);
		}
		if (input.isKeyDown(GLFW_KEY_D)) {
			renderer.camera.transX -= (movement==2?0.046f:movement==1?0.11f:0.023f);
		}
		if (input.getScrollY() > 0)
			renderer.camera.zoom *= 1.2;
		if (input.getScrollY() < 0)
			renderer.camera.zoom /= 1.2;
	}
	
	@Override
	public void render() {
		renderer.begin();
		for (Chunk c : map.values())
			c.render(renderer);
		renderer.end();
	}
	
	private static class Point3I {
		public int x,y,z;
//		public Point3I() {
//			this(0,0,0);
//		}
		public Point3I(int x, int y, int z) {
			set(x,y,z);
		}
		@Override
		public int hashCode() {
			return x * 37 + y * 379 + z * 3823;
		}
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Point3I))
				return false;
			Point3I p = (Point3I) o;
			return p.x == x && p.y == y && p.z == z;
		}
		public Point3I set(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
			return this;
		}
	}
}

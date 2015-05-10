package minusk.citysim.world;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import java.util.HashMap;

import minusk.citysim.Main;
import minusk.citysim.entities.Car;
import minusk.citysim.entities.friendly.Player;
import minusk.render.core.Input;
import minusk.render.graphics.draw.SpriteDrawPass;
import minusk.render.graphics.filters.BlendFunc;
import minusk.render.graphics.globjects.SpriteSheet;
import minusk.render.interfaces.Renderable;
import minusk.render.interfaces.Updateable;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

public class Map implements Renderable, Updateable {
	public final World physics = new World(new Vec2());
	public final Player player = new Player();
	
	private final Point2I cache = new Point2I();
	
	private HashMap<Point2I, Chunk> map = new HashMap<>();
	private MapRenderer renderer;
	private SpriteDrawPass carDraws;
	private Car c;
	
	public Map() {
		renderer = new MapRenderer();
		SpriteSheet sheet = new SpriteSheet(32, 64, 16, 1);
		sheet.setSprites(getClass().getResourceAsStream("/minusk/citysim/res/cars.png"), 0, 0);
		carDraws = new SpriteDrawPass(sheet);
		carDraws.camera = renderer.camera;
		carDraws.setBlendFunc(BlendFunc.TRANSPARENCY);
	}
	
	public void init() {
		player.init();
		c = new Car();
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
		
		if (!map.containsKey(cache.set((int)Math.floor(-renderer.camera.transX/100),(int)Math.floor(-renderer.camera.transY/100))))
			map.put(new Point2I(cache), new Chunk(cache.x, cache.y));
		
		player.update();
		c.update();
		
		renderer.camera.roll = -player.getPhysicsObject().getTransform().q.getAngle();
		physics.step(1/60f, 8, 3);
		renderer.camera.transX = -player.getPhysicsObject().getTransform().p.x;
		renderer.camera.transY = -player.getPhysicsObject().getTransform().p.y;
	}
	
	@Override
	public void render() {
		renderer.begin();
		for (Chunk c : map.values())
			c.render(renderer);
		renderer.end();
		
		carDraws.begin();
		player.render(carDraws);
		c.render(carDraws);
		carDraws.end();
	}
	
	private static class Point2I {
		public int x,y;
		public Point2I() {
			this(0,0);
		}
		public Point2I(Point2I p) {
			this(p.x,p.y);
		}
		public Point2I(int x, int y) {
			set(x,y);
		}
		@Override
		public int hashCode() {
			return x * 37 + y * 379;
		}
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Point2I))
				return false;
			Point2I p = (Point2I) o;
			return p.x == x && p.y == y;
		}
		public Point2I set(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}
	}
}

package minusk.citysim.world;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Q;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;

import java.util.ArrayList;
import java.util.HashMap;

import minusk.citysim.Main;
import minusk.citysim.devtools.REDebugDraw;
import minusk.citysim.entities.Entity;
import minusk.citysim.entities.friendly.Player;
import minusk.render.core.Input;
import minusk.render.graphics.Color;
import minusk.render.graphics.draw.ColorDrawPass;
import minusk.render.graphics.draw.MultisampledTextureDrawPass;
import minusk.render.graphics.draw.SpriteDrawPass;
import minusk.render.graphics.filters.BlendFunc;
import minusk.render.graphics.globjects.DepthStencilBuffer;
import minusk.render.graphics.globjects.Framebuffer;
import minusk.render.graphics.globjects.MultisampledTexture;
import minusk.render.graphics.globjects.Shader;
import minusk.render.graphics.globjects.SpriteSheet;
import minusk.render.graphics.globjects.Texture;
import minusk.render.interfaces.Renderable;
import minusk.render.interfaces.Updateable;

import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.Joint;

public class Map implements Renderable, Updateable {
	public final World physics = new World(new Vec2());
	public final Player player = new Player();
	
	private final Point2I cache = new Point2I();
	
	private HashMap<Point2I, Chunk> map = new HashMap<>();
	private MapRenderer renderer;
	private SpriteDrawPass carDraws;
	private ArrayList<Entity> entities = new ArrayList<>();
	private int alphaLoc, pposLoc;
	private REDebugDraw debug = new REDebugDraw();
	private float[] roll = new float[20];
	
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
		debug.initalize(renderer.camera);
		debug.appendFlags(DebugDraw.e_shapeBit);
		physics.setDebugDraw(debug);
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
		
		if (input.isKeyTapped(GLFW_KEY_Q)) {
			BodyDef bodyDef = new BodyDef();
			bodyDef.position.x = 0;
			bodyDef.position.y = 50;
			bodyDef.type = BodyType.DYNAMIC;
			bodyDef.linearVelocity.x = -20;
			PolygonShape circleShape = new PolygonShape();
			circleShape.setAsBox(1, 1);
			physics.createBody(bodyDef).createFixture(circleShape, 50);
		}

		renderer.camera.roll = -player.getPhysicsObject().getTransform().q.getAngle();
		physics.step(1/60f, 20, 15);
		renderer.camera.transX = -player.getPhysicsObject().getTransform().p.x;
		renderer.camera.transY = -player.getPhysicsObject().getTransform().p.y;
	}
	
	@Override
	public void render() {
		renderer.begin();
		for (Chunk chunk : map.values())
			chunk.render(renderer);
		renderer.end();
		
		carDraws.begin();
		for (Entity entity : entities)
			entity.render();
		player.render();
		carDraws.end();

		physics.drawDebugData();
	}
	
	public SpriteDrawPass getCarDrawer() {
		return carDraws;
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

package minusk.citysim.world;

import java.util.HashMap;

import minusk.citysim.entities.friendly.Player;
import minusk.render.interfaces.Renderable;

public class Map implements Renderable {
	public final Player player = new Player();
	
//	private final Point3I cache = new Point3I();
	
	private HashMap<Point3I, Chunk> map = new HashMap<>();
	private MapRenderer renderer;
	
	public Map() {
		renderer = new MapRenderer();
		map.put(new Point3I(0,0,0), new Chunk(0,0));
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

package minusk.citysim.world;

import minusk.render.math.Matrix2;
import minusk.render.math.Vec2;

class MapStructures {
	public static final class Road {
		public final float x1, y1, x2, y2, width;
		public final Vec2 perp = new Vec2();
		public final LaneLine[] lanelines;
		public Road(float x1, float y1, float x2, float y2, float width, LaneLine... lanes){
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
			this.width = width;
			perp.set(x2-x1, y2-y1);
			perp.normalize();
			perp.transform(new Matrix2(0,-1,1,0));
			lanelines = lanes;
		}
	}
	
	public static final class LaneLine {
		public final float offsetFromMiddle;
		public final Type type;
		public LaneLine(float offset, String type) {
			offsetFromMiddle = offset;
			this.type = Type.valueOf(type);
		}
		public static enum Type {
			SOLID_SOLID_YELLOW, DOTTED_SOLID_YELLOW, SOLID_DOTTED_YELLOW, DOTTED_YELLOW, SOLID_YELLOW,
			SOLID_WHITE, LONG_DOTTED_WHITE, MEDIUM_DOTTED_WHITE, SHORT_DOTTED_WHITE
		}
	}
}

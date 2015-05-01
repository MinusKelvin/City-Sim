package minusk.citysim.world;

//import static org.lwjgl.opengl.GL11.GL_REPEAT;
//import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
//import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
//import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
//import static org.lwjgl.opengl.GL11.glTexParameteri;
import minusk.citysim.world.MapStructures.LaneLine;
import minusk.render.graphics.Color;
import minusk.render.graphics.OrthoCamera;
import minusk.render.graphics.draw.ColorDrawPass;
import minusk.render.graphics.draw.TextureDrawPass;
import minusk.render.graphics.globjects.Texture;
import minusk.render.math.Easing;
import minusk.render.math.Matrix2;
import minusk.render.math.Vec2;

class MapRenderer {
	private TextureDrawPass roadPass;
	private ColorDrawPass colorPass;
	
	OrthoCamera camera = new OrthoCamera(0,1024/16f,576/16f,0);
	
	MapRenderer() {
		Texture roadTex = new Texture(128,128,1,false);
		roadTex.setTextureData(getClass().getResourceAsStream("/minusk/citysim/res/road.png"), 0);
		roadPass = new TextureDrawPass(roadTex);
		roadPass.camera = camera;
		colorPass = new ColorDrawPass();
		colorPass.camera = camera;
	}
	
	public void drawRoad(MapStructures.Road road) {
		Vec2 thing = new Vec2(road.perp);
		thing.scale(road.width/2);
		
		if (road.controlPoints.size() == 0)
			drawRoadBit(new Vec2(road.x1,road.y1),new Vec2(road.x2,road.y2),thing,0,road);
		else {
			float STEP = 0.0625f;
			Vec2[] controls = new Vec2[road.controlPoints.size()+2];
			controls[0] = new Vec2(road.x1, road.y1);
			for (int i = 1; i <= road.controlPoints.size(); i++)
				controls[i] = road.controlPoints.get(i-1);
			controls[road.controlPoints.size()+1] = new Vec2(road.x2,road.y2);
			
			float m = 0;
			Vec2 dir = Easing.bezier(controls, STEP);
			dir.sub(Easing.bezier(controls, 0));
			dir.normalize();
			dir.transform(new Matrix2(0,-1,1,0));
			dir.scale(road.width/2);
			
			for (float t = 0; t < 1; t+=STEP) {
				Vec2 p1 = Easing.bezier(controls, t);
				Vec2 p2 = Easing.bezier(controls, t+STEP);
				m += drawRoadBit(p1, p2, dir, m, road);
			}
		}

		thing.scale(2/road.width);
		for (MapStructures.LaneLine l : road.lanelines) {
			Vec2 offset = new Vec2(thing);
			switch (l.type) {
			case MEDIUM_DOTTED_WHITE:
			{
				offset.scale(l.offsetFromMiddle);
				Vec2 roadDir = new Vec2(road.perp);
				roadDir.transform(new Matrix2(0, 1, -1, 0));
				for (int m = 0; m < road.length; m += 5)
					colorPass.drawLine(road.x1+(roadDir.x*m)+offset.x, road.y1+(roadDir.y*m)+offset.y,
							road.x1+(roadDir.x*(m+2))+offset.x, road.y1+(roadDir.y*(m+2))+offset.y, 0.1f, Color.Gray87);
				break;
			}
			case SHORT_DOTTED_WHITE:
			{
				offset.scale(l.offsetFromMiddle);
				Vec2 roadDir = new Vec2(road.perp);
				roadDir.transform(new Matrix2(0, 1, -1, 0));
				for (int m = 0; m < road.length; m += 2)
					colorPass.drawLine(road.x1+(roadDir.x*m)+offset.x, road.y1+(roadDir.y*m)+offset.y,
							road.x1+(roadDir.x*(m+1))+offset.x, road.y1+(roadDir.y*(m+1))+offset.y, 0.1f, Color.Gray87);
				break;
			}
			case SOLID_YELLOW:
				offset.scale(l.offsetFromMiddle);
				colorPass.drawLine(road.x1+offset.x, road.y1+offset.y, road.x2+offset.x, road.y2+offset.y, 0.1f, new Color(0.87f, 0.75f, 0));
				break;
			case SOLID_SOLID_YELLOW:
			{
				Vec2 f = new Vec2(offset);
				offset.scale(l.offsetFromMiddle-0.1f);
				colorPass.drawLine(road.x1+offset.x, road.y1+offset.y, road.x2+offset.x, road.y2+offset.y, 0.1f, new Color(0.87f, 0.75f, 0));
				f.scale(l.offsetFromMiddle+0.1f);
				colorPass.drawLine(road.x1+f.x, road.y1+f.y, road.x2+f.x, road.y2+f.y, 0.1f, new Color(0.87f, 0.75f, 0));
				break;
			}
			case SOLID_DOTTED_YELLOW:
			{
				Vec2 f = new Vec2(offset);
				offset.scale(l.offsetFromMiddle-0.1f);
				Vec2 roadDir = new Vec2(road.perp);
				roadDir.transform(new Matrix2(0, 1, -1, 0));
				colorPass.drawLine(road.x1+offset.x, road.y1+offset.y, road.x2+offset.x, road.y2+offset.y, 0.1f, new Color(0.87f, 0.75f, 0));
				f.scale(l.offsetFromMiddle+0.1f);
				for (int m = 0; m < road.length; m += 5)
					colorPass.drawLine(road.x1+(roadDir.x*m)+f.x, road.y1+(roadDir.y*m)+f.y,
							road.x1+(roadDir.x*(m+2))+f.x, road.y1+(roadDir.y*(m+2))+f.y, 0.1f, new Color(0.87f, 0.75f, 0));
				break;
			}
			case DOTTED_SOLID_YELLOW:
			{
				Vec2 f = new Vec2(offset);
				offset.scale(l.offsetFromMiddle-0.1f);
				Vec2 roadDir = new Vec2(road.perp);
				roadDir.transform(new Matrix2(0, 1, -1, 0));
				for (int m = 0; m < road.length; m += 5)
					colorPass.drawLine(road.x1+(roadDir.x*m)+offset.x, road.y1+(roadDir.y*m)+offset.y,
							road.x1+(roadDir.x*(m+2))+offset.x, road.y1+(roadDir.y*(m+2))+offset.y, 0.1f, new Color(0.87f, 0.75f, 0));
				f.scale(l.offsetFromMiddle+0.1f);
				colorPass.drawLine(road.x1+f.x, road.y1+f.y, road.x2+f.x, road.y2+f.y, 0.1f, new Color(0.87f, 0.75f, 0));
				break;
			}
			case DOTTED_YELLOW:
				break;
			default:
				break;
			}
		}
	}
	
	private float drawRoadBit(Vec2 start, Vec2 end, Vec2 oldDir, float m, MapStructures.Road road) {
		float x1 = start.x + oldDir.x;
		float y1 = start.y + oldDir.y;

		float x2 = start.x - oldDir.x;
		float y2 = start.y - oldDir.y;
		
		Vec2 olderDir = new Vec2(oldDir);
		oldDir.set(end);
		oldDir.sub(start);
		float inc = oldDir.length();
		oldDir.normalize();
		oldDir.transform(new Matrix2(0,-1,1,0));
		oldDir.scale(road.width/2);
		
		float x3 = end.x + oldDir.x;
		float y3 = end.y + oldDir.y;

		float x4 = end.x - oldDir.x;
		float y4 = end.y - oldDir.y;

		roadPass.drawTriangle(x1, y1, x1/8, y1/8, x2, y2, x2/8, y2/8, x3, y3, x3/8, y3/8);
		roadPass.drawTriangle(x4, y4, x4/8, y4/8, x2, y2, x2/8, y2/8, x3, y3, x3/8, y3/8);
		
		oldDir.scale(2/road.width);
		olderDir.scale(2/road.width);
		for (LaneLine l : road.lanelines) {
			Vec2 offsetstart = new Vec2(olderDir);
			Vec2 offsetend = new Vec2(oldDir);
			switch (l.type) {
			case SOLID_WHITE:
				offsetstart.scale(l.offsetFromMiddle);
				offsetend.scale(l.offsetFromMiddle);
				colorPass.drawLine(start.x+offsetstart.x, start.y+offsetstart.y, end.x+offsetend.x, end.y+offsetend.y, 0.1f, Color.Gray87);
				break;
			case LONG_DOTTED_WHITE:
			{
				offsetstart.scale(l.offsetFromMiddle);
				offsetend.scale(l.offsetFromMiddle);
				Vec2 roadDir = new Vec2(oldDir);
				roadDir.transform(new Matrix2(0, 1, -1, 0));
				for (float i = m; i < m+inc; i += 10)
					colorPass.drawLine(start.x+(roadDir.x*(i-m))+offsetstart.x, start.y+(roadDir.y*(i-m))+offsetstart.y,
							start.x+(roadDir.x*((i-m)+5))+offsetend.x, start.y+(roadDir.y*((i-m)+5))+offsetend.y, 0.1f, Color.Gray87);
				break;
			}
//			case MEDIUM_DOTTED_WHITE:
//			{
//				offset.scale(l.offsetFromMiddle);
//				Vec2 roadDir = new Vec2(road.perp);
//				roadDir.transform(new Matrix2(0, 1, -1, 0));
//				for (int m = 0; m < road.length; m += 5)
//					colorPass.drawLine(road.x1+(roadDir.x*m)+offset.x, road.y1+(roadDir.y*m)+offset.y,
//							road.x1+(roadDir.x*(m+2))+offset.x, road.y1+(roadDir.y*(m+2))+offset.y, 0.1f, Color.Gray87);
//				break;
//			}
//			case SHORT_DOTTED_WHITE:
//			{
//				offset.scale(l.offsetFromMiddle);
//				Vec2 roadDir = new Vec2(road.perp);
//				roadDir.transform(new Matrix2(0, 1, -1, 0));
//				for (int m = 0; m < road.length; m += 2)
//					colorPass.drawLine(road.x1+(roadDir.x*m)+offset.x, road.y1+(roadDir.y*m)+offset.y,
//							road.x1+(roadDir.x*(m+1))+offset.x, road.y1+(roadDir.y*(m+1))+offset.y, 0.1f, Color.Gray87);
//				break;
//			}
//			case SOLID_YELLOW:
//				offset.scale(l.offsetFromMiddle);
//				colorPass.drawLine(road.x1+offset.x, road.y1+offset.y, road.x2+offset.x, road.y2+offset.y, 0.1f, new Color(0.87f, 0.75f, 0));
//				break;
//			case SOLID_SOLID_YELLOW:
//			{
//				Vec2 f = new Vec2(offset);
//				offset.scale(l.offsetFromMiddle-0.1f);
//				colorPass.drawLine(road.x1+offset.x, road.y1+offset.y, road.x2+offset.x, road.y2+offset.y, 0.1f, new Color(0.87f, 0.75f, 0));
//				f.scale(l.offsetFromMiddle+0.1f);
//				colorPass.drawLine(road.x1+f.x, road.y1+f.y, road.x2+f.x, road.y2+f.y, 0.1f, new Color(0.87f, 0.75f, 0));
//				break;
//			}
//			case SOLID_DOTTED_YELLOW:
//			{
//				Vec2 f = new Vec2(offset);
//				offset.scale(l.offsetFromMiddle-0.1f);
//				Vec2 roadDir = new Vec2(road.perp);
//				roadDir.transform(new Matrix2(0, 1, -1, 0));
//				colorPass.drawLine(road.x1+offset.x, road.y1+offset.y, road.x2+offset.x, road.y2+offset.y, 0.1f, new Color(0.87f, 0.75f, 0));
//				f.scale(l.offsetFromMiddle+0.1f);
//				for (int m = 0; m < road.length; m += 5)
//					colorPass.drawLine(road.x1+(roadDir.x*m)+f.x, road.y1+(roadDir.y*m)+f.y,
//							road.x1+(roadDir.x*(m+2))+f.x, road.y1+(roadDir.y*(m+2))+f.y, 0.1f, new Color(0.87f, 0.75f, 0));
//				break;
//			}
//			case DOTTED_SOLID_YELLOW:
//			{
//				Vec2 f = new Vec2(offset);
//				offset.scale(l.offsetFromMiddle-0.1f);
//				Vec2 roadDir = new Vec2(road.perp);
//				roadDir.transform(new Matrix2(0, 1, -1, 0));
//				for (int m = 0; m < road.length; m += 5)
//					colorPass.drawLine(road.x1+(roadDir.x*m)+offset.x, road.y1+(roadDir.y*m)+offset.y,
//							road.x1+(roadDir.x*(m+2))+offset.x, road.y1+(roadDir.y*(m+2))+offset.y, 0.1f, new Color(0.87f, 0.75f, 0));
//				f.scale(l.offsetFromMiddle+0.1f);
//				colorPass.drawLine(road.x1+f.x, road.y1+f.y, road.x2+f.x, road.y2+f.y, 0.1f, new Color(0.87f, 0.75f, 0));
//				break;
//			}
//			case DOTTED_YELLOW:
//				break;
			default:
				break;
			}
		}
		oldDir.scale(road.width/2);
		return inc;
	}
	
	void begin() {
		roadPass.begin();
		colorPass.begin();
	}
	
	void end(){
		roadPass.end();
		colorPass.end();
	}
}

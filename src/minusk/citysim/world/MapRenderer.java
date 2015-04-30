package minusk.citysim.world;

//import static org.lwjgl.opengl.GL11.GL_REPEAT;
//import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
//import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
//import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
//import static org.lwjgl.opengl.GL11.glTexParameteri;
import minusk.render.graphics.Color;
import minusk.render.graphics.OrthoCamera;
import minusk.render.graphics.draw.ColorDrawPass;
import minusk.render.graphics.draw.TextureDrawPass;
import minusk.render.graphics.filters.BlendFunc;
import minusk.render.graphics.globjects.Texture;
import minusk.render.math.Matrix2;
import minusk.render.math.Vec2;

class MapRenderer {
	private TextureDrawPass roadPass;
	private ColorDrawPass colorPass;
	
	OrthoCamera camera = new OrthoCamera(0,1024/16f,576/16f,0);
	
	MapRenderer() {
		Texture roadTex = new Texture(128,128,1,false);
		roadTex.setTextureData(getClass().getResourceAsStream("/minusk/citysim/res/road.png"), 0);
//		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
//		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		roadPass = new TextureDrawPass(roadTex);
		roadPass.camera = camera;
		colorPass = new ColorDrawPass();
		colorPass.camera = camera;
		colorPass.setBlendFunc(BlendFunc.ADDITIVE);
	}
	
	public void drawRoad(MapStructures.Road road) {
		Vec2 thing = new Vec2(road.perp);
		thing.scale(road.width/2);
		float x1 = road.x1 + thing.x;
		float y1 = road.y1 + thing.y;

		float x2 = road.x1 - thing.x;
		float y2 = road.y1 - thing.y;

		float x3 = road.x2 + thing.x;
		float y3 = road.y2 + thing.y;

		float x4 = road.x2 - thing.x;
		float y4 = road.y2 - thing.y;

		roadPass.drawTriangle(x1, y1, x1/8, y1/8, x2, y2, x2/8, y2/8, x3, y3, x3/8, y3/8);
		roadPass.drawTriangle(x4, y4, x4/8, y4/8, x2, y2, x2/8, y2/8, x3, y3, x3/8, y3/8);
		
		thing.scale(2/road.width);
		for (MapStructures.LaneLine l : road.lanelines) {
			Vec2 offset = new Vec2(thing);
			switch (l.type) {
			case SOLID_WHITE:
				offset.scale(l.offsetFromMiddle);
				colorPass.drawLine(road.x1+offset.x, road.y1+offset.y, road.x2+offset.x, road.y2+offset.y, 0.1f, Color.Gray87);
				break;
			case LONG_DOTTED_WHITE:
			{
				offset.scale(l.offsetFromMiddle);
				Vec2 roadDir = new Vec2(road.perp);
				roadDir.transform(new Matrix2(0, 1, -1, 0));
				for (int m = 0; m < road.length; m += 10)
					colorPass.drawLine(road.x1+(roadDir.x*m)+offset.x, road.y1+(roadDir.y*m)+offset.y,
							road.x1+(roadDir.x*(m+5))+offset.x, road.y1+(roadDir.y*(m+5))+offset.y, 0.1f, Color.Gray87);
				break;
			}
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
	
	void begin() {
		roadPass.begin();
		colorPass.begin();
	}
	
	void end(){
		roadPass.end();
		colorPass.end();
	}
}

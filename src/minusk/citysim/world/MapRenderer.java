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
import minusk.render.math.Vec2;

class MapRenderer {
	private TextureDrawPass roadPass;
	private ColorDrawPass colorPass;
	
	MapRenderer() {
		Texture roadTex = new Texture(128,128,1,false);
		roadTex.setTextureData(getClass().getResourceAsStream("/minusk/citysim/res/road.png"), 0);
//		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
//		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		roadPass = new TextureDrawPass(roadTex);
		roadPass.camera = new OrthoCamera(0,1024/16f,576/16f,0);
		colorPass = new ColorDrawPass();
		colorPass.camera = roadPass.camera;
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
			offset.scale(l.offsetFromMiddle);
			switch (l.type) {
			case SOLID_WHITE:
				colorPass.drawLine(road.x1+offset.x, road.y1+offset.y, road.x2+offset.x, road.y2+offset.y, 0.1f, Color.Gray87);
				break;
			case LONG_DOTTED_WHITE:
				break;
			case MEDIUM_DOTTED_WHITE:
				break;
			case SHORT_DOTTED_WHITE:
				break;
			case SOLID_YELLOW:
				colorPass.drawLine(road.x1+offset.x, road.y1+offset.y, road.x2+offset.x, road.y2+offset.y, 0.1f, new Color(0.87f, 0.75f, 0));
				break;
			case SOLID_SOLID_YELLOW:
				break;
			case SOLID_DOTTED_YELLOW:
				break;
			case DOTTED_SOLID_YELLOW:
				break;
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

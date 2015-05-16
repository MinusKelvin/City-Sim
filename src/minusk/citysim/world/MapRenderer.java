package minusk.citysim.world;


import java.util.Arrays;

import minusk.citysim.Main;
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
	private int x,y;
	
	OrthoCamera camera = new OrthoCamera(
			-Main.game.getResolutionX()/32f,
			Main.game.getResolutionX()/32f,
			Main.game.getResolutionY()/32f,
			-Main.game.getResolutionY()/32f);
	
	MapRenderer() {
		Texture roadTex = new Texture(128,128,1,false,0);
		roadTex.setTextureData(getClass().getResourceAsStream("/minusk/citysim/res/road.png"), 0);
		roadPass = new TextureDrawPass(roadTex);
		roadPass.camera = camera;
		colorPass = new ColorDrawPass();
		colorPass.camera = camera;
		camera.near = 0;
		camera.far = 15;
	}
	
	public void drawRoad(MapStructures.Road road, boolean outputInfo) {
		if (road.controlPoints.size() == 0) {
			Vec2 thing = new Vec2(road.perp);
			thing.scale(road.width/2);
			float[] dists = Arrays.copyOf(road.laneDistances, road.laneDistances.length);
			drawRoadBit(new Vec2(road.x1,road.y1),new Vec2(road.x2,road.y2),thing,
					dists,road,null);
			thing.scale(2/road.width);
			thing.transform(new Matrix2(0, 1, -1, 0));
			if (outputInfo) {
				System.out.println("Road direction: " + thing.x + ", " + thing.y);
				for (int i = 0; i < dists.length; i++)
					System.out.println("Road laneline distance " + i + ": " + dists[i]);
			}
		} else {
			float STEP = 1f/128;
			Vec2[] controls = new Vec2[road.controlPoints.size()+2];
			controls[0] = new Vec2(road.x1, road.y1);
			for (int i = 1; i <= road.controlPoints.size(); i++)
				controls[i] = road.controlPoints.get(i-1);
			controls[road.controlPoints.size()+1] = new Vec2(road.x2,road.y2);
			
			Vec2 dir;
			if (road.initialDirection == null) {
				dir = Easing.bezier(controls, STEP);
				dir.sub(controls[0]);
				dir.normalize();
			} else
				dir = new Vec2(road.initialDirection);
			dir.transform(new Matrix2(0,-1,1,0));
			dir.scale(road.width/2);
			
			float[] perlaneDistances = Arrays.copyOf(road.laneDistances, road.laneDistances.length);
			for (float t = 0; t < 1; t+=STEP) {
				Vec2 p1 = Easing.bezier(controls, t);
				Vec2 p2 = Easing.bezier(controls, Math.min(1, t+STEP));
				drawRoadBit(p1, p2, dir, perlaneDistances, road,
						t+STEP>=1&&road.endDirection!=null?new Vec2(road.endDirection):null);
			}
			dir.scale(2/road.width);
			dir.transform(new Matrix2(0, 1, -1, 0));
			if (outputInfo) {
				System.out.println("Road direction: " + dir.x + ", " + dir.y);
				for (int i = 0; i < perlaneDistances.length; i++)
					System.out.println("Road laneline distance " + i + ": " + perlaneDistances[i]);
			}
		}
	}
	
	private void drawRoadBit(Vec2 start, Vec2 end, Vec2 oldDir, float[] distances, MapStructures.Road road, Vec2 newDirOverride) {
		float x1 = start.x + oldDir.x;
		float y1 = start.y + oldDir.y;

		float x2 = start.x - oldDir.x;
		float y2 = start.y - oldDir.y;
		
		Vec2 olderDir = new Vec2(oldDir);
		if (newDirOverride == null) {
			oldDir.set(end);
			oldDir.sub(start);
			oldDir.normalize();
		} else
			oldDir.set(newDirOverride);
		oldDir.transform(new Matrix2(0,-1,1,0));
		oldDir.scale(road.width/2);
		
		float x3 = end.x + oldDir.x;
		float y3 = end.y + oldDir.y;

		float x4 = end.x - oldDir.x;
		float y4 = end.y - oldDir.y;

		roadPass.drawTriangle(x1+x, y1+y, x1/8+x, y1/8+y, x2+x, y2+y, x2/8+x, y2/8+y, x3+x, y3+y, x3/8+x, y3/8+y, road.z);
		roadPass.drawTriangle(x4+x, y4+y, x4/8+x, y4/8+y, x2+x, y2+y, x2/8+x, y2/8+y, x3+x, y3+y, x3/8+x, y3/8+y, road.z);
		
		oldDir.scale(2/road.width);
		olderDir.scale(2/road.width);
		for (int i = 0; i < road.lanelines.length; i++) {
			Vec2 offsetstart = new Vec2(olderDir);
			Vec2 offsetend = new Vec2(oldDir);
			LaneLine l = road.lanelines[i];
			float m = distances[i];
			offsetstart.scale(l.offsetFromMiddle);
			offsetend.scale(l.offsetFromMiddle);
			
			Vec2 temp = new Vec2(offsetend);
			temp.add(end);
			Vec2 temp2 = new Vec2(offsetstart);
			temp2.add(start);
			temp.sub(temp2);
			float inc = temp.length();
			temp.scale(1/inc);
			
			switch (l.type) {
			case SOLID_WHITE:
				colorPass.drawLine(start.x+offsetstart.x+x, start.y+offsetstart.y+y, end.x+offsetend.x+x, end.y+offsetend.y+y, 0.1f, road.z, Color.Gray87);
				break;
			case LONG_DOTTED_WHITE:
			{
				Vec2 roadDir = new Vec2(temp);
				
				float off = 0;
				float enddist = m+inc;
				while (true) {
					float linestartdist = Math.max(m, round(m/20+off,0.3f)*20);
					if (linestartdist > enddist)
						break;
					if (enddist <= round(m/20+off,0.3f)*20+6) {
						colorPass.drawLine(start.x+offsetstart.x+roadDir.x*(linestartdist-m)+x, start.y+offsetstart.y+roadDir.y*(linestartdist-m)+y,
								end.x+offsetend.x+x, end.y+offsetend.y+y, 0.1f, road.z, Color.Gray87);
						break;
					} else {
						float lineenddist = round(m/20+off,0.3f)*20+6;
						colorPass.drawLine(start.x+offsetstart.x+roadDir.x*(linestartdist-m)+x, start.y+offsetstart.y+roadDir.y*(linestartdist-m)+y,
								start.x+offsetstart.x+roadDir.x*(lineenddist-m)+x, start.y+offsetstart.y+roadDir.y*(lineenddist-m)+y, 0.1f, road.z, Color.Gray87);
					}
					off++;
				}
				
				break;
			}
			case MEDIUM_DOTTED_WHITE:
			{
				Vec2 roadDir = new Vec2(temp);

				float off = 0;
				float enddist = m+inc;
				while (true) {
					float linestartdist = Math.max(m, round(m/14+off,0.28f)*14);
					if (linestartdist > enddist)
						break;
					if (enddist <= round(m/14+off,0.28f)*14+4) {
						colorPass.drawLine(start.x+offsetstart.x+roadDir.x*(linestartdist-m)+x, start.y+offsetstart.y+roadDir.y*(linestartdist-m)+y,
								end.x+offsetend.x+x, end.y+offsetend.y+y, 0.1f, road.z, Color.Gray87);
						break;
					} else {
						float lineenddist = round(m/14+off,0.28f)*14+4;
						colorPass.drawLine(start.x+offsetstart.x+roadDir.x*(linestartdist-m)+x, start.y+offsetstart.y+roadDir.y*(linestartdist-m)+y,
								start.x+offsetstart.x+roadDir.x*(lineenddist-m)+x, start.y+offsetstart.y+roadDir.y*(lineenddist-m)+y, 0.1f, road.z, Color.Gray87);
					}
					off++;
				}

				break;
			}
			case SHORT_DOTTED_WHITE:
			{
				Vec2 roadDir = new Vec2(temp);

				float off = 0;
				float enddist = m+inc;
				while (true) {
					float linestartdist = Math.max(m, round(m/5+off,0.3f)*5);
					if (linestartdist > enddist)
						break;
					if (enddist <= round(m/5+off,0.3f)*5+1.5f) {
						colorPass.drawLine(start.x+offsetstart.x+roadDir.x*(linestartdist-m)+x, start.y+offsetstart.y+roadDir.y*(linestartdist-m)+y,
								end.x+offsetend.x+x, end.y+offsetend.y+y, 0.1f, road.z, Color.Gray87);
						break;
					} else {
						float lineenddist = round(m/5+off,0.3f)*5+1.5f;
						colorPass.drawLine(start.x+offsetstart.x+roadDir.x*(linestartdist-m)+x, start.y+offsetstart.y+roadDir.y*(linestartdist-m)+y,
								start.x+offsetstart.x+roadDir.x*(lineenddist-m)+x, start.y+offsetstart.y+roadDir.y*(lineenddist-m)+y, 0.1f, road.z, Color.Gray87);
					}
					off++;
				}

				break;
			}
			case SOLID_YELLOW:
				colorPass.drawLine(start.x+offsetstart.x+x, start.y+offsetstart.y+y,
						end.x+offsetend.x+x,end.y+offsetend.y+y, 0.1f, road.z, new Color(0.87f, 0.75f, 0));
				break;
			case DOTTED_YELLOW:
			{
				Vec2 roadDir = new Vec2(temp);

				float off = 0;
				float enddist = m+inc;
				while (true) {
					float linestartdist = Math.max(m, round(m/14+off,0.28f)*14);
					if (linestartdist > enddist)
						break;
					if (enddist <= round(m/14+off,0.28f)*14+4) {
						colorPass.drawLine(start.x+offsetstart.x+roadDir.x*(linestartdist-m)+x, start.y+offsetstart.y+roadDir.y*(linestartdist-m)+y,
								end.x+offsetend.x+x, end.y+offsetend.y+y, 0.1f, road.z, Color.Gray87);
						break;
					} else {
						float lineenddist = round(m/14+off,0.28f)*14+4;
						colorPass.drawLine(start.x+offsetstart.x+roadDir.x*(linestartdist-m)+x, start.y+offsetstart.y+roadDir.y*(linestartdist-m)+y,
								start.x+offsetstart.x+roadDir.x*(lineenddist-m)+x, start.y+offsetstart.y+roadDir.y*(lineenddist-m)+y,
								0.1f, road.z, new Color(0.87f, 0.75f, 0));
					}
					off++;
				}

				break;
			}
			default:
				break;
			}
			distances[i] += inc;
		}
		oldDir.scale(road.width/2);
	}
	
	void begin() {
		roadPass.begin();
		colorPass.begin();
	}
	
	void end() {
		for (int i = (int) -camera.transY-50; i < -camera.transY+50; i++)
			colorPass.drawLine(0, i, 100, i, 0.05f, Color.Gray50);
		
		roadPass.end();
		colorPass.end();
	}
	
	private static int round(float f, float half) {
		return (int) Math.floor(f + (1-half));
	}
	
	public void setChunkOffset(int x, int y) {
		this.x = x*100;
		this.y = y*100;
	}
}

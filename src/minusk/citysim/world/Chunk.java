package minusk.citysim.world;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

import minusk.citysim.Main;
import minusk.citysim.world.MapStructures.Road;
import minusk.render.math.Vec2;

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

public class Chunk {
	public final boolean useable;
	public final int x,y;
	private final ArrayList<MapStructures.Road> roads = new ArrayList<>();
	private boolean outputRoadInformation = true;
	private Body barriers;
	
	public Chunk(int x, int y) {
		this.x = x;
		this.y = y;
		File f = new File("map");
		if (!f.exists())
			throw new IllegalStateException("No /map/ folder");
		if (!f.isDirectory())
			throw new IllegalStateException("/map/ is not a directory");
		
		f = new File("map/c"+hex(x));
		if (!f.exists() || !f.isDirectory()) {
			useable = false;
			System.err.println("Cannot find directory /map/c"+hex(x));
			return;
		}
		
		f = new File("map/c"+hex(x)+"/c"+hex(y)+".cnk");
		if (!f.exists() || !f.isFile()) {
			useable = false;
			System.err.println("Cannot find file /map/c"+hex(x)+"/c"+hex(y)+".cnk");
			return;
		}
		
		BodyDef def = new BodyDef();
		def.position.x = x*100;
		def.position.y = y*100;
		def.type = BodyType.STATIC;
		barriers = Main.game.getMap().physics.createBody(def);
		
		FixtureDef fDef = new FixtureDef();
		ChainShape shape = new ChainShape();
		fDef.shape = shape;
		int layer = 0;
		
		try (Scanner s = new Scanner(new BufferedInputStream(new FileInputStream(f)))) {
			while (s.hasNext()) {
				String input = s.next();
				switch (input) {
				case "ROAD":
				{
					float x1 = s.nextFloat();
					float y1 = s.nextFloat();
					float idirx = Float.NaN;
					float idiry = Float.NaN;
					String in = s.next();
					if (in.equals("DIR")) {
						idirx = s.nextFloat();
						idiry = s.nextFloat();
						in = s.next();
					}
					ArrayList<Vec2> controls = new ArrayList<>();
					while (in.equals("CURVEPOINT")) {
						controls.add(new Vec2(s.nextFloat(), s.nextFloat()));
						in = s.next();
					}
					float x2 = Float.parseFloat(in);
					float y2 = s.nextFloat();
					in = s.next();
					float endx = Float.NaN;
					float endy = Float.NaN;
					if (in.equals("DIR")) {
						endx = s.nextFloat();
						endy = s.nextFloat();
						in = s.next();
					}
					float width = Float.parseFloat(in);
					MapStructures.LaneLine[] lanes = new MapStructures.LaneLine[s.nextInt()];
					float[] dists = new float[lanes.length];
					in = s.next();
					for (int i = 0; i < lanes.length; i++) {
						lanes[i] = new MapStructures.LaneLine(Float.parseFloat(in), s.next());
						in = s.next();
						if (in.equals("DIST")) {
							dists[i] = s.nextFloat();
							in = s.next();
						}
					}
					roads.add(new MapStructures.Road(x1,y1,idirx!=idirx?null:new Vec2(idirx,idiry),
							x2,y2,endx!=endx?null:new Vec2(endx,endy),width,lanes,dists,controls, layer));
					break;
				}
				case "WALL":
				{
					String in = s.next();
					boolean isloop = !in.equals("OPEN");
					org.jbox2d.common.Vec2[] points = new org.jbox2d.common.Vec2[s.nextInt()];
					for (int i = 0; i < points.length; i++)
						points[i] = new org.jbox2d.common.Vec2(s.nextFloat(), s.nextFloat());
					if (isloop)
						shape.createLoop(points, points.length);
					else
						shape.createChain(points, points.length);
					barriers.createFixture(fDef);
					break;
				}
				case "LAYER":
				{
					layer = s.nextInt();
					break;
				}
				default:
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			useable = false;
			return;
		}
		roads.sort(new Comparator<MapStructures.Road>() {
			@Override
			public int compare(MapStructures.Road o1, MapStructures.Road o2) {
				return (int) Math.signum(o1.z-o2.z);
			}
		});
		useable = true;
	}
	
	private int lastIndex;
	private boolean doDraw = true;
	public void render(MapRenderer renderer, int stage) {
		if (!useable)
			return;
		
		renderer.setChunkOffset(x, y);
		
		if (stage != -1) {
			lastIndex = 0;
			doDraw = true;
		}
		
		if (outputRoadInformation)
			System.out.println("Chunk " + x + ", " + y);
		
		if (doDraw) {
			doDraw = false;
			for (int i = lastIndex; i < roads.size(); i++) {
				Road road = roads.get(i);
				if (stage != -1 && stage < road.z) {
					lastIndex = i;
					doDraw = true;
					break;
				}
				
				if (outputRoadInformation)
					System.out.println("Road number " + i);
				
				renderer.drawRoad(road,outputRoadInformation);
			}
		}
		outputRoadInformation = stage != -1 && outputRoadInformation;
	}
	
	private static String hex(int i) {
		return (i<0?"-":"") + Integer.toHexString(Math.abs(i)).toUpperCase();
	}
}

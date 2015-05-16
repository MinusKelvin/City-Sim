package minusk.citysim.entities.friendly;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import minusk.citysim.Main;
import minusk.citysim.entities.Car;
import minusk.citysim.entities.Car.CarDef;
import minusk.citysim.entities.Car.TireDef;
import minusk.citysim.entities.Entity;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

public class Player extends Entity {
	private Car car;
	
	public void init() {
		CarDef def = new CarDef();
		def.acceleration = 28/6f;
		def.height = 4.8f;
		def.width = 1.9f;
		def.tires = new TireDef[4];
		def.tires[0] = new TireDef();
		def.tires[0].canTurn = true;
		def.tires[0].drive = 1;
		def.tires[0].localx = .775f;
		def.tires[0].localy = 1.5f;
		def.tires[1] = new TireDef();
		def.tires[1].canTurn = true;
		def.tires[1].drive = 1;
		def.tires[1].localx = -.775f;
		def.tires[1].localy = 1.5f;
		def.tires[2] = new TireDef();
		def.tires[2].stoppedByHandbrake = true;
		def.tires[2].drive = 0;
		def.tires[2].localx = .775f;
		def.tires[2].localy = -1.5f;
		def.tires[3] = new TireDef();
		def.tires[3].stoppedByHandbrake = true;
		def.tires[3].drive = 0;
		def.tires[3].localx = -.775f;
		def.tires[3].localy = -1.5f;
		def.topspeed = 55;
		def.turnRadius = 0.6f;
		def.mass = 1814;
		car = new Car(def);
	}
	
	@Override
	public void update() {
		boolean left = Main.game.getInput().isKeyDown(GLFW_KEY_A);
		boolean right = Main.game.getInput().isKeyDown(GLFW_KEY_D);
		if (!Boolean.logicalXor(left, right))
			car.turn(0);
		else if (left)
			car.turn(1);
		else
			car.turn(-1);
		
		boolean forward = Main.game.getInput().isKeyDown(GLFW_KEY_W);
		boolean backward = Main.game.getInput().isKeyDown(GLFW_KEY_S);
		if (!Boolean.logicalXor(forward, backward))
			car.drive(0);
		else if (forward)
			car.drive(1);
		else
			car.drive(-0.8f);
		
		car.handbrake(Main.game.getInput().isKeyDown(GLFW_KEY_SPACE));
		
		car.update();
		
		float mps = car.getChassis().getLinearVelocity().length();
		System.out.println("Speed: " + mps*3600/1000 + "km/h");
	}
	
	public void render() {
		car.render();
	}
	
	public Body getPhysicsObject() {
		return car.getChassis();
	}
	
	@Override
	public Vec2 getCenter() {
		return car.getCenter();
	}
}

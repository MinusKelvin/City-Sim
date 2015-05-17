package minusk.citysim.entities;

import java.util.Arrays;

import minusk.citysim.Main;
import minusk.render.util.Util;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.jbox2d.dynamics.joints.WeldJointDef;

public class Car extends Entity {
	private final Body chassis;
	private final Body[] tires;
	private final float topspeed, turnRadius, force, totalMass;
	private final float[] prevTurning = new float[10], tireTraction, tireAccelerationCoefficents;
	private final boolean[] tiresSkidding, tireCanBrake, tiresCanTurn;
	private final Vec2[] tiresLocalPoints;
	private float turning = 0, enginePower = 0;
	private boolean handbrakeApplied;
	
	public Car(CarDef carDef) {
		this.topspeed = carDef.topspeed;
		this.turnRadius = carDef.turnRadius;
		float totalMass = carDef.mass;
		
		// Chassis
		BodyDef bDef = new BodyDef();
		bDef.position.x = carDef.x;
		bDef.position.y = carDef.y;
		bDef.angle = carDef.angle;
		bDef.type = BodyType.DYNAMIC;
		chassis = Main.game.getMap().physics.createBody(bDef);
		
		PolygonShape shape = new PolygonShape();
		FixtureDef fDef = new FixtureDef();
		shape.setAsBox(carDef.width/2, carDef.height/2);
		fDef.shape = shape;
		fDef.density = carDef.mass / (carDef.width*carDef.height);
		chassis.createFixture(fDef);
		chassis.setUserData(this);
		
		// Tires
		RevoluteJointDef rjDef = new RevoluteJointDef();
		rjDef.bodyA = chassis;
		rjDef.collideConnected = false;
		
		WeldJointDef wjDef = new WeldJointDef();
		wjDef.bodyA = chassis;
		wjDef.collideConnected = false;
		
		fDef.filter.maskBits = 0;
		
		tires = new Body[carDef.tires.length];
		tireAccelerationCoefficents = new float[tires.length];
		tireTraction = new float[tires.length];
		tiresSkidding = new boolean[tires.length];
		tireCanBrake = new boolean[tires.length];
		tiresCanTurn = new boolean[tires.length];
		tiresLocalPoints = new Vec2[tires.length];
		
		for (int i = 0; i < tires.length; i++) {
			shape.setAsBox(carDef.tires[i].width/2, carDef.tires[i].height/2);
			
			fDef.density = carDef.tires[i].mass / (carDef.tires[i].width*carDef.tires[i].height);
			
			totalMass += carDef.tires[i].mass;
			
			tiresLocalPoints[i] = new Vec2(carDef.tires[i].localx, carDef.tires[i].localy);
			
			bDef.position.x = carDef.x + tiresLocalPoints[i].x;
			bDef.position.y = carDef.y + tiresLocalPoints[i].y;
			
			tires[i] = Main.game.getMap().physics.createBody(bDef);
			tires[i].createFixture(fDef);
			tires[i].setUserData(this);
			
			if (tiresCanTurn[i] = carDef.tires[i].canTurn) {
				rjDef.bodyB = tires[i];
				rjDef.localAnchorA.x = carDef.tires[i].localx;
				rjDef.localAnchorA.y = carDef.tires[i].localy;
				Main.game.getMap().physics.createJoint(rjDef);
			} else {
				wjDef.bodyB = tires[i];
				wjDef.localAnchorA.x = carDef.tires[i].localx;
				wjDef.localAnchorA.y = carDef.tires[i].localy;
				Main.game.getMap().physics.createJoint(wjDef);
			}
			
			tireTraction[i] = carDef.tires[i].traction;
			tireAccelerationCoefficents[i] = carDef.tires[i].drive;
			tireCanBrake[i] = carDef.tires[i].stoppedByHandbrake;
		}
		
		force = totalMass * carDef.acceleration / Util.sum(tireAccelerationCoefficents);
		this.totalMass = totalMass;
	}
	
	@Override
	public void update() {
		prevTurning[9] = prevTurning[8];
		prevTurning[8] = prevTurning[7];
		prevTurning[7] = prevTurning[6];
		prevTurning[6] = prevTurning[5];
		prevTurning[5] = prevTurning[4];
		prevTurning[4] = prevTurning[3];
		prevTurning[3] = prevTurning[2];
		prevTurning[2] = prevTurning[1];
		prevTurning[1] = prevTurning[0];
		prevTurning[0] = turning;
		
		float angle = Util.sum(prevTurning) / prevTurning.length;
		for (int i = 0; i < tires.length; i++) {
			if (tiresCanTurn[i])
				tires[i].setTransform(chassis.getWorldPoint(tiresLocalPoints[i]), angle*1/Math.max(tires[i].getLinearVelocity().length()/10,1)+chassis.getAngle());
			tires[i].applyForceToCenter(tires[i].getWorldVector(new Vec2(0, 1)).mulLocal(enginePower*tireAccelerationCoefficents[i]));
		}
		for (int i = 0; i < tires.length; i++)
			recalcVel(i);
		
		float speed = chassis.getLinearVelocity().length();
		if (speed > topspeed)
			chassis.setLinearVelocity(chassis.getLinearVelocity().mul(topspeed/speed));
	}
	
	private void recalcVel(int index) {
		Body tire = tires[index];
		Vec2 lateral;
		if (tireCanBrake[index] && handbrakeApplied) {
			lateral = new Vec2(tire.getLinearVelocity());
			tiresSkidding[index] = true;
		} else {
			lateral = tire.getWorldVector(new Vec2(1, 0));
			lateral.mulLocal(Vec2.dot(lateral, tire.getLinearVelocity()));
			
			if (enginePower == 0) {
				Vec2 forward = tire.getWorldVector(new Vec2(0, 1));
				forward.mulLocal(Vec2.dot(forward, tire.getLinearVelocity()));
				float speed = forward.normalize();
				float drag = -0.08f * Math.min(speed, 10) * totalMass;
				if (speed < 0.5f)
					drag = -speed * totalMass;
				tire.applyForceToCenter(forward.mulLocal(drag));
			}
		}
		if (lateral.length() > tireTraction[index]) {
			lateral.mulLocal(tireTraction[index]/lateral.length());
			tiresSkidding[index] = true;
		} else if (!(tireCanBrake[index] && handbrakeApplied)) {
			tiresSkidding[index] = false;
		}
		lateral.mulLocal(-tire.getMass());
		tire.applyLinearImpulse(lateral, tire.getWorldCenter(), true);
	}

	public void render() {
		Vec2[] vertices = Arrays.copyOf(((PolygonShape)chassis.getFixtureList().getShape()).getVertices(),
				((PolygonShape)chassis.getFixtureList().getShape()).getVertexCount());
		for (int i = 0; i < 4; i++)
			vertices[i] = Transform.mul(chassis.getTransform(), vertices[i]);
		
		Main.game.getMap().getCarDrawer().drawTriangle(
				vertices[0].x, vertices[0].y, 0, 1,
				vertices[1].x, vertices[1].y, 1, 1,
				vertices[2].x, vertices[2].y, 1, 0,
				0, getLayer());
		Main.game.getMap().getCarDrawer().drawTriangle(
				vertices[0].x, vertices[0].y, 0, 1,
				vertices[2].x, vertices[2].y, 1, 0,
				vertices[3].x, vertices[3].y, 0, 0,
				0, getLayer());
	}
	
	public void turn(float dir) {
		turning = dir * turnRadius;
	}
	
	public void drive(float power) {
		enginePower = power*force;
	}
	
	public void handbrake(boolean applied) {
		handbrakeApplied = applied;
	}
	
	public Body getChassis() {
		return chassis;
	}
	
	@Override
	public Vec2 getCenter() {
		return chassis.getPosition();
	}
	
	public static final class CarDef {
		public float width, height, acceleration, topspeed, turnRadius, mass=50, x, y, angle;
		public TireDef[] tires;
	}
	
	public static final class TireDef {
		public float localx, localy, width=0.25f, height=0.75f, traction=2.5f, mass=45;
		public float drive;
		public boolean stoppedByHandbrake=false, canTurn=false;
	}
}

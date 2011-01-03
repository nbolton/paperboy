package nbolton.paperboy2;

import java.text.DecimalFormat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

public class Boy {
	
	private World world;
	
	public RevoluteJoint leftArmJoint, rightArmJoint, 
		leftLegTopJoint, rightLegTopJoint,
		leftLegBottomJoint, rightLegBottomJoint;
	
	public Body torso, torsoSupport1, torsoSupport2;
	
	public float legAngle = (float)Math.toRadians(100);
	public float armAngle = (float)Math.toRadians(60);
	public float headAngle = (float)Math.toRadians(30);
	
	public Action lastAction = Action.None;
	public Action action = Action.WalkRight; //Action.None;
	public Vector2 position;
	
	enum Action
	{
		None,
		WalkRight,
		WalkLeft,
		Jump,
		Grab
	};
	
	public Boy(World world) {
		this.world = world;
	}

	public void createStickManSideOn(float x, float y) {

		torso = createRectangleBodyPart(x, y, 0.1f, 0.8f);		
		Body head = createRoundBodyPart(x, y + 1.2f, 0.4f);
		
		Body leftArm = createRectangleBodyPart(x, y, 0.1f, 0.65f);
		Body rightArm = createRectangleBodyPart(x, y, 0.1f, 0.65f);
		
		Body leftLegTop = createRectangleBodyPart(x, y - 1.1f, 0.1f, 0.5f);
		Body rightLegTop = createRectangleBodyPart(x, y - 1.1f, 0.1f, 0.5f);
		Body leftLegBottom = createRectangleBodyPart(x, y - 1.9f, 0.1f, 0.5f);
		Body rightLegBottom = createRectangleBodyPart(x, y - 1.9f, 0.1f, 0.5f);

		torsoSupport1 = createSupportBody(position.x - 2, position.y);
		torsoSupport2 = createSupportBody(position.x + 2, position.y);
		
		joinBodyParts(torso, head, new Vector2(0, 0.8f), headAngle);
		
		leftArmJoint = joinBodyParts(torso, leftArm, new Vector2(0, 0.6f), -armAngle * 0.7f, armAngle);
		rightArmJoint = joinBodyParts(torso, rightArm, new Vector2(0, 0.6f), -armAngle * 0.7f, armAngle);
		
		leftLegTopJoint = joinBodyParts(torso, leftLegTop, new Vector2(0, -0.7f), 0.1f, legAngle);
		rightLegTopJoint = joinBodyParts(torso, rightLegTop, new Vector2(0, -0.7f), 0.1f, legAngle);
		leftLegBottomJoint = joinBodyParts(leftLegTop, leftLegBottom, new Vector2(0, -0.4f), -legAngle * 1.5f, 0);
		rightLegBottomJoint = joinBodyParts(rightLegTop, rightLegBottom, new Vector2(0, -0.4f), -legAngle * 1.5f, 0);

		joinSupportBody(torso, torsoSupport1, new Vector2(0, 0.7f));
		joinSupportBody(torso, torsoSupport2, new Vector2(0, 0.7f));
		joinSupportBody(torso, torsoSupport1, new Vector2(0, -0.7f));
		joinSupportBody(torso, torsoSupport2, new Vector2(0, -0.7f));
		
		//supportY = y + 5;
		//supportLeft = createSupportBody(new Vector2(x - 6, supportY));
		//supportRight = createSupportBody(new Vector2(x + 6, supportY));

		//joinSupportBody(head, supportLeft, new Vector2(0, 0));
		//joinSupportBody(torso, supportLeft, new Vector2(0, -1f));
		//joinSupportBody(head, supportRight, new Vector2(0, 0));
		//joinSupportBody(torso, supportRight, new Vector2(0, -1f));
	}
	
	private Body createSupportBody(float x, float y)
	{
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(0.1f, 0.1f);
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.x = x;
		bodyDef.position.y = y;
		
		Body body = world.createBody(bodyDef);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		
		// don't collide with anything
		fixtureDef.filter.groupIndex = -1;
		fixtureDef.filter.categoryBits = Game.FILTER_SUPPORT;
		fixtureDef.filter.maskBits = Game.FILTER_WALL;
		
		body.createFixture(fixtureDef);
		shape.dispose();
		
		return body;
	}

	private DistanceJoint joinSupportBody(Body body, Body support, Vector2 bodyAnchor) {
		
		DistanceJointDef jointDef = new DistanceJointDef();
		
		jointDef.initialize(
			body, support, body.getWorldPoint(bodyAnchor), support.getWorldCenter());
		
		return (DistanceJoint)world.createJoint(jointDef);
	}
	
	private RevoluteJoint joinBodyParts(Body a, Body b, Vector2 anchor, float angle) {
		
		return joinBodyParts(a, b, anchor, -angle, angle);
	}
	
	private RevoluteJoint joinBodyParts(
		Body a, Body b, Vector2 anchor, float lowerAngle, float upperAngle) {

		RevoluteJointDef jointDef = new RevoluteJointDef();
		
		jointDef.initialize(a, b, a.getWorldPoint(anchor));
		
		//jointDef.enableLimit = true;
		//jointDef.lowerAngle = lowerAngle;
		//jointDef.upperAngle = upperAngle;
		
		jointDef.enableMotor = true;
		jointDef.maxMotorTorque = 1000;
		
		return (RevoluteJoint)world.createJoint(jointDef);
	}

	private Body createRoundBodyPart(float x, float y, float radius) {

		CircleShape shape = new CircleShape();
		shape.setRadius(radius);

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.x = x;
		bodyDef.position.y = y;
		bodyDef.allowSleep = false;
		
		Body body = world.createBody(bodyDef);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1f;
		
		// -1 means no body parts collide
		fixtureDef.filter.groupIndex = -1;
		fixtureDef.filter.categoryBits = Game.FILTER_BOY;
		fixtureDef.filter.maskBits = Game.FILTER_STUFF | Game.FILTER_WALL;

		// add the boxPoly shape as a fixture
		body.createFixture(fixtureDef);
		shape.dispose();
		
		return body;
	}

	private Body createRectangleBodyPart(float x, float y, float width, float height) {
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width, height);
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.y = y;
		bodyDef.position.x = x;
		bodyDef.allowSleep = false;
		
		Body body = world.createBody(bodyDef);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1;
		
		// -1 means no body parts collide
		fixtureDef.filter.groupIndex = -1;
		fixtureDef.filter.categoryBits = Game.FILTER_BOY;
		fixtureDef.filter.maskBits = Game.FILTER_STUFF | Game.FILTER_WALL;
		
		body.createFixture(fixtureDef);
		shape.dispose();
		
		return body;
	}
	
	float walkDelta = 0;
	
	public void render() {
		
		float delta = Gdx.graphics.getDeltaTime();
		
		// action has been toggled
		if (lastAction != action) {
			
			// boy has started walking - reset delta
			if (action == Action.WalkLeft || action == Action.WalkRight)
				walkDelta = 0;
		} else {
			walkDelta += delta;
		}
		
		if (action == Action.WalkRight) {
			
			rightLegBottomJoint.setMotorSpeed(getLegMotorSpeed());

			DecimalFormat f = new DecimalFormat("0.00");
			System.out.println(f.format(getLegMotorSpeed()));
		}
		
		lastAction = action;
	}
	
	private float getLegMotorSpeed() {
		return getOscillation() * 2;
	}
	
	private float getOscillation() {
		return (float)Math.sin(walkDelta);
	}
}

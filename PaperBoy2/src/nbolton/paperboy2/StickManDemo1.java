package nbolton.paperboy2;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

public class StickManDemo1 implements ApplicationListener, InputProcessor {
	
	OrthographicCamera camera;
	Box2DDebugRenderer renderer;
	World world;

	Body groundBody;
	Body leftWall;
	Body rightWall;

	MouseJoint mouseJoint;
	Fixture hitFixture;

	Fixture walkLeftButton;
	Fixture walkRightButton;
	Fixture jumpButton;
	Fixture grabButton;

	final short FILTER_NONE = 0x0000;
	final short FILTER_SUPPORT = 0x0001;
	final short FILTER_WALL = 0x0002;
	final short FILTER_BOY = 0x0004;
	final short FILTER_STUFF = 0x0008;
	final short FILTER_CONTROLS = 0x0016;
	
	final float CAMERA_WIDTH = 12;
	final float CAMERA_HEIGHT = 8;

	RevoluteJoint leftArmJoint, rightArmJoint, 
		leftLegTopJoint, rightLegTopJoint,
		leftLegBottomJoint, rightLegBottomJoint;
	Body torso, torsoSupport;
	
	float legAngle = (float)Math.toRadians(100);
	float armAngle = (float)Math.toRadians(60);
	float headAngle = (float)Math.toRadians(30);
	
	enum Action
	{
		WalkRight,
		WalkLeft,
		Jump,
		Grab
	};
	
	Action action;
	
	protected void createWorld() {
		
		groundBody = createGround(50, 0.1f, 0);
		createControls();
		
		//createCircles();
		//createBoxes();
		
		createStickManSideOn(0, 2.5f);
	}
	
	private void createControls() {

		walkLeftButton = createControlButton(CAMERA_WIDTH / 7, CAMERA_HEIGHT / 10);
		walkRightButton = createControlButton(CAMERA_WIDTH / 7, CAMERA_HEIGHT / 10);
		jumpButton = createControlButton(CAMERA_WIDTH / 7, CAMERA_HEIGHT / 20);
		grabButton = createControlButton(CAMERA_WIDTH / 7, CAMERA_HEIGHT / 20);
	}

	private Fixture createControlButton(float width, float height) {
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width, height);
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.StaticBody;
		
		Body body = world.createBody(bodyDef);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1;
		fixtureDef.filter.categoryBits = FILTER_CONTROLS;
		
		return body.createFixture(fixtureDef);
	}

	private void createStickManSideOn(float x, float y) {
		
		torso = createRectangleBodyPart(x, y, 0.1f, 0.8f);
		Body head = createRoundBodyPart(x, y + 1.2f, 0.4f);
		
		Body leftArm = createRectangleBodyPart(x, y, 0.1f, 0.65f);
		Body rightArm = createRectangleBodyPart(x, y, 0.1f, 0.65f);
		
		Body leftLegTop = createRectangleBodyPart(x, y - 1.1f, 0.1f, 0.5f);
		Body rightLegTop = createRectangleBodyPart(x, y - 1.1f, 0.1f, 0.5f);
		Body leftLegBottom = createRectangleBodyPart(x, y - 1.9f, 0.1f, 0.5f);
		Body rightLegBottom = createRectangleBodyPart(x, y - 1.9f, 0.1f, 0.5f);
		
		joinBodyParts(torso, head, new Vector2(0, 0.8f), headAngle);
		
		leftArmJoint = joinBodyParts(torso, leftArm, new Vector2(0, 0.6f), -armAngle * 0.7f, armAngle);
		rightArmJoint = joinBodyParts(torso, rightArm, new Vector2(0, 0.6f), -armAngle * 0.7f, armAngle);
		
		leftLegTopJoint = joinBodyParts(torso, leftLegTop, new Vector2(0, -0.7f), 0.1f, legAngle);
		rightLegTopJoint = joinBodyParts(torso, rightLegTop, new Vector2(0, -0.7f), 0.1f, legAngle);
		leftLegBottomJoint = joinBodyParts(leftLegTop, leftLegBottom, new Vector2(0, -0.4f), -legAngle * 1.5f, 0);
		rightLegBottomJoint = joinBodyParts(rightLegTop, rightLegBottom, new Vector2(0, -0.4f), -legAngle * 1.5f, 0);
		
		//torsoSupport = createSupportBody(new Vector2(x, y + 5));
		//joinSupportBody(torso, torsoSupport, new Vector2(0, 0));
		
		//supportY = y + 5;
		//supportLeft = createSupportBody(new Vector2(x - 6, supportY));
		//supportRight = createSupportBody(new Vector2(x + 6, supportY));

		//joinSupportBody(head, supportLeft, new Vector2(0, 0));
		//joinSupportBody(torso, supportLeft, new Vector2(0, -1f));
		//joinSupportBody(head, supportRight, new Vector2(0, 0));
		//joinSupportBody(torso, supportRight, new Vector2(0, -1f));
	}

	/*private WeldJoint joinSupportBody(Body body, Body support, Vector2 bodyAnchor) {
		
		WeldJointDef jointDef = new WeldJointDef();
		
		jointDef.initialize(body, support, body.getWorldPoint(bodyAnchor));
		
		return (WeldJoint)world.createJoint(jointDef);
	}*/
	
	private RevoluteJoint joinBodyParts(Body a, Body b, Vector2 anchor, float angle) {
		
		return joinBodyParts(a, b, anchor, -angle, angle);
	}
	
	private RevoluteJoint joinBodyParts(
		Body a, Body b, Vector2 anchor, float lowerAngle, float upperAngle) {

		RevoluteJointDef jointDef = new RevoluteJointDef();
		
		jointDef.initialize(a, b, a.getWorldPoint(anchor));
		
		jointDef.enableLimit = true;
		jointDef.lowerAngle = lowerAngle;
		jointDef.upperAngle = upperAngle;
		
		return (RevoluteJoint)world.createJoint(jointDef);
	}

	private Body createRoundBodyPart(float x, float y, float radius) {

		CircleShape shape = new CircleShape();
		shape.setRadius(radius);

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.x = x;
		bodyDef.position.y = y;
		Body body = world.createBody(bodyDef);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1f;
		
		// -1 means no body parts collide
		fixtureDef.filter.groupIndex = -1;
		fixtureDef.filter.categoryBits = FILTER_BOY;
		fixtureDef.filter.maskBits = FILTER_STUFF | FILTER_WALL;

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
		
		Body body = world.createBody(bodyDef);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1;
		
		// -1 means no body parts collide
		fixtureDef.filter.groupIndex = -1;
		fixtureDef.filter.categoryBits = FILTER_BOY;
		fixtureDef.filter.maskBits = FILTER_STUFF | FILTER_WALL;
		
		body.createFixture(fixtureDef);
		shape.dispose();
		
		return body;
	}
	/*
	private Body createSupportBody(Vector2 position)
	{
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(1, 1);
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.x = position.x;
		bodyDef.position.y = position.y;
		
		Body body = world.createBody(bodyDef);
		
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 10;
		
		// don't collide with anything
		fixtureDef.filter.categoryBits = FILTER_SUPPORT;
		fixtureDef.filter.maskBits = FILTER_WALL;
		
		body.createFixture(fixtureDef);
		shape.dispose();
		
		return body;
	}
	*/
	private void createCircles() {
		// next we add a few more circles
		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(0.25f);

		for (int i = 0; i < 10; i++) {
			BodyDef circleBodyDef = new BodyDef();
			circleBodyDef.type = BodyType.DynamicBody;
			circleBodyDef.position.x = -5 + (float)(Math.random() * 5);
			circleBodyDef.position.y = 5 + ((float)Math.random() * 5);
			Body circleBody = world.createBody(circleBodyDef);

			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = circleShape;
			fixtureDef.density = 1;
			
			fixtureDef.filter.categoryBits = FILTER_STUFF;
			fixtureDef.filter.maskBits = FILTER_STUFF | FILTER_BOY | FILTER_WALL;
			
			// add the boxPoly shape as a fixture
			circleBody.createFixture(fixtureDef);
		}
		circleShape.dispose();
	}

	private void createBoxes() {
		// next we create 50 boxes at random locations above the ground
		// body. First we create a nice polygon representing a box 2 meters
		// wide and high.
		PolygonShape boxPoly = new PolygonShape();
		boxPoly.setAsBox(0.25f, 0.25f);

		// next we create the 50 box bodies using the PolygonShape we just
		// defined. This process is similar to the one we used for the ground
		// body. Note that we reuse the polygon for each body fixture.
		for (int i = 0; i < 10; i++) {
			// Create the BodyDef, set a random position above the
			// ground and create a new body
			BodyDef boxBodyDef = new BodyDef();
			boxBodyDef.type = BodyType.DynamicBody;
			boxBodyDef.position.x = -5 + (float)(Math.random() * 5);
			boxBodyDef.position.y = 5 + (float)(Math.random() * 5);
			Body boxBody = world.createBody(boxBodyDef);

			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = boxPoly;
			fixtureDef.density = 1;

			fixtureDef.filter.categoryBits = FILTER_STUFF;
			fixtureDef.filter.maskBits = FILTER_STUFF | FILTER_BOY | FILTER_WALL;
			
			// add the boxPoly shape as a fixture
			boxBody.createFixture(fixtureDef);
		}

		// we are done, all that's left is disposing the boxPoly
		boxPoly.dispose();
	}

	private Body createGround(float width, float height, float xOffset) {
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width, height);

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.x = xOffset;
		
		Body body = world.createBody(bodyDef);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1f;
		
		fixtureDef.filter.categoryBits = FILTER_WALL;
		fixtureDef.filter.maskBits = FILTER_BOY | FILTER_STUFF | FILTER_SUPPORT;
		
		body.createFixture(fixtureDef);
		shape.dispose();
		
		return body;
	}
	/*
	final float motorSpeed = 100f;
	float leftMotorVelocity;
	float rightMotorVelocity;
	int leftMotorDirection = 1;
	int rightMotorDirection = -1;
	*/
	/** temp vector **/
	protected Vector2 tmp = new Vector2();

	@Override public void render () {
		
		float timeStep = 1.0f / 60.f;
		int velocityIterations = 10;
		int positionIterations = 8;
		
		// update the world with a fixed time step
		world.step(timeStep, velocityIterations, positionIterations);

		float cameraX = torso.getPosition().x;
		float cameraY = torso.getPosition().y + 1;
		
		// follow the boy!
		camera.getPosition().set(cameraX, cameraY, 0);

		setControlPositions(cameraX, cameraY);

		// clear the screen and setup the projection matrix
		GL10 gl = Gdx.app.getGraphics().getGL10();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		camera.setMatrices();
		
		/*float leftAngle = leftLegTopJoint.getJointAngle();
		float legAngleShort = legAngle * 0.8f;

		if (leftAngle > legAngleShort) {
			
			leftMotorDirection = -1;
		}
		
		if (leftAngle < -legAngleShort) {
			
			leftMotorDirection = 1;
		}
		
		System.out.println("a: " + leftAngle + " / m: " + leftMotorDirection);
		
		rightLegBottomJoint.setMotorSpeed(8);
		leftLegBottomJoint.setMotorSpeed(-8);

		leftMotorVelocity = delta * leftMotorDirection * motorSpeed;
		rightMotorVelocity = -leftMotorVelocity;
		
		leftLegTopJoint.setMotorSpeed(leftMotorVelocity * 0.9f);
		rightLegTopJoint.setMotorSpeed(rightMotorVelocity * 0.9f);
		
		leftArmJoint.setMotorSpeed(-leftMotorVelocity * 0.5f);
		rightArmJoint.setMotorSpeed(-rightMotorVelocity * 0.5f);
		
		// ensure the support block stays on the same y plane, but follows the head.
		//float supportY = torso.getPosition().y;
		float supportY = 7;
		supportLeft.setTransform(new Vector2(torso.getPosition().x - 10, supportY), 0);
		supportRight.setTransform(new Vector2(torso.getPosition().x + 10, supportY), 0);*/
		
		//torso.setTransform(torso.getPosition(), 0);
		//torsoSupport.setTransform(new Vector2(torso.getPosition().x, groundBody.getPosition().y + 6), 0);

		// render the world using the debug renderer
		renderer.render(world);
	}

	private void setControlPositions(float cameraX, float cameraY) {
		
		walkLeftButton.getBody().setTransform(
			new Vector2(cameraX - (CAMERA_WIDTH * 0.33f), cameraY - (CAMERA_HEIGHT * 0.38f)), 0);
		
		walkRightButton.getBody().setTransform(
			new Vector2(cameraX + (CAMERA_WIDTH * 0.33f), cameraY - (CAMERA_HEIGHT * 0.38f)), 0);
		
		jumpButton.getBody().setTransform(
			new Vector2(cameraX, cameraY - (CAMERA_HEIGHT * 0.3f)), 0);
		
		grabButton.getBody().setTransform(
			new Vector2(cameraX, cameraY - (CAMERA_HEIGHT * 0.43f)), 0);
	}

	/*Body aabbHit = null;
	private Body getBodyBelow(Body body) {
		
		Body found = null;
		
		Iterator<Body> bodies = world.getBodies();
		while (bodies.hasNext()) {
			
			QueryCallback aabb = new QueryCallback() {
				
				@Override
				public boolean reportFixture(Fixture arg0) {
					
					aabbHit = arg0.getBody();
					return true;
				}
			};
			
			Body test = bodies.next();
			Vector2 testPos = test.getPosition();
			world.QueryAABB(aabb, testPos.x, testPos.y, testPos.x, testPos.y);
			
			if ((testPos.y < body.getPosition().y) && (aabbHit != null)) {
				
				float distanceLast = 0;
				if (found != null)
					distanceLast = Math.abs(found.getPosition().x - body.getPosition().x);
				else
					found = test;
				
				float distanceThis = Math.abs(testPos.x - body.getPosition().x);
				
				if (distanceThis < distanceLast)
					found = test;
			}
		}
		
		return found;
	}*/

	@Override public void create () {
		
		camera = new OrthographicCamera();
		camera.setViewport(CAMERA_WIDTH, CAMERA_HEIGHT);

		// create the debug renderer
		renderer = new Box2DDebugRenderer();

		// create the world
		world = new World(new Vector2(0, -9.8f), true);

		// we also need an invisible zero size ground body
		// to which we can connect the mouse joint
		BodyDef bodyDef = new BodyDef();
		groundBody = world.createBody(bodyDef);

		// call abstract method to populate the world
		createWorld();
		
		Gdx.input.setInputProcessor(this);
	}

	@Override public void dispose () {		
		renderer.dispose();
		world.dispose();
	}

	@Override public boolean keyDown (int keycode) {
		return false;
	}

	@Override public boolean keyTyped (char character) {
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		return false;
	}

	/** we instantiate this vector and the callback here so we don't irritate the GC **/
	Vector2 testPoint = new Vector2();
	QueryCallback callback = new QueryCallback() {
		@Override public boolean reportFixture (Fixture fixture) {
			// if the hit point is inside the fixture of the body
			// we report it
			if (fixture.testPoint(testPoint)) {
				hitFixture = fixture;
				return false;
			} else
				return true;
		}
	};

	@Override public boolean touchDown (int x, int y, int pointer) {

		hitFixture = null;
		
		camera.getScreenToWorld(x, y, testPoint);
		world.QueryAABB(callback, testPoint.x - 0.0001f, testPoint.y - 0.0001f, testPoint.x + 0.0001f, testPoint.y + 0.0001f);
		
		if (hitFixture != null) {
			
			//hitFixture.
		}
		
		/*// translate the mouse coordinates to world coordinates
		camera.getScreenToWorld(x, y, testPoint);
		// ask the world which bodies are within the given
		// bounding box around the mouse pointer
		hitBody = null;
		world.QueryAABB(callback, testPoint.x - 0.0001f, testPoint.y - 0.0001f, testPoint.x + 0.0001f, testPoint.y + 0.0001f);

		if (hitBody == groundBody) hitBody = null;

		// ignore kinematic bodies, they don't work with the mouse joint
		if (hitBody != null && hitBody.getType() == BodyType.KinematicBody) return false;

		// if we hit something we create a new mouse joint
		// and attach it to the hit body.
		if (hitBody != null) {
			MouseJointDef def = new MouseJointDef();
			def.bodyA = groundBody;
			def.bodyB = hitBody;
			def.collideConnected = true;
			def.target.set(testPoint);
			def.maxForce = 1000.0f * hitBody.getMass();

			mouseJoint = (MouseJoint)world.createJoint(def);
			hitBody.setAwake(true);
		}

		return false;*/
		return false;
	}

	///** another temporary vector **/
	//Vector2 target = new Vector2();

	@Override public boolean touchDragged (int x, int y, int pointer) {
		/*// if a mouse joint exists we simply update
		// the target of the joint based on the new
		// mouse coordinates
		if (mouseJoint != null) {
			camera.getScreenToWorld(x, y, target);
			mouseJoint.setTarget(target);
		}
		return false;*/
		return false;
	}

	@Override public boolean touchUp (int x, int y, int pointer) {
		/*// if a mouse joint exists we simply destroy it
		if (mouseJoint != null) {
			world.destroyJoint(mouseJoint);
			mouseJoint = null;
		}
		return false;*/
		return false;
	}
	
	public void pause() {
		
	}
	
	public void resume() {
		
	}
	
	public void resize(int width, int height) {
		
	}
}

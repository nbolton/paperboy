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
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class HelloWorld implements ApplicationListener, InputProcessor {

	World world;
	Body body;
	Box2DDebugRenderer renderer;
	OrthographicCamera camera;
	
	final float CAMERA_WIDTH = 28;
	final float CAMERA_HEIGHT = 16;
	
	@Override
	public void create() {

		camera = new OrthographicCamera();
		camera.setViewport(CAMERA_WIDTH, CAMERA_HEIGHT);
		
		renderer = new Box2DDebugRenderer();
		
		// Define the gravity vector.
		Vector2 gravity = new Vector2(0.0f, -10.0f);

		// Do we want to let bodies sleep?
		boolean doSleep = true;

		// Construct a world object, which will hold and simulate the rigid bodies.
		world = new World(gravity, doSleep);

		// Define the ground body.
		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.position.set(0.0f, -5.0f);

		// Call the body factory which allocates memory for the ground body
		// from a pool and creates the ground box shape (also from a pool).
		// The body is also added to the world.
		Body groundBody = world.createBody(groundBodyDef);

		// Define the ground box shape.
		PolygonShape groundBox = new PolygonShape();

		// The extents are the half-widths of the box.
		groundBox.setAsBox(50.0f, 1.0f);

		// Add the ground fixture to the ground body.
		groundBody.createFixture(groundBox, 0.0f);

		// Define the dynamic body. We set its position and call the body factory.
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(0.0f, 4.0f);
		body = world.createBody(bodyDef);

		// Define another box shape for our dynamic body.
		PolygonShape dynamicBox = new PolygonShape();
		dynamicBox.setAsBox(1.0f, 1.0f);

		// Define the dynamic body fixture.
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = dynamicBox;

		// Set the box density to be non-zero, so it will be dynamic.
		fixtureDef.density = 1.0f;

		// Override the default friction.
		fixtureDef.friction = 0.3f;

		// Add the shape to the body.
		body.createFixture(fixtureDef);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render() {

		// Prepare for simulation. Typically we use a time step of 1/60 of a
		// second (60Hz) and 10 iterations. This provides a high quality simulation
		// in most game scenarios.
		float timeStep = 1.0f / 60.0f;
		int velocityIterations = 6;
		int positionIterations = 2;
		
		// Instruct the world to perform a single step of simulation.
		// It is generally best to keep the time step and iterations fixed.
		world.step(timeStep, velocityIterations, positionIterations);
		
		// clear the screen and setup the projection matrix
		GL10 gl = Gdx.app.getGraphics().getGL10();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		camera.setMatrices();
		
		// Clear applied body forces. We didn't apply any forces, but you
		// should know about this function.
		world.clearForces();
		
		renderer.render(world);
	}

	@Override
	public void resize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean keyDown(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return false;
	}

}

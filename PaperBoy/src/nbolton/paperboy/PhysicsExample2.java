package nbolton.paperboy;

import static org.anddev.andengine.extension.physics.box2d.util.constants.PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;

import android.hardware.SensorManager;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class PhysicsExample2 extends BaseGameActivity implements IAccelerometerListener, IOnSceneTouchListener {

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;
	
	private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	
	Texture texture;
	TiledTextureRegion boxFaceTextureRegion;
	TiledTextureRegion circleFaceTextureRegion;
	TiledTextureRegion triangleFaceTextureRegion;
	TiledTextureRegion hexagonFaceTextureRegion;
	PhysicsWorld physicsWorld;
	int faceCount;
	
	@Override
	public void onLoadComplete() {
	}

	@Override
	public Engine onLoadEngine() {
		
		Toast.makeText(this, "Touch the screen to add objects.", Toast.LENGTH_LONG).show();
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		
		return new Engine(
			new EngineOptions(true, ScreenOrientation.LANDSCAPE, 
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera));
	}
	
	@Override
	public void onLoadResources() {
		
		texture = new Texture(64, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		TextureRegionFactory.setAssetBasePath("gfx/");
		
		boxFaceTextureRegion = TextureRegionFactory.createTiledFromAsset(texture, this, "face_box_tiled.png", 0, 0, 2, 1); // 64x32
		circleFaceTextureRegion = TextureRegionFactory.createTiledFromAsset(texture, this, "face_circle_tiled.png", 0, 32, 2, 1); // 64x32
		triangleFaceTextureRegion = TextureRegionFactory.createTiledFromAsset(texture, this, "face_triangle_tiled.png", 0, 64, 2, 1); // 64x32
		hexagonFaceTextureRegion = TextureRegionFactory.createTiledFromAsset(texture, this, "face_hexagon_tiled.png", 0, 96, 2, 1); // 64x32
		mEngine.getTextureManager().loadTexture(texture);
		
		enableAccelerometerSensor(this);
	}

	@Override
	public Scene onLoadScene() {
		
		mEngine.registerUpdateHandler(new FPSLogger());
		
		final Scene scene = new Scene(2);
		scene.setBackground(new ColorBackground(0, 0, 0));
		scene.setOnSceneTouchListener(this);
		
		physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
		
		final Shape ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2);
		final Shape roof = new Rectangle(0, 0, CAMERA_WIDTH, 2);
		final Shape left = new Rectangle(0, 0, 2, CAMERA_HEIGHT);
		final Shape right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT);
		
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(physicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physicsWorld, right, BodyType.StaticBody, wallFixtureDef);
		
		scene.getBottomLayer().addEntity(ground);
		scene.getBottomLayer().addEntity(roof);
		scene.getBottomLayer().addEntity(left);
		scene.getBottomLayer().addEntity(right);
		
		scene.registerUpdateHandler(physicsWorld);
		
		return scene;
	}

	@Override
	public void onAccelerometerChanged(AccelerometerData pAccelerometerData) {
		
		physicsWorld.setGravity(new Vector2(pAccelerometerData.getY(), pAccelerometerData.getX()));
	}
	
	private void addFace(final float x, final float y) {
		
		final Scene scene = mEngine.getScene();
		
		faceCount++;
		Debug.d("Faces: " + faceCount);
		
		final AnimatedSprite face;
		final Body body;
		
		if ((faceCount % 4) == 0) {
			face = new AnimatedSprite(x, y, boxFaceTextureRegion);
			body = PhysicsFactory.createBoxBody(physicsWorld, face, BodyType.DynamicBody, FIXTURE_DEF);
			
		} else if ((faceCount % 4) == 1) {
			face = new AnimatedSprite(x, y, circleFaceTextureRegion);
			body = PhysicsFactory.createCircleBody(physicsWorld, face, BodyType.DynamicBody, FIXTURE_DEF);
			
		} else if ((faceCount % 4) == 2) {
			face = new AnimatedSprite(x, y, triangleFaceTextureRegion);
			body = PhysicsExample2.createTriangleBody(physicsWorld, face, BodyType.DynamicBody, FIXTURE_DEF);
			
		} else {
			face = new AnimatedSprite(x, y, hexagonFaceTextureRegion);
			body = PhysicsExample2.createHexagonBody(physicsWorld, face, BodyType.DynamicBody, FIXTURE_DEF);
		}
		
		face.animate(200);
		face.setUpdatePhysics(false);
		
		scene.getTopLayer().addEntity(face);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true, false, false));
	}
	
	/**
	 * Creates a {@link Body} based on a {@link PolygonShape} in the form of a triangle.:
	 * <pre>
	 *  /\
	 * /__\
	 * </pre>
	 */
	private static Body createTriangleBody(
		final PhysicsWorld physicsWorld, final Shape shape, 
		final BodyType bodyType, final FixtureDef fixtureDef) {
		
		// remember that the vertices are relative to the centre-coordinates of the shape
		final float halfWidth = shape.getWidthScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;
		final float halfHeight = shape.getHeightScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;
		
		final float top = -halfHeight;
		final float bottom = halfHeight;
		final float left = -halfHeight;
		final float right = halfWidth;
		final float centerX = 0;
		
		final Vector2[] vertices = {
			new Vector2(centerX, top),
			new Vector2(right, bottom),
			new Vector2(left, bottom)
		};
		
		return PhysicsFactory.createPolygonBody(physicsWorld, shape, vertices, bodyType, fixtureDef);
	}
	
	/**
	 * Creates a {@link Body} based on a {@link PolygonShape} in the form of a hexagon:
	 * <pre>
	 *  /\
	 * /  \
	 * |  |
	 * |  |
	 * \  /
	 *  \/
	 * </pre>
	 */
	private static Body createHexagonBody(
		final PhysicsWorld physicsWorld, final Shape shape,
		final BodyType bodyType, final FixtureDef fixtureDef) {
		
		// remember the vertices are relative to the center-coordinates of the shape
		final float halfWidth = shape.getWidthScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;
		final float halfHeight = shape.getHeightScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;
		
		// the top and bottom vertex of the hexagon are on the bottom and top of the hexagon sprite
		final float top = -halfHeight;
		final float bottom = halfHeight;
		
		final float centerX = 0;
		
		// the left and right vertices of the hexagon are not on the edge of the hexagon-sprite,
		// so we need to inset them a little.
		final float left = -halfWidth + 2.5f / PIXEL_TO_METER_RATIO_DEFAULT;
		final float right = halfWidth - 2.5f / PIXEL_TO_METER_RATIO_DEFAULT;
		final float higher = top + 8.25f / PIXEL_TO_METER_RATIO_DEFAULT;
		final float lower = bottom - 8.25f / PIXEL_TO_METER_RATIO_DEFAULT;
		
		final Vector2[] vertices = {
			new Vector2(centerX, top),
			new Vector2(right, higher),
			new Vector2(right, lower),
			new Vector2(centerX, bottom),
			new Vector2(left, lower),
			new Vector2(left, higher)
		};
		
		return PhysicsFactory.createPolygonBody(physicsWorld, shape, vertices, bodyType, fixtureDef);
	}

	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		
		if ((physicsWorld != null) && 
			(pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN)) {
			
			runOnUpdateThread(new Runnable() {
				
				@Override
				public void run() {
					
					PhysicsExample2.this.addFace(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
				}
			});
			
			return true;
		}
		
		return false;
	}

}

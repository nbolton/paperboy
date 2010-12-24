package nbolton.paperboy;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.ui.activity.BaseGameActivity;

public class SimpleExample extends BaseGameActivity {

	private Camera camera;
	private Texture texture;
	private TextureRegion faceTextureRegion;

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;
	
	@Override
	public Engine onLoadEngine() {
		
		camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		
		RatioResolutionPolicy resolutionPolicy = new RatioResolutionPolicy(
				CAMERA_WIDTH, CAMERA_HEIGHT);
		
		EngineOptions options = new EngineOptions(
			true, ScreenOrientation.LANDSCAPE, resolutionPolicy, camera);
		
		return new Engine(options);
	}

	@Override
	public void onLoadResources() {
		
		texture = new Texture(32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		faceTextureRegion = TextureRegionFactory.createFromAsset(texture, this, "gfx/face_box.png", 0, 0);
		
		mEngine.getTextureManager().loadTexture(texture);
	}

	@Override
	public Scene onLoadScene() {
		
		Scene scene = new Scene(1);
		scene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
		
		// calculate the coordinates of the face so it's centered
		final int centerX = ((CAMERA_WIDTH - this.faceTextureRegion.getWidth()) / 2);
		final int centerY = ((CAMERA_HEIGHT - this.faceTextureRegion.getHeight()) / 2);
		
		// create the face and add it to the scene
		final Sprite face = new Sprite(centerX, centerY, faceTextureRegion);
		scene.getTopLayer().addEntity(face);
		
		return scene;
	}

	@Override
	public void onLoadComplete() {
	}
}

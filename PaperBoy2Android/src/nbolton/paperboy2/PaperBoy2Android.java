package nbolton.paperboy2;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class PaperBoy2Android extends AndroidApplication {
	/** Called when the activity is first created. */
	@Override public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(new Game(), false, 16);
	}
}

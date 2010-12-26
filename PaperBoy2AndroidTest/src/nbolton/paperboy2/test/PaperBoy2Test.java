package nbolton.paperboy2.test;

import nbolton.paperboy2.PaperBoy2Android;

import android.test.ActivityInstrumentationTestCase2;
//import android.widget.TextView;

public class PaperBoy2Test extends 
	ActivityInstrumentationTestCase2<PaperBoy2Android> {
	
	//private HelloAndroid activity;

	public PaperBoy2Test() {
		super("nbolton.paperboy2", PaperBoy2Android.class);
	}
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
        //activity = this.getActivity();
	}
	
	public void testFake() {
		
	}

	/*public void testTextView() {
		TextView textView = activity.getTextView();
		assertEquals("Hello, Android", textView.getText());
	}*/
}


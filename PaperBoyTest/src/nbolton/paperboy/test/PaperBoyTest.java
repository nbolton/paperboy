package nbolton.paperboy.test;

import nbolton.paperboy.PaperBoy;

import android.test.ActivityInstrumentationTestCase2;
//import android.widget.TextView;

public class PaperBoyTest extends 
	ActivityInstrumentationTestCase2<PaperBoy> {
	
	//private HelloAndroid activity;

	public PaperBoyTest() {
		super("nbolton.paperboy", PaperBoy.class);
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


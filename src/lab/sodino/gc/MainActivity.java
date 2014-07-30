package lab.sodino.gc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {
	public static final int NUMBER = 50000;
	
	private Button btnFinalize;
	private Button btnWeakReferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btnFinalize = (Button)findViewById(R.id.btnFinalize);
		btnFinalize.setOnClickListener(this);
		btnWeakReferences = (Button)findViewById(R.id.btnWeakReferences);
		btnWeakReferences.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch(v.getId()){
		case R.id.btnFinalize:
			intent.setClass(MainActivity.this, FinalizeActivity.class);
			break;
		case R.id.btnWeakReferences:
			intent.setClass(MainActivity.this, WeakReferencesActivity.class);
			break;
		}
		startActivity(intent);
	}
}

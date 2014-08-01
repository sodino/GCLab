package lab.sodino.gc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {
	public static final int MAX = 50000;
	public static final int MARKUP = 10000;
	private int number = MARKUP;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button btnNumber = (Button)findViewById(R.id.btnNumber);
		btnNumber.setOnClickListener(this);
		Button btnFinalize = (Button)findViewById(R.id.btnFinalize);
		btnFinalize.setOnClickListener(this);
		Button btnWeakReferences = (Button)findViewById(R.id.btnWeakReferences);
		btnWeakReferences.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch(v.getId()){
		case R.id.btnNumber:
			number += MARKUP;
			if (number > MAX) {
				number = MARKUP;
			}
			((Button)v).setText("Choice Number: " + number);
			break;
		case R.id.btnFinalize:
			intent = new Intent();
			intent.setClass(MainActivity.this, FinalizeActivity.class);
			intent.putExtra("number", number);
			startActivity(intent);
			break;
		case R.id.btnWeakReferences:
			intent = new Intent();
			intent.setClass(MainActivity.this, WeakReferencesActivity.class);
			intent.putExtra("number", number);
			startActivity(intent);
			break;
		}
	}
}

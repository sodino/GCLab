package lab.sodino.gc;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * finalize:
 * obj num  consume    increase  <br/>
 * 10000:   433ms                <br/>
 * 20000:   1244ms     811ms     <br/>
 * 30000:   2775ms     1531ms    <br/>
 * 40000:   4940ms     2165ms    <br/>
 * 50000:   7553ms     2613ms    <br/>
 * */
public class FinalizeActivity extends Activity implements OnClickListener {
	private Button btnNew;
	private Button btnRelease;
	private TextView txtResult;
	private long startGCTime = 0l;
	private ArrayList<FinalizeObject> listBusiness = new ArrayList<FinalizeObject>();
	private ArrayList<String> listGCLog = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_finalize);
		btnNew = (Button)findViewById(R.id.btnNew);
		btnNew.setOnClickListener(this);
		btnNew.setEnabled(true);
		btnRelease = (Button)findViewById(R.id.btnRelease);
		btnRelease.setOnClickListener(this);
		btnRelease.setEnabled(false);
		
		txtResult = (TextView)findViewById(R.id.txtResult);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	class FinalizeObject {
		int id = -1;
		String idStr = null;
		public FinalizeObject(int id) {
			this.id = id;
			this.idStr = Integer.toString(id);
		}
		
		@Override
		public void finalize() {
			boolean contains = listGCLog.contains(FinalizeObject.this.idStr); 
			if (contains) {
				listGCLog.remove(idStr);
			}
			if (listGCLog.size() == 0) {
				final long consume = (System.currentTimeMillis() - startGCTime);
				Log.d("ANDROID_LAB", "finalize size=0, consumeTime=" + consume +" name=" + Thread.currentThread().getName());
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						txtResult.setText("GC "+ MainActivity.NUMBER+" objs,\nconsume:" + consume +" ms");
						btnNew.setEnabled(true);						
					}
				});
			}
		}
	}


	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnNew:
			newObject();
			break;
		case R.id.btnRelease:
			releaseObject();
			break;
		}
	}
	
	private void newObject(){
		for (int i = 0;i < MainActivity.NUMBER;i ++) {
			FinalizeObject obj = new FinalizeObject(i);
			listBusiness.add(obj);
			listGCLog.add(obj.idStr);
		}
		Log.d("ANDROID_LAB", "newObject " + MainActivity.NUMBER);
		
		btnNew.setEnabled(false);
		btnRelease.setEnabled(true);
		txtResult.setText("");
	}
	
	private void releaseObject() {
		btnRelease.setEnabled(false);
		listBusiness.clear();
		startGCTime = System.currentTimeMillis();
		System.gc();
	}
}

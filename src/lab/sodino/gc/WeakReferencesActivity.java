package lab.sodino.gc;

import java.lang.ref.WeakReference;
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
 * 10000:   95ms                 <br/>
 * 20000:   124ms       29ms     <br/>
 * 30000:   145ms       39ms     <br/>
 * 40000:   182ms       37ms     <br/>
 * 50000:   217ms       35ms     <br/>
 * */
public class WeakReferencesActivity extends Activity implements OnClickListener {
	private Button btnNew;
	private Button btnRelease;
	private TextView txtResult;
	private long startGCTime = 0l;
	private ArrayList<WFObject> listBusiness = new ArrayList<WFObject>();
	private ArrayList<WeakReference<WFObject>> listGCLog = new ArrayList<WeakReference<WFObject>>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weak_references);
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

	
	class WFObject {
		int id = -1;
		String idStr = null;
		public WFObject(int id) {
			this.id = id;
			this.idStr = Integer.toString(id);
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
			WFObject obj = new WFObject(i);
			listBusiness.add(obj);
			WeakReference<WFObject> wf  = new WeakReference<WFObject>(obj);
			listGCLog.add(wf);
		}
		Log.d("ANDROID_LAB", "newObject " + MainActivity.NUMBER);
		
		btnNew.setEnabled(false);
		btnRelease.setEnabled(true);
		txtResult.setText("");
	}
	
	private void releaseObject() {
		btnRelease.setEnabled(false);
		listBusiness.clear();
		new Thread() {
			public void run() {
				startGCTime = System.currentTimeMillis();
				System.gc();
				int size = 0;
				while((size = listGCLog.size()) > 0) {
					for (int i = size - 1; i >= 0; i--) {
						WeakReference<WFObject> wfObj = listGCLog.get(i);
						if (wfObj.get() == null) { // 即WFObject已经被回收了
							listGCLog.remove(i);
						}
					}
				}
				long consume = System.currentTimeMillis() - startGCTime;
				Log.d("ANDROID_LAB", "releaseObject() consume=" + consume);
				showResult(consume);
			}
		}.start();
	}
	
	private void showResult(final long consume) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				txtResult.setText("GC "+ MainActivity.NUMBER+" objs,\nconsume:" + consume +" ms");
				btnNew.setEnabled(true);
			}
		});
	}
}

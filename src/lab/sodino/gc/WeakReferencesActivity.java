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
	private Button btnNew,btnRelease;
	private TextView txtResult;
	private long startGCTime = 0l;
	private ArrayList<WFObject> listBusiness = new ArrayList<WFObject>();
	private ArrayList<WeakReference<WFObject>> listGCLog = new ArrayList<WeakReference<WFObject>>();
	private int number;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weak_references);
		number = getIntent().getIntExtra("number", MainActivity.MAX);
		TextView txtNumber = (TextView) findViewById(R.id.txtNumber);
		txtNumber.setText("Finalize:Object's number=" + number);
		btnNew = (Button)findViewById(R.id.btnNew);
		btnNew.setOnClickListener(this);
		btnNew.setEnabled(true);
		btnRelease = (Button)findViewById(R.id.btnRelease);
		btnRelease.setOnClickListener(this);
		btnRelease.setEnabled(false);
		
		txtResult = (TextView)findViewById(R.id.txtResult);
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
		txtResult.setText("");
		long startNewTime = System.currentTimeMillis();
		for (int i = 0;i < number;i ++) {
			WFObject obj = new WFObject(i);
			listBusiness.add(obj);
		}
		long consume = System.currentTimeMillis() - startNewTime;
		showResult(true, consume);
		for (int i = 0;i < number;i ++) {
			WFObject obj = listBusiness.get(i);
			WeakReference<WFObject> wf  = new WeakReference<WFObject>(obj);
			listGCLog.add(wf);
		}
		Log.d("ANDROID_LAB", "newObject " + number);
	}
	
	private void releaseObject() {
		btnRelease.setEnabled(false);
		new Thread() {
			public void run() {
				startGCTime = System.currentTimeMillis();
				listBusiness.clear();
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
				showResult(false, consume);
			}
		}.start();
	}
	
	private void showResult(final boolean isNew, final long consume) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (isNew) {
					txtResult.setText("New "+ number +" objs,\nconsume:" + consume +" ms");
					btnNew.setEnabled(false);
					btnRelease.setEnabled(true);
				} else {
					String newObjStr = txtResult.getText().toString();
					txtResult.setText(newObjStr + "\n\nGC "+ number +" objs,\nconsume:" + consume +" ms");
					btnNew.setEnabled(true);
					btnRelease.setEnabled(false);
				}
			}
		});
	}
}

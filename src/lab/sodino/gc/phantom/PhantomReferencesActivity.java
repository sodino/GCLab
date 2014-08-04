package lab.sodino.gc.phantom;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;

import lab.sodino.gc.MainActivity;
import lab.sodino.gc.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * release:
 * obj num  consume    increase  <br/>
 * 10000:    <br/>
 * 20000:    <br/>
 * 30000:    <br/>
 * 40000:    <br/>
 * 50000:    <br/>
 * */
public class PhantomReferencesActivity extends Activity implements OnClickListener {
	private Button btnNew,btnRelease;
	private TextView txtResult;
	private long startGCTime = 0l;
	private ArrayList<PFObject> listBusiness = new ArrayList<PFObject>();
	private ArrayList<PhantomReference<PFObject>> listGCLog = new ArrayList<PhantomReference<PFObject>>();
	private ReferenceQueue<PFObject> refQueue = new ReferenceQueue<PFObject>();
	private int number;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phantom_references);
		number = getIntent().getIntExtra("number", MainActivity.MAX);
		TextView txtNumber = (TextView) findViewById(R.id.txtNumber);
		txtNumber.setText("PhantomReferences:Object's number=" + number);
		btnNew = (Button)findViewById(R.id.btnNew);
		btnNew.setOnClickListener(this);
		btnNew.setEnabled(true);
		btnRelease = (Button)findViewById(R.id.btnRelease);
		btnRelease.setOnClickListener(this);
		btnRelease.setEnabled(false);
		
		txtResult = (TextView)findViewById(R.id.txtResult);
	}

	
	class PFObject {
		int id = -1;
		String idStr = null;
		public PFObject(int id) {
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
			PFObject obj = new PFObject(i);
			listBusiness.add(obj);
		}
		long consume = System.currentTimeMillis() - startNewTime;
		showResult(true, consume);
		for (int i = 0;i < number;i ++) {
			PFObject obj = listBusiness.get(i);
			PhantomReference<PFObject> phantomRef  = new PhantomReference<PFObject>(obj, refQueue);
			listGCLog.add(phantomRef);
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
				int count = 0;
				while(count != number) {
					Reference<? extends PFObject> ref = (Reference<? extends PFObject>) refQueue.poll();
					if (ref != null) {
						boolean bool = listGCLog.remove(ref);
						count ++;
//						Log.d("ANDROID_LAB", "vm collected count=" + count +" remove_bool=" + bool);
					} else {
						Log.d("ANDROID_LAB", "only null, call gc! count=" + count);
						// 催促jvm尽早执行回收操作
						System.gc();
						try {
							Thread.sleep(1000l);
						} catch (InterruptedException e) {
							e.printStackTrace();
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

package lab.sodino.gc.soft;

import java.lang.ref.SoftReference;
import java.util.ArrayList;

import lab.sodino.gc.MainActivity;
import lab.sodino.gc.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * SoftReferences:只有在内存不足时，才会释放所引用的对象。<br/>
 * newHugeObject()用于不断申请图片，制作OOM的机会。<br/>
 * */
public class SoftReferencesActivity extends Activity implements OnClickListener {
	private Button btnNew,btnRelease,btnHugeObject;
	private TextView txtResult;
	private long startGCTime = 0l;
	private ArrayList<RefObject> listBusiness = new ArrayList<RefObject>();
	private ArrayList<SoftReference<RefObject>> listGCLog = new ArrayList<SoftReference<RefObject>>();
	private int number;
	private ArrayList<Bitmap> listBitmap = new ArrayList<Bitmap>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_soft_references);
		number = getIntent().getIntExtra("number", MainActivity.MAX);
		TextView txtNumber = (TextView) findViewById(R.id.txtNumber);
		txtNumber.setText("SoftReferences:Object's number=" + number);
		btnNew = (Button)findViewById(R.id.btnNew);
		btnNew.setOnClickListener(this);
		btnNew.setEnabled(true);
		btnRelease = (Button)findViewById(R.id.btnRelease);
		btnRelease.setOnClickListener(this);
		btnRelease.setEnabled(false);
		btnHugeObject = (Button)findViewById(R.id.btnHugeObject);
		btnHugeObject.setOnClickListener(this);
		btnHugeObject.setEnabled(false);
		
		txtResult = (TextView)findViewById(R.id.txtResult);
//        ReferenceQueue queue = new ReferenceQueue();
//        PhantomReference ref = new PhantomReference(new RefObject(), queue);
	}

	
	class RefObject {
		int id = -1;
		String idStr = null;
		public RefObject(int id) {
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
		case R.id.btnHugeObject:
			newHugeObject();
			break;
		}
	}
	
	private void newHugeObject() {
		btnHugeObject.setEnabled(false);
		new Thread(){
			public void run() {
				Log.d("ANDROID_LAB", "newHugObject start");
				int count = 0;
				while(true) {
					count ++;
					try{
						Bitmap bit = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
						listBitmap.add(bit);
					}catch(OutOfMemoryError err){
						err.printStackTrace();
						System.gc();
						if (listGCLog.size() == 0) {
							break;
						}
					}
				}
				Log.d("ANDROID_LAB", "newHugObject oom, count=" + count);
			}
		}.start();
	}

	private void newObject(){
		txtResult.setText("");
		long startNewTime = System.currentTimeMillis();
		for (int i = 0;i < number;i ++) {
			RefObject obj = new RefObject(i);
			listBusiness.add(obj);
		}
		long consume = System.currentTimeMillis() - startNewTime;
		showResult(true, consume);
		for (int i = 0;i < number;i ++) {
			RefObject obj = listBusiness.get(i);
			SoftReference<RefObject> wf  = new SoftReference<RefObject>(obj);
			listGCLog.add(wf);
		}
		Log.d("ANDROID_LAB", "newObject " + number);
	}
	
	private void releaseObject() {
		btnRelease.setEnabled(false);
		btnHugeObject.setEnabled(true);
		new Thread() {
			public void run() {
				startGCTime = System.currentTimeMillis();
				listBusiness.clear();
				System.gc();
				int size = 0;
				while((size = listGCLog.size()) > 0) {
					for (int i = size - 1; i >= 0; i--) {
						SoftReference<RefObject> wfObj = listGCLog.get(i);
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
					btnHugeObject.setEnabled(false);
					listBitmap.clear();
				}
			}
		});
	}
}

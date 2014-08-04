package lab.sodino.gc.finalize;

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
 * 数据证明这是一个坏例子：时间太长了。
 * 另其它风险：
 * 1. finalize()方法有可能让对象再次复活
 * 2. 实现了finalize()的对象至少要执行两次gc才有可能被回收。
 * 
 * finalize:
 * obj num  consume    increase  <br/>
 * 10000:   433ms                <br/>
 * 20000:   1244ms     811ms     <br/>
 * 30000:   2775ms     1531ms    <br/>
 * 40000:   4940ms     2165ms    <br/>
 * 50000:   7553ms     2613ms    <br/>
 * */
public class FinalizeActivity extends Activity implements OnClickListener {
	private TextView txtResult;
	private Button btnNew,btnRelease;
	private long startGCTime = 0l;
	private ArrayList<FinalizeObject> listBusiness = new ArrayList<FinalizeObject>();
	private ArrayList<String> listGCLog = new ArrayList<String>();
	private int number;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_finalize);
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
						String newObjStr = txtResult.getText().toString();
						txtResult.setText(newObjStr + "\n\nGC "+ number +" objs,\nconsume:" + consume +" ms");
						btnNew.setEnabled(true);
						btnRelease.setEnabled(false);
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
		txtResult.setText("");
		long startNewTime = System.currentTimeMillis();
		for (int i = 0;i < number;i ++) {
			FinalizeObject obj = new FinalizeObject(i);
			listBusiness.add(obj);
		}
		final long consume = System.currentTimeMillis() - startNewTime;
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				txtResult.setText("New "+ number +" objs,\nconsume:" + consume +" ms");
				btnNew.setEnabled(false);
				btnRelease.setEnabled(true);
			}
		});
		for (int i = 0;i < number;i ++) {
			FinalizeObject obj = listBusiness.get(i);
			listGCLog.add(obj.idStr);
		}
		Log.d("ANDROID_LAB", "newObject " + number +" consume=" + consume);
	}
	
	private void releaseObject() {
		btnRelease.setEnabled(false);
		startGCTime = System.currentTimeMillis();
		listBusiness.clear();
		System.gc();
	}
}

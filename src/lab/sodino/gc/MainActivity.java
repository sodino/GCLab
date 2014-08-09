package lab.sodino.gc;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lab.sodino.gc.finalize.FinalizeActivity;
import lab.sodino.gc.phantom.PhantomReferencesActivity;
import lab.sodino.gc.soft.SoftReferencesActivity;
import lab.sodino.gc.weak.WeakReferencesActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
/**
 * 
 * 本文中的代码可以加QQ群Code2Share(363267446)，从群共享文件中去下载获得。
 * 也可以在http://blog.csdn.net/sodino中阅读详细文章。
 * */
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
		Button btnSoftReferences = (Button)findViewById(R.id.btnSoftReferences);
		btnSoftReferences.setOnClickListener(this);
		Button btnPhantomReferences = (Button)findViewById(R.id.btnPhantomReferences);
		btnPhantomReferences.setOnClickListener(this);
		Button btnObjectLifeCycle = (Button)findViewById(R.id.btnObjectLifeCycle);
		btnObjectLifeCycle.setOnClickListener(this);
		Button btnDumpFile = (Button)findViewById(R.id.btnDumpFile);
		btnDumpFile.setOnClickListener(this);
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
		case R.id.btnSoftReferences:
			intent = new Intent();
			intent.setClass(MainActivity.this, SoftReferencesActivity.class);
			intent.putExtra("number", number);
			startActivity(intent);
			break;
		case R.id.btnPhantomReferences:
			intent = new Intent();
			intent.setClass(MainActivity.this, PhantomReferencesActivity.class);
			intent.putExtra("number", number);
			startActivity(intent);
			break;
		case R.id.btnObjectLifeCycle:
			intent = new Intent();
			intent.setClass(MainActivity.this, ObjectLifeCycleActivity.class);
			startActivity(intent);
			break;
		case R.id.btnDumpFile:
			createDumpFile(this);
			break;
		}
	}
	
	public static boolean createDumpFile(Context context) {
		String LOG_PATH = "/dump.gc/";
		boolean bool = false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ssss");
		String createTime = sdf.format(new Date(System.currentTimeMillis()));
		String state = android.os.Environment.getExternalStorageState();
		// 判断SdCard是否存在并且是可用的
		if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
			File file = new File(Environment.getExternalStorageDirectory().getPath() + LOG_PATH);
			if (!file.exists()) {
				file.mkdirs();
			}
			String hprofPath = file.getAbsolutePath();
			if (!hprofPath.endsWith("/")) {
				hprofPath += "/";
			}				 
			
			hprofPath += createTime + ".hprof";
			try {
				android.os.Debug.dumpHprofData(hprofPath);
				bool = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			bool = false;
		}
		
		return bool;
	}
}

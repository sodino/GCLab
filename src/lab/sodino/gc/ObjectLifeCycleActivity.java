package lab.sodino.gc;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * 本类用于验证Java对象的生命周期:
 * 先finalize->再phantom
 * 得证证据为：
 * 先输出finalize()方法中"finalize() idStr=[idStr]" 打印日志,才会有"refQueue.poll() is not null"的内容打印出来。
 * 
 * 本文中的代码可以加QQ群Code2Share(363267446)，从群共享文件中去下载获得。
 * 也可以在http://blog.csdn.net/sodino中阅读详细文章。
 * */
public class ObjectLifeCycleActivity extends Activity implements View.OnClickListener {
	Button btnTrigger;
	CycleObject objFatal;
	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(2130903040);
		this.btnTrigger = ((Button) findViewById(2131165186));
		this.btnTrigger.setOnClickListener(this);
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnTrigger:
			checkLifeCycle();
		}
	}

	private void checkLifeCycle() {
		new Thread() {
			public void run() {
				CycleObject obj = new CycleObject("1");
				ReferenceQueue<CycleObject> refQueue = new ReferenceQueue<CycleObject>();
				PhantomReference<CycleObject> phantomRef = new PhantomReference<CycleObject>(obj, refQueue);
				Log.d("ANDROID_LAB", "set obj == null. hashCode=" + obj.toString());
				// 置空后，CycleObject仍存在内存的，可用mat查询到
				obj = null;
				// gc()只是通知jvm说application需要回收内存垃圾了，但并不一定会立即回收。但多催促有尽快执行的可能。
				System.gc();
				Reference<? extends CycleObject> ref;
				while (true) {
					// phantomReference的被回收时机是未知的，只好一直循环啦..
					ref = refQueue.poll();
					if (ref == null){
						// 至此， 仍可用mat查询到CycleObject
						Log.d("ANDROID_LAB", "refQueue.poll() is null " + System.currentTimeMillis());
						// 为加快poll()，可以在ddms->Devices界面多多点击"Cause GC"按钮
						try {
							Thread.sleep(1000L);
						} catch (InterruptedException localInterruptedException) {
							localInterruptedException.printStackTrace();
						}
					} else {
						// 至此，CycleObject被jvm彻底回收了。
						Log.d("ANDROID_LAB", "refQueue.poll() is not null:" + ref + " " + System.currentTimeMillis());
						break;
					} 
				}
			}
		}.start();
	}

	class CycleObject {
		String idStr;

		public CycleObject(String id) {
			this.idStr = id;
		}

		public void finalize() {
			Log.d("ANDROID_LAB", "finalize() idStr=" + this.idStr + " toString=" + super.toString());
		}
	}
}
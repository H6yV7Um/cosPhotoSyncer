package com.hujiwei.cosphotosyncer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static android.view.View.VISIBLE;

public class MainActivity extends Activity implements View.OnClickListener {

	private QServiceCfg config;

	private ImageButton syncPhotoBtn;

	private TextView syncProgressTv;

	private ProgressBar syncProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		config = QServiceCfg.instance(this);
		syncPhotoBtn = findViewById(R.id.syncPhoto);
		syncProgressTv = findViewById(R.id.syncProgressTv);
		syncProgressBar = findViewById(R.id.syncProgressBar);
		syncPhotoBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				new ObjectSyncer(config, mainHandler).start();
			}
		}).start();
	}

	private Handler mainHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				syncPhotoBtn.setEnabled(false);
				syncProgressTv.setVisibility(VISIBLE);
				break;
			case 1:
				syncPhotoBtn.setEnabled(true);
				syncProgressTv.setText((String) msg.obj);
				break;
			case 2:
				Object[] datas = (Object[]) msg.obj;
				syncProgressTv.setText((String) datas[0]);
				syncProgressBar.setVisibility(VISIBLE);
				syncProgressBar.setMax((int) datas[1]);
				break;
			case 3:
				Object[] objs = (Object[]) msg.obj;
				syncProgressTv.setText((String) objs[0]);
				syncProgressBar.setProgress((int) objs[1]);
				break;
			}
		}

	};
	private long exitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public Handler getMainHandler() {
		return mainHandler;
	}
}

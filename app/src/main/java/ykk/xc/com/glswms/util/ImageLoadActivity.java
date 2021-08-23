package ykk.xc.com.glswms.util;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.OnClick;
import ykk.xc.com.glswms.R;
import ykk.xc.com.glswms.comm.BaseActivity;

public class ImageLoadActivity extends BaseActivity {

	@BindView(R.id.imgView)
	TouchImageView imgView;

	private ImageLoadActivity context;
	private ImageLoadingDialog imgDialog;
	private String url;

	@Override
	public int setLayoutResID() {
		return R.layout.imageshower;
	}

	/**
	 * 初始化数据
	 */
	@Override
	public void initData() {
		context = this;
		imgDialog = new ImageLoadingDialog(this);
		imgDialog.setCancelable(false);
		imgDialog.setCanceledOnTouchOutside(false);
		imgDialog.show();

		bundle();
	}

	@OnClick({R.id.imgView})
	public void onViewClicked(View view) {
		switch (view.getId()) {
			case R.id.imgView:
				context.finish();

				break;
		}
	}

	/**
	 * 得到上个页面传来的值
	 */
	private void bundle() {
		Bundle bundle = context.getIntent().getExtras();
		if(bundle != null) {
			String imageUrl = bundle.getString("imageUrl", "");

			if(imageUrl.length() > 6) { // 图片地址都大于6
				// 网络地址包含small的就替换成空字符串
				int samllExist = imageUrl.indexOf("_small"); // 是否有small文件夹

				if(samllExist > -1) {
					url = imageUrl.replace("_small", ""); // 来自于网络地址
				} else {
					url = imageUrl;
				}

				// 加载各资源对应的常量
//				url = Scheme.FILE.wrap(url);
//				url = Scheme.ASSETS.wrap(url);
//				url = Scheme.DRAWABLE.wrap(url);
//				url = Scheme.HTTP.wrap(url);
//				url = Scheme.HTTPS.wrap(url);

//				Glide.with(context)
//						.load(imageUrl)
//						.placeholder(R.drawable.image_before)
//						.error(R.drawable.image_error)
//						.into(imgView);

				imgDialog.dismiss();
//				//延迟读取图片
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						Glide.with(context)
								.load(url)
								.placeholder(R.drawable.image_wait)
								.error(R.drawable.image_null)
								.into(imgView);
						imgDialog.dismiss();
					}
				}, 200);
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			context.finish();
		}
		return false;
	}
}

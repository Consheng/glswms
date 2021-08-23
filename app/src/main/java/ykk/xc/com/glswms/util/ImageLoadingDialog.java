package ykk.xc.com.glswms.util;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import ykk.xc.com.glswms.R;

public class ImageLoadingDialog extends Dialog{

	public ImageLoadingDialog(Context context) {
		super(context, R.style.ImageloadingDialogStyle);
	}
	
	private ImageLoadingDialog(Context context,int theme){
		super(context,theme);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_imageloading);
	}
}

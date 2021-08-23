package ykk.xc.com.glswms.entrance.page5;

import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.OnClick;
import ykk.xc.com.glswms.R;
import ykk.xc.com.glswms.comm.BaseActivity;
import ykk.xc.com.glswms.comm.Consts;

public class ServiceSetActivity extends BaseActivity {


    @BindView(R.id.et_ip)
    EditText etIp;
    @BindView(R.id.et_port)
    EditText etPort;
    @BindView(R.id.btn_save)
    Button btnSave;
    @BindView(R.id.btn_close)
    Button btnClose;
    private ServiceSetActivity context = this;

    private SharedPreferences spfConfig;


    @Override
    public int setLayoutResID() {
        return R.layout.aa_main_item5_set_service;
    }

    @Override
    public void initView() {
        spfConfig = spf(getResStr(R.string.saveConfig));
        String ip = spfConfig.getString("ip", "192.168.3.214");
        String port = spfConfig.getString("port", "8080");
        setTexts(etIp, ip);
        setTexts(etPort, port);
    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.btn_close, R.id.btn_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_close: // 关闭
                hideKeyboard(getCurrentFocus());
                context.finish();

                break;
            case R.id.btn_save: // 保存配置信息
                String ip = getValues(etIp).trim();
                String port = getValues(etPort).trim();
                SharedPreferences.Editor editor = spfConfig.edit();
                editor.putString("ip", ip);
                editor.putString("port", port);
                editor.commit();

                Consts.setIp(ip);
                Consts.setPort(port);
                hideKeyboard(view);

                context.finish();

                break;
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

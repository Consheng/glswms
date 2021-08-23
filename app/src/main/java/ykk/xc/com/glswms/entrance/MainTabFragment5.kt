package ykk.xc.com.glswms.entrance


import android.app.Activity
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.comm.BaseFragment
import java.lang.ref.WeakReference


/**
 * 图表
 */
class MainTabFragment5 : BaseFragment() {

    private val context = this
    private var mContext: Activity? = null


    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: MainTabFragment5) : Handler() {
        private val mActivity: WeakReference<MainTabFragment5>

        init {
            mActivity = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val m = mActivity.get()
            if (m != null) {
                m.hideLoadDialog()
                when (msg.what) {
                    SUCC1 // 得到更新信息
                    -> {
                    }
                    UNSUCC1 // 得到更新信息失败
                    -> m.toasts("已经是最新版本了！！！")
                }
            }
        }
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.aa_main_item5, container, false)
    }

    override fun initData() {
        mContext = activity
    }

    @OnClick(R.id.relative1, R.id.relative2, R.id.relative3)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.relative1 -> {// 订单报表
//                show(OrderSearchMainActivity::class.java, null)
            }
            R.id.relative2 // 服务器设置
            -> {
            }
            R.id.relative3 // 网络测试
            -> toasts("网络通畅！！！")
        }//                show(ServiceSetActivity.class, null);
        //                run_test();
    }

    companion object {
        private val SUCC1 = 200
        private val UNSUCC1 = 500
    }

}

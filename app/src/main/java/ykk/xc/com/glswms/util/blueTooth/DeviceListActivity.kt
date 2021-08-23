package ykk.xc.com.glswms.util.blueTooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import kotlinx.android.synthetic.main.device_list.*
import ykk.xc.com.glswms.R


class DeviceListActivity : Activity() {

    // 成员域
    private var mBtAdapter: BluetoothAdapter? = null
    private var mPairedDevicesArrayAdapter: ArrayAdapter<String>? = null
    private var mNewDevicesArrayAdapter: ArrayAdapter<String>? = null

    // 选择设备响应函数
    private val mDeviceClickListener = OnItemClickListener { av, v, arg2, arg3 ->
        // 准备连接设备，关闭服务查找
        mBtAdapter!!.cancelDiscovery()

        // 得到mac地址
        val info = (v as TextView).text.toString()
        val address = info.substring(info.length - 17)

        // 设置返回数据
        val intent = Intent()
        intent.putExtra(EXTRA_DEVICE_ADDRESS, address)

        // 设置返回值并结束程序
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    // 查找到设备和搜索完成action监听器
    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.e(TAG, action)
            // 查找到设备action
            if (BluetoothDevice.ACTION_FOUND == action) {
                // 得到蓝牙设备
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // 如果是已配对的则略过，已得到显示，其余的在添加到列表中进行显示
                if (device.bondState != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter!!.add(device.name + "\n" + device.address)
                } else {  //添加到已配对设备列表
                    mPairedDevicesArrayAdapter!!.add(device.name + "\n" + device.address)
                }
                Log.e(TAG, device.name + "---" + device.address)
                // 搜索完成action
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                setProgressBarIndeterminateVisibility(false)
                title = "选择要连接的设备"
                if (mNewDevicesArrayAdapter!!.count == 0) {
                    val noDevices = "没有找到新设备"
                    mNewDevicesArrayAdapter!!.add(noDevices)
                }
                //   if(mPairedDevicesArrayAdapter.getCount() > 0)
                //  	findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 创建并显示窗口
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)  //设置窗口显示模式为窗口方式
        setContentView(R.layout.device_list)

        // 设定默认返回值为取消
        setResult(Activity.RESULT_CANCELED)

        // 设定扫描按键响应
        val scanButton = findViewById(R.id.button_scan) as Button
        scanButton.setOnClickListener { v ->
            doDiscovery()
            v.visibility = View.GONE
        }

        // 初使化设备存储数组
        mPairedDevicesArrayAdapter = ArrayAdapter(this, R.layout.device_name)
        mNewDevicesArrayAdapter = ArrayAdapter(this, R.layout.device_name)

        // 设置已配队设备列表

        val pairedListView = findViewById(R.id.paired_devices) as ListView
        pairedListView.adapter = mPairedDevicesArrayAdapter
        pairedListView.onItemClickListener = mDeviceClickListener

        // 设置新查找设备列表
        val newDevicesListView = findViewById(R.id.new_devices) as ListView
        newDevicesListView.adapter = mNewDevicesArrayAdapter
        newDevicesListView.onItemClickListener = mDeviceClickListener

        // 注册接收查找到设备action接收器
        //IntentFilter filter = ;
        this.registerReceiver(mReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))

        // 注册查找结束action接收器
        //filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))

        this.registerReceiver(mReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED))

        // 得到本地蓝牙句柄
        mBtAdapter = BluetoothAdapter.getDefaultAdapter()

        // 得到已配对蓝牙设备列表
        val pairedDevices = mBtAdapter!!.bondedDevices

        // 添加已配对设备到列表并显示
        if (pairedDevices.size > 0) {
            //         findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (device in pairedDevices) {
                mPairedDevicesArrayAdapter!!.add(device.name + "\n" + device.address)
            }
        } else {
            val noDevices = "No devices have been paired"
            mPairedDevicesArrayAdapter!!.add(noDevices)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // 关闭服务查找
        if (mBtAdapter != null) {
            mBtAdapter!!.cancelDiscovery()
        }

        // 注销action接收器
        this.unregisterReceiver(mReceiver)
    }

    fun OnCancel(v: View) {
        finish()
    }

    /**
     * 开始服务和设备查找
     */
    private fun doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()")

        // 在窗口显示查找中信息
        setProgressBarIndeterminateVisibility(true)
        title = "查找设备中..."

        // 显示其它设备（未配对设备）列表
        title_new_devices.setVisibility(View.VISIBLE)

        // 关闭再进行的服务查找
        if (mBtAdapter!!.isDiscovering) {
            mBtAdapter!!.cancelDiscovery()
        }

        mBtAdapter!!.startDiscovery()
        Log.e(TAG, "startDiscovery()")
    }

    companion object {
        // 调试用
        private val TAG = "DeviceListActivity"
        private val D = true

        // 返回时数据标签
        var EXTRA_DEVICE_ADDRESS = "设备地址"
    }


}

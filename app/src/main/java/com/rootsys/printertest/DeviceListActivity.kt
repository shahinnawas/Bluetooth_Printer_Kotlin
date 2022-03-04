package com.rootsys.printertest

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import java.lang.Exception

class DeviceListActivity : AppCompatActivity() {
    protected val TAG = "TAG"
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mPairedDevicesArrayAdapter: ArrayAdapter<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        setResult(RESULT_CANCELED)
        mPairedDevicesArrayAdapter = ArrayAdapter(this, R.layout.device_name)

        val mPairedListView = findViewById<View>(R.id.paired_devices) as ListView
        mPairedListView.adapter = mPairedDevicesArrayAdapter
        mPairedListView.onItemClickListener = mDeviceClickListener

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        var mPairedDevices = mBluetoothAdapter?.getBondedDevices()

        if (mPairedDevices?.size!! > 0) {
            findViewById<View>(R.id.title_paired_devices).visibility = View.VISIBLE
            for (mDevice in mPairedDevices) {
                mPairedDevicesArrayAdapter!!.add(
                    """
                ${mDevice.name}
                ${mDevice.address}
                """.trimIndent()
                )
            }
        } else {
            val mNoDevices =
                "None Paired" //getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter!!.add(mNoDevices)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter!!.cancelDiscovery()
        }
    }

    private val mDeviceClickListener =
        OnItemClickListener { mAdapterView, mView, mPosition, mLong ->
            try {
                mBluetoothAdapter!!.cancelDiscovery()
                val mDeviceInfo = (mView as TextView).text.toString()
                val mDeviceAddress = mDeviceInfo.substring(mDeviceInfo.length - 17)
                Log.v(TAG, "Device_Address $mDeviceAddress")
                val mBundle = Bundle()
                mBundle.putString("DeviceAddress", mDeviceAddress)
                val mBackIntent = Intent()
                mBackIntent.putExtras(mBundle)
                setResult(RESULT_OK, mBackIntent)
                finish()
            } catch (ex: Exception) {
            }
        }
}
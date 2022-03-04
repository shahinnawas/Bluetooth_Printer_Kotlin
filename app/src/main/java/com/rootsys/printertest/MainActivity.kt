package com.rootsys.printertest

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.io.IOException
import java.lang.Exception
import java.util.*

private const val REQUEST_CONNECT_DEVICE = 1
private const val REQUEST_ENABLE_BT = 2
var mBluetoothAdapter: BluetoothAdapter? = null
var mBluetoothDevice: BluetoothDevice? = null
private var mBluetoothConnectProgressDialog: ProgressDialog? = null
private val applicationUUID = UUID
    .fromString("00001101-0000-1000-8000-00805F9B34FB")
private var mBluetoothSocket: BluetoothSocket? = null

val utils  = Utils()


class MainActivity : AppCompatActivity(), Runnable {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getPrinter()

        findViewById<View>(R.id.print).setOnClickListener {
            try {
                val os = mBluetoothSocket!!.outputStream
                val imageUrl = "iVBORw0KGgoAAAANSUhEUgAAALkAAAC5CAIAAAD7zwkLAAAABnRSTlMA/wD/AP83WBt9AAAACXBIWXMAAA7EAAAOxAGVKw4bAAAD1ElEQVR4nO3dy2rlOBRA0U7T///" +
                        "L1XMPVNvo5cBa08ot34SNOBhZ/vnz588/EPx7+wvwa2iFSitUWqHSCpVWqLRCpRUqrVBphUorVFqh0gqVVqi0QqUVKq1QaYVKK1T/zXz45+dn1fcYG28KfnyNVz88c92ZrzH+" +
                        "7D4z26utK1RaodIKlVaopmbbh4WPpY1nvYXD7OOzjx8eX2jfQHrsL/mKdYVKK1RaodIK1crZ9mHfHdKZC73yapgd//CxX3DfwRfWFSqtUGmFSitUG2fbfWbuny6897pvmP0m6wqVV" +
                        "qi0QqUVql852x6bIj9yw/QjrCtUWqHSCpVWqDbOtsdmvVe7Ysffauau7qsdC698ZGq2rlBphUorVFqhWjnbHnvY/+HVMLvwXxd+q4dbf8kx6wqVVqi0QqUVqqnZ9iP3ExduUVg4CM9c9" +
                        "5usK1RaodIKlVaoNp5vu+/G5cLjbl9dd2aLwr5Td/fthXiwrlBphUorVFqhuna+7Xj023fA68JNCAun5oV/un37GawrVFqh0gqVVqiu7bf95nsQ9p3A8OolEd9kXaHSCpVWqLRC9dFzEm" +
                        "4d63XsZQ3HbtQunNatK1RaodIKlVaorp1vu3Bn677tt7e2KMx8jfFnZ1hXqLRCpRUqrVD97NvZ+rBwTjx2fMG+na0fuTH9inWFSitUWqHSCtVH30v2kSev9s3jrxw7CWHMukKlFSqtUGm" +
                        "Fauq+7b7TthZ+q1f2Tc23Du5ayLpCpRUqrVBphWrjnoR9z2Udu9BC++75um/L52iFSitUWqE6t9/24dhEduw02IVT87GJ+xXrCpVWqLRCpRWqjXsSFh4FcOyggH0D6bF/HfMsGSdohUor" +
                        "VFqhWnnf9i9X2na4wcL/eeGjZePPjq9764fHrCtUWqHSCpVWqM7dt913jMA+3/yNbm1vsK5QaYVKK1RaoTp333ahY5tk97l1jNkM6wqVVqi0QqUVqqnzbT8yCd56IdjC637z4bEH6wqVVq" +
                        "i0QqUVqpXvbrj1irOZ/2rhq39nfHOYfbCuUGmFSitUWqHa+F6yY8/v33pZw4xju3Htt+UCrVBphUorVB995+7Yvhnz2CaEhXtmF27YGLOuUGmFSitUWqH6lbPtwgfAXg2GrybQmaHy2EnBr" +
                        "1hXqLRCpRUqrVBtnG2Pvb92336G8bj6+OzCe68LX9Drvi0XaIVKK1RaoVp5vu0+x27UPtx6O8PM13gw23KBVqi0QqUVql95vi1XWFeotEKlFSqtUGmFSitUWqHSCpVWqLRCpRUqrVBphUor" +
                        "VFqh0gqVVqi0QqUVqv8Bz74jeBbteTgAAAAASUVORK5CYII="

                val decodeByte = Base64.decode(imageUrl, Base64.NO_WRAP)
                val imageBit = BitmapFactory.decodeByteArray(decodeByte,0, decodeByte.size)

                val newBitmap = Bitmap.createBitmap(
                    imageBit.getWidth(),
                    imageBit.getHeight(), imageBit.getConfig()
                )
                val canvas = Canvas(newBitmap)
                canvas.drawColor(Color.WHITE)
                canvas.drawBitmap(imageBit, 0f, 0f, null)

                val command = utils.decodeBitmap(newBitmap)

                val printspacebootm = "\n\n\n\n\n"
                val printspcaeup = "\n"
                val line = "--------------------------------"
                val dates = "Date: "
                val hisabs = "Card No: "
                val nam = "Recipient No: "
                val cus = "Customer No: "
                val acc = "Account No: "
                val boards = "Board Name: "
                val circ = "Circle: e"
                val amnt = "Amount: amount"
                val bank = "Charges: charge"
                val tot = "Total:    total SR"
                val format = byteArrayOf(27, 33, 0)
                val change = byteArrayOf(27, 33, 0)
                format[2] = 0x10.toByte()
                change[2] = 0x5.toByte()
                os.write(command)
                os.write(printspcaeup.toByteArray())
                os.write(format)
                os.write(dates.toByteArray())
                os.write(printspcaeup.toByteArray())
                os.write(hisabs.toByteArray())
                os.write(printspcaeup.toByteArray())
                os.write(nam.toByteArray())
                os.write(printspcaeup.toByteArray())
                os.write(cus.toByteArray())
                os.write(printspcaeup.toByteArray())
                os.write(acc.toByteArray())
                os.write(printspcaeup.toByteArray())
                os.write(boards.toByteArray())
                os.write(printspcaeup.toByteArray())
                os.write(circ.toByteArray())
                os.write(printspcaeup.toByteArray())
                os.write(amnt.toByteArray())
                os.write(printspcaeup.toByteArray())
                os.write(bank.toByteArray())
                os.write(printspcaeup.toByteArray())
                os.write(line.toByteArray())
                os.write(format)
                os.write(tot.toByteArray())
                os.write(change)
                os.write(change)
                os.write(printspacebootm.toByteArray())
                Toast.makeText(this, "Printing", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error :$e", Toast.LENGTH_SHORT).show()
                Log.e("PrintActivity", "Exe ", e)
            }
        }


    }

    fun getPrinter() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            Toast.makeText(applicationContext, "Message1", Toast.LENGTH_SHORT).show()
        } else {
            if (!mBluetoothAdapter!!.isEnabled) {
                val enableBtIntent = Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE
                )
                startActivityForResult(
                    enableBtIntent,
                    REQUEST_ENABLE_BT
                )
            } else {
                ListPairedDevices()
                val connectIntent = Intent(
                    applicationContext,
                    DeviceListActivity::class.java
                )
                startActivityForResult(
                    connectIntent,
                    REQUEST_CONNECT_DEVICE
                )
            }
        }
    }

    private fun ListPairedDevices() {
        val mPairedDevices = mBluetoothAdapter
            ?.getBondedDevices()
        if (mPairedDevices != null) {
            if (mPairedDevices.size > 0) {
                for (mDevice in mPairedDevices) {
                    Log.v(
                        "TAG", "PairedDevices: " + mDevice.name + "  "
                                + mDevice.address
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, mResultCode: Int, mDataIntent: Intent?) {
        super.onActivityResult(requestCode, mResultCode, mDataIntent)
        when (requestCode) {
           REQUEST_CONNECT_DEVICE -> if (mResultCode == RESULT_OK) {
                val mExtra: Bundle? = mDataIntent?.getExtras()
                val mDeviceAddress = mExtra?.getString("DeviceAddress")
                Log.v("TAG", "Coming incoming address $mDeviceAddress")
                mBluetoothDevice = mBluetoothAdapter
                    ?.getRemoteDevice(mDeviceAddress)
                mBluetoothConnectProgressDialog = ProgressDialog.show(
                    this,
                    "Connecting...",
                    (mBluetoothDevice?.getName() ) + " : " + mBluetoothDevice?.getAddress(),
                    true,
                    false
                )
                val mBlutoothConnectThread: Thread = Thread(this)
                mBlutoothConnectThread.start()
                // pairToDevice(mBluetoothDevice); This method is replaced by
                // progress dialog with thread
            }
            REQUEST_ENABLE_BT -> if (mResultCode == RESULT_OK) {
                ListPairedDevices()
                val connectIntent = Intent(
                    this,
                    DeviceListActivity::class.java
                )
                startActivityForResult(
                    connectIntent,
                    REQUEST_CONNECT_DEVICE
                )
            } else {
                Toast.makeText(this, "Message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            mBluetoothConnectProgressDialog!!.dismiss()
            Toast.makeText(applicationContext, "Connected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun run() {
        try {
            mBluetoothSocket = mBluetoothDevice
                ?.createRfcommSocketToServiceRecord(applicationUUID)
            mBluetoothAdapter!!.cancelDiscovery()
            mBluetoothSocket?.connect()
            mHandler.sendEmptyMessage(0)
        } catch (eConnectException: IOException) {
            Log.d("TAG", "CouldNotConnectToSocket", eConnectException)
            mBluetoothSocket?.let { closeSocket(it) }
            return
        }
    }

    private fun closeSocket(nOpenSocket: BluetoothSocket) {
        try {
            nOpenSocket.close()
            Log.d("TAG", "SocketClosed")
        } catch (ex: IOException) {
            Log.d("TAG", "CouldNotCloseSocket")
        }
    }
}
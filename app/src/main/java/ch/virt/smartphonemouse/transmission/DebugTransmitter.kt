package ch.virt.smartphonemouse.transmission

import android.os.Build
import android.util.Log
import ch.virt.smartphonemouse.mouse.Processing
import ch.virt.smartphonemouse.mouse.math.Vec2f
import ch.virt.smartphonemouse.mouse.math.Vec3f
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingDeque

class DebugTransmitter {

    //Shows the status of debugging, either enabled or disabled

    val isEnabled: Boolean
    private val host: String?
    private val port: Int

    var isConnected = false
        private set
    private var connectionFailure: String? = null
    private val pendingPackets: BlockingQueue<ByteBuffer>
    private var thread: Thread? = null

    private class Column(var name: String, var type: Byte)

    private val columns: MutableList<Column>
    private var size = 0
    private var transmitting = false
    private var currentData: ByteBuffer? = null

    //creation of transmitter
    init {
        isEnabled = true
        host = "10.239.104.28"
        port = 11111
        pendingPackets = LinkedBlockingDeque()
        columns = ArrayList()
    }

    val serverString: String
        //string combination of server with hostname and port
        get() = "$host:$port"


    //connecting to SensorServer
    fun connect() {
        if (!isEnabled || isConnected) return
        Log.i(TAG, "connect: Connecting to debug host on $host:$port")
        isConnected = false
        if (thread != null) thread!!.interrupt()
        thread = Thread {
            while (true) {
                try {
                    val socket = Socket(host, port)
                    val stream = socket.getOutputStream()
                    Log.i(TAG, "connect: Successfully connected to debug host")
                    pendingPackets.clear()
                    transmitLogin()
                    reloadColumns()
                    transmitColumns()
                    isConnected = true
                    try {
                        while (socket.isConnected) {
                            val packet = pendingPackets.take()
                            stream.write(packet.array())
                            stream.flush()
                        }
                        if (socket.isConnected) socket.close()
                        isConnected = false
                    } catch (e: InterruptedException) {
                        Log.w(TAG, "Thread was interrupted, disconnecting")
                        socket.close()
                        isConnected = false
                        break
                    }
                } catch (e: IOException) {
                    isConnected = false
                    connectionFailure = e.message
                    Log.i(TAG, "connect: Failed to connect to debug host: $connectionFailure", e)
                    try {
                        Thread.sleep(10000) // Wait 10 seconds until attempting reconnection
                    } catch (ignored: InterruptedException) {
                        break
                    }
                }
            }
        }
        thread!!.start()
    }

    //disconnecting the server
    fun disconnect() {
        thread!!.interrupt()
        isConnected = false
    }

    //registration of column
    fun registerColumn(name: String, type: Class<*>) {
        if (!isEnabled) return

        // Add columns
        if (Vec2f::class.java == type) {
            columns.add(Column("$name-x", TYPE_F32))
            columns.add(Column("$name-y", TYPE_F32))
        } else if (Vec3f::class.java == type) {
            columns.add(Column("$name-x", TYPE_F32))
            columns.add(Column("$name-y", TYPE_F32))
            columns.add(Column("$name-z", TYPE_F32))
        } else if (Float::class.java == type) columns.add(
            Column(
                name,
                TYPE_F32
            )
        ) else if (Double::class.java == type) {
            columns.add(Column(name, TYPE_F64))
        } else if (Int::class.java == type) {
            columns.add(Column(name, TYPE_I32))
        } else if (Long::class.java == type) {
            columns.add(Column(name, TYPE_I64))
        } else if (Boolean::class.java == type) {
            columns.add(Column(name, TYPE_BOOL))
        }
        var size = 0
        for (c in columns) {
            when (c.type) {
                TYPE_BOOL -> size += 1
                TYPE_I32, TYPE_F32 -> size += 4
                TYPE_I64, TYPE_F64 -> size += 8
            }
        }
        this.size = size
    }

    //initialization and starting of transmission
    fun startTransmission() {
        if (!isEnabled || !isConnected) return  // Allow when not connected, to instantly start transmission
        pendingPackets.clear() // remove packets from previous transmission if present
        transmitTransmission(true)
        transmitting = true
        currentData = ByteBuffer.allocate(size)
    }

    //current transmission ended
    fun endTransmission() {
        if (!isEnabled || !isConnected || !transmitting) return
        transmitTransmission(false)
        transmitting = false
    }


    //staging a  3 dimensional float vector
    fun stageVec3f(data: Vec3f?) {
        if (!isEnabled || !isConnected || !transmitting) return
        if (currentData!!.remaining() < 3 * 4) return
        currentData!!.putFloat(data!!.x)
        currentData!!.putFloat(data.y)
        currentData!!.putFloat(data.z)
    }

    //staging a 2 dimensional float vector
    fun stageVec2f(data: Vec2f?) {
        if (!isEnabled || !isConnected || !transmitting) return
        if (currentData!!.remaining() < 2 * 4) return
        currentData!!.putFloat(data!!.x)
        currentData!!.putFloat(data.y)
    }

    //staging a float
    fun stageFloat(data: Float) {
        if (!isEnabled || !isConnected || !transmitting) return
        if (currentData!!.remaining() < 4) return
        currentData!!.putFloat(data)
    }

    //staging a double
    fun stageDouble(data: Double) {
        if (!isEnabled || !isConnected || !transmitting) return
        if (currentData!!.remaining() < 8) return
        currentData!!.putDouble(data)
    }

    //staging an integer
    fun stageInteger(data: Int) {
        if (!isEnabled || !isConnected || !transmitting) return
        if (currentData!!.remaining() < 4) return
        currentData!!.putInt(data)
    }

    //staging a long value
    fun stageLong(data: Long) {
        if (!isEnabled || !isConnected || !transmitting) return
        if (currentData!!.remaining() < 8) return
        currentData!!.putLong(data)
    }

    //staging a boolean
    fun stageBoolean(data: Boolean) {
        if (!isEnabled || !isConnected || !transmitting) return
        if (currentData!!.remaining() < 1) return
        currentData!!.put((if (data) 0x01 else 0x00).toByte())
    }

    //committing the staged data
    fun commit() {
        if (!isEnabled || !isConnected || !transmitting) return
        transmitData(currentData)
        currentData!!.clear()
    }

    //login packet transmitted to server
    private fun transmitLogin() {
        // null-terminate string here --v
        val model = (Build.MODEL + '\u0000').toByteArray(StandardCharsets.US_ASCII)
        val buffer = ByteBuffer.allocate(1 + model.size) // Packet ID + Model Name
        buffer.put(ID_LOGIN)
        buffer.put(model)
        pendingPackets.add(buffer)
    }

    //reloading required columns
    private fun reloadColumns() {
        columns.clear()
        Processing.Companion.registerDebugColumns(this)
    }

    //transmission of all registered columns to server
    private fun transmitColumns() {
        for (column in columns) {
            // null-terminate string here  v
            val name = (column.name + '\u0000').toByteArray(StandardCharsets.US_ASCII)
            val buffer = ByteBuffer.allocate(1 + 1 + name.size) // Packet ID + Type + Column Name
            buffer.put(ID_DATA_REGISTER)
            buffer.put(column.type)
            buffer.put(name)
            pendingPackets.add(buffer)
        }
    }

    //transmission of the beginning/end of transmission
    private fun transmitTransmission(start: Boolean) {
        val buffer = ByteBuffer.allocate(1 + 1) // Packet ID + Start or End
        buffer.put(ID_TRANSMISSION_STATE)
        buffer.put((if (start) 0x01 else 0x00).toByte())
        pendingPackets.add(buffer)
    }


    //transmission of processing data to server
    private fun transmitData(data: ByteBuffer?) {
        val buffer = ByteBuffer.allocate(1 + data!!.capacity()) // Packet ID + Data
        data.position(0)
        buffer.put(ID_DATA)
        buffer.put(data)
        pendingPackets.add(buffer)
    }

    companion object {
        private const val TAG = "DebugTransmitter"
        const val ID_LOGIN: Byte = 0x01
        const val ID_DATA_REGISTER: Byte = 0x02
        const val ID_TRANSMISSION_STATE: Byte = 0x03
        const val ID_DATA: Byte = 0x04
        const val TYPE_BOOL: Byte = 0x01
        const val TYPE_I32: Byte = 0x02
        const val TYPE_I64: Byte = 0x03
        const val TYPE_F32: Byte = 0x04
        const val TYPE_F64: Byte = 0x05
    }
}
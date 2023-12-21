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
    /**
     * Returns whether debugging is currently enabled
     */
    // A change requires a restart of the app
    val isEnabled: Boolean
    private val host: String?
    private val port: Int

    /**
     * Returns whether the transmitter is currently connected
     */
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

    /**
     * Creates a transmitter
     * @param preferences preferences to read enabled, host and port from
     */
    init {
//        isEnabled = preferences.getBoolean("debugEnabled", false)
//        host = preferences.getString("debugHost", "undefined")
//        port = preferences.getInt("debugPort", 55555)
        isEnabled = true
        host = "10.239.104.28"
        port = 11111
        pendingPackets = LinkedBlockingDeque()
        columns = ArrayList()
    }

    val serverString: String
        /**
         * Returns a string combination of the server hostname and port
         */
        get() = "$host:$port"
//        get() = "10.239.104.28:11111"

    /**
     * Connects to the set SensorServer
     */
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

    /**
     * Disconnects from the server
     */
    fun disconnect() {
        thread!!.interrupt()
        isConnected = false
    }

    /**
     * Registers a column for transmission
     * @param name column name
     * @param type data type of column
     */
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

        // Update size
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

    /**
     * Starts a new transmission
     */
    fun startTransmission() {
        if (!isEnabled || !isConnected) return  // Allow when not connected, to instantly start transmission
        pendingPackets.clear() // remove packets from previous transmission if present
        transmitTransmission(true)
        transmitting = true
        currentData = ByteBuffer.allocate(size)
    }

    /**
     * Ends the current transmission
     */
    fun endTransmission() {
        if (!isEnabled || !isConnected || !transmitting) return
        transmitTransmission(false)
        transmitting = false
    }

    /**
     * Stages a 3d float vector
     */
    fun stageVec3f(data: Vec3f?) {
        if (!isEnabled || !isConnected || !transmitting) return
        if (currentData!!.remaining() < 3 * 4) return
        currentData!!.putFloat(data!!.x)
        currentData!!.putFloat(data.y)
        currentData!!.putFloat(data.z)
    }

    /**
     * Stages a 2d float vector
     */
    fun stageVec2f(data: Vec2f?) {
        if (!isEnabled || !isConnected || !transmitting) return
        if (currentData!!.remaining() < 2 * 4) return
        currentData!!.putFloat(data!!.x)
        currentData!!.putFloat(data.y)
    }

    /**
     * Stages a float
     */
    fun stageFloat(data: Float) {
        if (!isEnabled || !isConnected || !transmitting) return
        if (currentData!!.remaining() < 4) return
        currentData!!.putFloat(data)
    }

    /**
     * Stages a double
     */
    fun stageDouble(data: Double) {
        if (!isEnabled || !isConnected || !transmitting) return
        if (currentData!!.remaining() < 8) return
        currentData!!.putDouble(data)
    }

    /**
     * Stages an integer
     */
    fun stageInteger(data: Int) {
        if (!isEnabled || !isConnected || !transmitting) return
        if (currentData!!.remaining() < 4) return
        currentData!!.putInt(data)
    }

    /**
     * Stages a long
     */
    fun stageLong(data: Long) {
        if (!isEnabled || !isConnected || !transmitting) return
        if (currentData!!.remaining() < 8) return
        currentData!!.putLong(data)
    }

    /**
     * Stages a boolean
     */
    fun stageBoolean(data: Boolean) {
        if (!isEnabled || !isConnected || !transmitting) return
        if (currentData!!.remaining() < 1) return
        currentData!!.put((if (data) 0x01 else 0x00).toByte())
    }

    /**
     * Commits current staged data and transmits it to the server
     */
    fun commit() {
        if (!isEnabled || !isConnected || !transmitting) return
        transmitData(currentData)
        currentData!!.clear()
    }

    /**
     * Transmits a login packet to the server
     */
    private fun transmitLogin() {
        // null-terminate string here --v
        val model = (Build.MODEL + '\u0000').toByteArray(StandardCharsets.US_ASCII)
        val buffer = ByteBuffer.allocate(1 + model.size) // Packet ID + Model Name
        buffer.put(ID_LOGIN)
        buffer.put(model)
        pendingPackets.add(buffer)
    }

    /**
     * Reloads required columns from the Processing class
     */
    private fun reloadColumns() {
        columns.clear()
        Processing.Companion.registerDebugColumns(this)
    }

    /**
     * Transmits all registered columns to the server
     */
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

    /**
     * Transmits the beginning or end of a transmission to the server
     * @param start is beginning
     */
    private fun transmitTransmission(start: Boolean) {
        val buffer = ByteBuffer.allocate(1 + 1) // Packet ID + Start or End
        buffer.put(ID_TRANSMISSION_STATE)
        buffer.put((if (start) 0x01 else 0x00).toByte())
        pendingPackets.add(buffer)
    }

    /**
     * Transmits processing data to the server
     * @param data buffer to transmit
     */
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
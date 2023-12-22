package ch.virt.ezController.transmission


 //storage of HID descriptor

object HidDescriptor {
    // Tag IDs
    private const val TAG_USAGE_PAGE: Byte = 0x05
    private const val TAG_USAGE: Byte = 0x09
    private const val TAG_COLLECTION = 0xA1.toByte()
    private const val TAG_USAGE_MIN: Byte = 0x19
    private const val TAG_USAGE_MAX: Byte = 0x29
    private const val TAG_LOGICAL_MIN: Byte = 0x15
    private const val TAG_LOGICAL_MAX: Byte = 0x25
    private const val TAG_REPORT_ID = 0x85.toByte()
    private const val TAG_REPORT_COUNT = 0x95.toByte()
    private const val TAG_REPORT_SIZE: Byte = 0x75
    private const val TAG_INPUT = 0x81.toByte()
    private const val TAG_END_COLLECTION = 0xC0.toByte()

    // Usage Page IDs
    private const val UP_GENERIC_DESKTOP: Byte = 0x01
    private const val UP_BUTTON: Byte = 0x09

    // Usage IDs
    private const val U_MOUSE: Byte = 0x02
    private const val U_POINTER: Byte = 0x01
    private const val U_X: Byte = 0x30
    private const val U_Y: Byte = 0x31
    private const val U_WHEEL: Byte = 0x38

    // Collection IDs
    private const val C_PHYSICAL: Byte = 0x00
    private const val C_APPLICATION: Byte = 0x01

    // Input IDs
    private const val I_DAT_VAR_ABS: Byte = 0x02
    private const val I_CON: Byte = 0x01
    private const val I_DAT_VAR_REL: Byte = 0x06

    // Descriptor
    val DESCRIPTOR = byteArrayOf(
        TAG_USAGE_PAGE, UP_GENERIC_DESKTOP,
        TAG_USAGE, U_MOUSE,
        TAG_COLLECTION, C_APPLICATION,
        TAG_REPORT_ID, 1,  // Record Id
        TAG_USAGE, U_POINTER,
        TAG_COLLECTION, C_PHYSICAL,
        TAG_USAGE_PAGE, UP_BUTTON,
        TAG_USAGE_MIN, 1,  // First Button 1
        TAG_USAGE_MAX, 3,  // Last Button 3
        TAG_LOGICAL_MIN, 0,
        TAG_LOGICAL_MAX, 1,
        TAG_REPORT_SIZE, 1,
        TAG_REPORT_COUNT, 3,
        TAG_INPUT, I_DAT_VAR_ABS,
        TAG_REPORT_SIZE, 5,
        TAG_REPORT_COUNT, 1,
        TAG_INPUT, I_CON,
        TAG_USAGE_PAGE, UP_GENERIC_DESKTOP,
        TAG_USAGE, U_X,
        TAG_USAGE, U_Y,
        TAG_USAGE, U_WHEEL,
        TAG_LOGICAL_MIN, 0x81.toByte(),  // -127
        TAG_LOGICAL_MAX, 0x7F,  //  127
        TAG_REPORT_SIZE, 8,
        TAG_REPORT_COUNT, 3,
        TAG_INPUT, I_DAT_VAR_REL,
        TAG_END_COLLECTION,
        TAG_END_COLLECTION
    )
}
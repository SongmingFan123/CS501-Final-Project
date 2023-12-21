package ch.virt.smartphonemouse

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import ch.virt.smartphonemouse.customization.DefaultSettings
import ch.virt.smartphonemouse.mouse.MouseInputs
import ch.virt.smartphonemouse.mouse.MovementHandler
import ch.virt.smartphonemouse.mouse.Parameters
import ch.virt.smartphonemouse.transmission.BluetoothHandler
import ch.virt.smartphonemouse.transmission.DebugTransmitter
import ch.virt.smartphonemouse.ui.ConnectFragment
import ch.virt.smartphonemouse.ui.HomeFragment
import ch.virt.smartphonemouse.ui.MouseFragment
import ch.virt.smartphonemouse.ui.SlidesControllerFragment
import ch.virt.smartphonemouse.ui.TouchpadFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import java.util.Locale


private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private var bar: MaterialToolbar? = null
    private var drawerLayout: DrawerLayout? = null
    private var drawer: NavigationView? = null
    private var bluetooth: BluetoothHandler? = null
    private var movement: MovementHandler? = null
    private var inputs: MouseInputs? = null
    private var debug: DebugTransmitter? = null
    private var mouseActive = false
    private var instanceSaved = false // Used to avoid ui changes if the activity is not rendered.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DefaultSettings.check(PreferenceManager.getDefaultSharedPreferences(this)) // Check whether new settings must be restored
        loadComponents()
        setupNavigation()
        loadContent()
        startDebugging()
        navigate(R.id.drawer_home)
        drawer!!.setCheckedItem(R.id.drawer_home)
    }

    private fun startDebugging() {
        debug = DebugTransmitter(PreferenceManager.getDefaultSharedPreferences(this))
        debug!!.connect()
    }

    /**
     * Loads the components into their variables.
     */
    private fun loadComponents() {
        drawerLayout = findViewById(R.id.drawer_layout)
        drawer = findViewById(R.id.drawer)
        bar = findViewById(R.id.bar)
    }

    /**
     * Sets the navigation up so it is ready to be used.
     */
    private fun setupNavigation() {
        bar!!.setNavigationOnClickListener { v: View? -> drawerLayout!!.open() }
        drawer!!.setNavigationItemSelectedListener { item: MenuItem ->
            if (navigate(item.itemId)) {
                drawerLayout!!.close()
                return@setNavigationItemSelectedListener false
            }
            false
        }
        drawerLayout!!.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {
                checkNavItems()
            }

            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })
    }

    /**
     * Sets the correct state of the nav items in the nav drawer.
     */
    private fun checkNavItems() {
        setNavItemEnable(R.id.drawer_connect, true)
        setNavItemEnable(R.id.drawer_mouse, bluetooth!!.isConnected)
//        if (bluetooth!!.isSupported) {
//            setNavItemEnable(R.id.drawer_connect, true)
//            setNavItemEnable(R.id.drawer_mouse, bluetooth!!.isConnected)
//        } else {
//            setNavItemEnable(R.id.drawer_connect, false)
//            setNavItemEnable(R.id.drawer_mouse, false)
//        }
    }

    /**
     * Enables / disables a certain nav item.
     *
     * @param item   item to change
     * @param enable enabled state
     */
    private fun setNavItemEnable(item: Int, enable: Boolean) {
        drawer!!.menu.findItem(item).isEnabled = enable
    }

    /**
     * Loads the contents of the app
     */
    private fun loadContent() {
        bluetooth = BluetoothHandler(this)
        inputs = MouseInputs(bluetooth!!, this)
        movement = MovementHandler(this, inputs!!)
    }

    /**
     * Updates the bluetooth status.
     */
    fun updateBluetoothStatus() {
        if (instanceSaved || currentFragment == null) return
        if (currentFragment is ConnectFragment || currentFragment is MouseFragment) {
            if (!bluetooth!!.isEnabled) navigate(R.id.drawer_home) else if (!bluetooth!!.isConnected && currentFragment is MouseFragment) navigate(
                R.id.drawer_connect
            )
        }
        runOnUiThread { if (currentFragment is HomeFragment) (currentFragment as HomeFragment?)!!.update() else if (currentFragment is ConnectFragment) (currentFragment as ConnectFragment?)!!.update() }
    }

    override fun onStart() {
        super.onStart()
        instanceSaved = false
        bluetooth!!.reInit()
        for (locale in Locale.getAvailableLocales()) {
            Log.d(
                "LOCALES",
                locale.language + "_" + locale.country + " [" + locale.displayName + "]"
            )
        }
        updateBluetoothStatus() // Current status is unknown
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        instanceSaved = true
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        instanceSaved = false
        updateBluetoothStatus() // Current status is unknown
    }

    /**
     * Navigates to the respective sites.
     *
     * @param entry entry to navigate to
     * @return whether that entry is navigated
     */
    fun navigate(entry: Int): Boolean {
        if (entry == R.id.drawer_mouse) {
            bar!!.visibility = View.GONE
            switchFragment(MouseFragment(inputs, movement), false)
            mouseActive = true
            movement!!.create(debug)
            debug!!.connect()
            debug!!.startTransmission()
            movement!!.register()
            inputs!!.start()
        } else {
            bar!!.visibility = View.VISIBLE
            if (mouseActive) {
                movement!!.unregister()
                debug!!.endTransmission()
                inputs!!.stop()
                mouseActive = false
            }
            when (entry) {
                R.id.drawer_connect -> {
                    switchFragment(ConnectFragment(bluetooth), false)
                    bar!!.setTitle(R.string.title_connect)
                }

                R.id.drawer_home -> {
                    switchFragment(HomeFragment(bluetooth), false)
                    bar!!.setTitle(R.string.title_home)
                }

                R.id.drawer_touchpad -> {
                    switchFragment(TouchpadFragment(inputs), false)
                    bar!!.visibility = View.GONE
                    mouseActive = true
                    inputs!!.start()
                }
                R.id.drawer_slides_controller -> {
                    switchFragment(SlidesControllerFragment(inputs), false)
                    bar!!.visibility = View.GONE
                    mouseActive = true
                    inputs!!.start()
                }

                else -> {
                    Toast.makeText(this, "Not yet implemented!", Toast.LENGTH_SHORT).show()
                    return false
                }
            }
        }
        drawer!!.setCheckedItem(entry)
        return true
    }

    /**
     * Switches the Fragment displayed on the app.
     *
     * @param fragment fragment that is displayed
     * @param stack    whether that fragment should be added to the back stack
     */
    private fun switchFragment(fragment: Fragment, stack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
        if (stack) transaction.addToBackStack(null)
        transaction.setReorderingAllowed(true)
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }

    private val currentFragment: Fragment?
        /**
         * Returns the currently shown fragment.
         *
         * @return currently shown fragment
         */
        private get() = supportFragmentManager.findFragmentById(R.id.container)

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) super.onBackPressed() // If something is on the backstack proceed
        else {
            if (currentFragment !is HomeFragment) { // Navigate to home if not in sub fragment and not in home
                if (currentFragment !is MouseFragment) navigate(R.id.drawer_home) // Make exception for mouse fragment
            } else super.onBackPressed()
        }
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        val args = pref.extras
        val fragment = pref.fragment?.let {
            supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                it
            )
        }
        if (fragment != null) {
            fragment.arguments = args
        }
        if (fragment != null) {
            fragment.setTargetFragment(caller, 0)
        }
        if (fragment != null) {
            switchFragment(fragment, true)
        }
        return true
    }

    override fun onStop() {
        super.onStop()
        bluetooth?.host?.disconnect()
        Log.d("MainActivity", "onStop()")
    }
}
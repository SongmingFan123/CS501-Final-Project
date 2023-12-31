package ch.virt.ezController

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
import ch.virt.ezController.customization.DefaultSettings
import ch.virt.ezController.mouse.MouseInputs
import ch.virt.ezController.mouse.MovementHandler
import ch.virt.ezController.transmission.BluetoothHandler
import ch.virt.ezController.transmission.DebugTransmitter
import ch.virt.ezController.ui.ConnectFragment
import ch.virt.ezController.ui.HomeFragment
import ch.virt.ezController.ui.MouseFragment
import ch.virt.ezController.ui.SlidesControllerFragment
import ch.virt.ezController.ui.TouchpadFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView


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
        debug = DebugTransmitter()
        debug!!.connect()
    }

    // Loads the components into their variables.
    private fun loadComponents() {
        drawerLayout = findViewById(R.id.drawer_layout)
        drawer = findViewById(R.id.drawer)
        bar = findViewById(R.id.bar)
    }

    // Sets the navigation up so it is ready to be used.
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

    // Sets the correct state of the nav items in the nav drawer.
    private fun checkNavItems() {
        setNavItemEnable(R.id.drawer_connect, true)
        setNavItemEnable(R.id.drawer_mouse, bluetooth!!.isConnected)
        setNavItemEnable(R.id.drawer_touchpad, bluetooth!!.isConnected)
        setNavItemEnable(R.id.drawer_slides_controller, bluetooth!!.isConnected)
    }

    // Enables / disables a certain nav item.
    private fun setNavItemEnable(item: Int, enable: Boolean) {
        drawer!!.menu.findItem(item).isEnabled = enable
    }

    // Loads the contents of the app
    private fun loadContent() {
        bluetooth = BluetoothHandler(this)
        inputs = MouseInputs(bluetooth!!, this)
        movement = MovementHandler(this, inputs!!)
    }

    // Updates the bluetooth status.
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

    // Navigates to the respective sites.
    fun navigate(entry: Int): Boolean {
        if (entry == R.id.drawer_mouse) {
            bar!!.visibility = View.GONE
            switchFragment(MouseFragment(inputs), false)
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

    // Switches the Fragment displayed on the app.
    private fun switchFragment(fragment: Fragment, stack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
        if (stack) transaction.addToBackStack(null)
        transaction.setReorderingAllowed(true)
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }

    //Returns the currently shown fragment.
    private val currentFragment: Fragment?
        private get() = supportFragmentManager.findFragmentById(R.id.container)

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) super.onBackPressed() // If something is on the backstack proceed
        else {
            if (currentFragment !is ConnectFragment) {
                if (currentFragment !is MouseFragment ||
                    currentFragment !is TouchpadFragment ||
                    currentFragment !is SlidesControllerFragment) navigate(R.id.drawer_connect)
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
        fragment?.setTargetFragment(caller, 0)
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
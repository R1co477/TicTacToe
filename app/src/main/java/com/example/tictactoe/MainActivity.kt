package com.example.tictactoe

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.tictactoe.contract.CustomAction
import com.example.tictactoe.contract.HasCustomAction
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.contract.Navigator
import com.example.tictactoe.databinding.ActivityMainBinding
import com.example.tictactoe.fragments.BotGameFragment
import com.example.tictactoe.fragments.DifficultyFragment
import com.example.tictactoe.fragments.EditProfileFragment
import com.example.tictactoe.fragments.MenuFragment

class MainActivity : AppCompatActivity(), Navigator {
    private lateinit var binding: ActivityMainBinding

    private val currentFragment: Fragment
        get() = supportFragmentManager.findFragmentById(R.id.fragmentContainer)!!

    private val fragmentListener = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(
            fm: FragmentManager,
            f: Fragment,
            v: View,
            savedInstanceState: Bundle?
        ) {
            super.onFragmentViewCreated(fm, f, v, savedInstanceState)
            updateUi()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }
        setSupportActionBar(binding.toolbar)
        if (savedInstanceState == null) {
            val fragment = MenuFragment()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragmentContainer, fragment)
                .commit()
        }
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentListener, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        updateUi()
        return true
    }


    private fun launchFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
            )
            .addToBackStack(null)
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        goBack()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentListener)
    }


    override fun showDifficultyScreen() {
        launchFragment(DifficultyFragment())
    }

    override fun showEditProfileScreen() {
        launchFragment(EditProfileFragment())
    }

    override fun goBack() {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun goToMenu() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private fun updateUi() {
        val fragment = currentFragment

        if (fragment is HasCustomTitle) {
            binding.toolbar.title = getString(fragment.getTitleRes())
        } else {
            binding.toolbar.title = getString(R.string.toolbar_menu)
        }

        if (supportFragmentManager.backStackEntryCount > 0) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        } else {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayShowHomeEnabled(false)
        }

        if (fragment is HasCustomAction) {
            createCustomToolbarAction(fragment.getCustomAction())
        } else {
            binding.toolbar.menu.clear()
        }
    }

    override fun createCustomToolbarAction(action: CustomAction) {
        binding.toolbar.menu.clear()

        val iconDrawable = DrawableCompat.wrap(ContextCompat.getDrawable(this, action.iconRes)!!)
        iconDrawable.setTint(Color.WHITE)

        val menuItem = binding.toolbar.menu.add(action.textRes)
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menuItem.icon = iconDrawable
        menuItem.setOnMenuItemClickListener {
            action.onCustomAction.run()
            return@setOnMenuItemClickListener true
        }
    }

    override fun showSingleGameScreen(level: Int) {
        launchFragment(BotGameFragment.newInstance(level))
    }
}
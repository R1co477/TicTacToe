package com.example.tictactoe

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.contract.Navigator
import com.example.tictactoe.databinding.ActivityMainBinding
import com.example.tictactoe.fragments.BoardFragment
import com.example.tictactoe.fragments.DifficultyFragment
import com.example.tictactoe.fragments.MenuFragment

class MainActivity : AppCompatActivity(), Navigator {
    private lateinit var binding: ActivityMainBinding

    private val currentFragment: Fragment
        get() = supportFragmentManager.findFragmentById(R.id.fragmentContainer)!!

    private val fragmentListener = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
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

    private fun launchFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
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
    }

    override fun showSingleGameScreen(level: Int) {
        launchFragment(BoardFragment.newInstance())
    }
}
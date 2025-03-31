package com.example.tictactoe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.tictactoe.ai.Mark
import com.example.tictactoe.contract.CustomAction
import com.example.tictactoe.contract.HasCustomAction
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.contract.Navigator
import com.example.tictactoe.databinding.ActivityMainBinding
import com.example.tictactoe.extensions.getObject
import com.example.tictactoe.screens.APP_PREFERENCES
import com.example.tictactoe.screens.BotGameFragment
import com.example.tictactoe.screens.DifficultyFragment
import com.example.tictactoe.screens.EditProfileFragment
import com.example.tictactoe.screens.GameOverFragment
import com.example.tictactoe.screens.KEY_PROFILE
import com.example.tictactoe.screens.LocalMultiplayerFragment
import com.example.tictactoe.screens.MenuFragment
import com.example.tictactoe.screens.OnRefreshClick
import com.example.tictactoe.screens.SettingsFragment
import com.example.tictactoe.utils.AvatarManager

class MainActivity : AppCompatActivity(), Navigator {
    private lateinit var binding: ActivityMainBinding

    private val currentFragment: Fragment
        get() = supportFragmentManager.findFragmentById(R.id.fragmentContainer)!!

    private val fragmentListener = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
            super.onFragmentStarted(fm, f)
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
                .setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out
                )
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

    override fun showSettingsMenuScreen() {
        launchFragment(SettingsFragment())
    }

    override fun showBotGameScreen(level: Int) {
        launchFragment(BotGameFragment.newInstance(level))
    }

    override fun showLocalMultiplayerScreen() {
        launchFragment(LocalMultiplayerFragment())
    }

    override fun showGameOverScreen(
        humanEntityCard: EntityCard,
        opponentEntityCard: EntityCard,
        resultGame: ResultGame,
        onRefreshClick: () -> Unit
    ) {

        val fragment = GameOverFragment.newInstance(
            humanEntityCard,
            opponentEntityCard,
            resultGame
        )

        fragment.onRefreshClick = onRefreshClick

        launchFragment(fragment)
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
}
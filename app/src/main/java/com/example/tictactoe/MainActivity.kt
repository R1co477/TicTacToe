package com.example.tictactoe

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.tictactoe.contract.CustomAction
import com.example.tictactoe.contract.HasCustomAction
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.contract.Navigator
import com.example.tictactoe.data.EntityCard
import com.example.tictactoe.data.ResultGame
import com.example.tictactoe.databinding.ActivityMainBinding
import com.example.tictactoe.fragments.BotGameFragment
import com.example.tictactoe.fragments.DifficultyFragment
import com.example.tictactoe.fragments.EditProfileFragment
import com.example.tictactoe.fragments.GameOverFragment
import com.example.tictactoe.fragments.LobbyFragment
import com.example.tictactoe.fragments.LocalMultiplayerFragment
import com.example.tictactoe.fragments.MenuFragment
import com.example.tictactoe.fragments.MultiplayerFragment
import com.example.tictactoe.fragments.MultiplayerGameFragment
import com.example.tictactoe.fragments.SettingsFragment

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
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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
            .addToBackStack(fragment::class.java.name)
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

    override fun showMultiplayerScreen() {
        launchFragment(MultiplayerFragment())
    }

    override fun showLobbyScreen(roomName: String) {
        launchFragment(LobbyFragment.newInstance(roomName))
    }

    override fun showMultiplayerGameScreen(roomName: String, secondPlayer: String, createdBy: String) {
        launchFragment(MultiplayerGameFragment.newInstance(roomName, secondPlayer, createdBy))
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
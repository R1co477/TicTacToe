package com.example.tictactoe.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tictactoe.Profile
import com.example.tictactoe.R
import com.example.tictactoe.Settings
import com.example.tictactoe.ai.Board
import com.example.tictactoe.ai.Mark
import com.example.tictactoe.ai.Minimax
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.databinding.FragmentBotGameBinding
import com.example.tictactoe.extensions.getObject
import com.example.tictactoe.utils.AvatarManager
import kotlinx.coroutines.delay
import kotlin.properties.Delegates
import kotlin.random.Random

class BotGameFragment : Fragment(), HasCustomTitle {
    private lateinit var binding: FragmentBotGameBinding
    private var levelDifficulty: Int by Delegates.notNull<Int>()
    private lateinit var profile: Profile
    private lateinit var settings: Settings
    private lateinit var sharedPref: SharedPreferences
    private lateinit var minimax: Minimax
    private val board: Board = Board.empty()
    private lateinit var humanMark: Mark
    private lateinit var computerMark: Mark

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = requireContext().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)

        profile = sharedPref.getObject(
            KEY_PROFILE, Profile.default(requireContext())
        )

        settings = sharedPref.getObject(
            KEY_SETTINGS, Settings()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBotGameBinding.inflate(inflater, container, false)

        levelDifficulty = arguments?.getInt(ARG_LEVEL)!!
        when (levelDifficulty) {
            1 -> setImage(R.drawable.easy_bot).also { setInfo(R.string.easy_bot) }
            2 -> setImage(R.drawable.medium_bot).also { setInfo(R.string.medium_bot) }
            3 -> setImage(R.drawable.difficult_bot).also { setInfo(R.string.difficult_bot) }
        }
        setupProfile()
        setupSettings()


        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        // remove listener
    }

    private fun setupProfile() {
        val nickname = profile.nickname
        binding.playerTextview.text = nickname
        AvatarManager(profile).setAvatar(binding.playerAvatar, binding.firstLetterTextView)
    }

private fun setupSettings() {
    val tic = R.drawable.tic
    val tac = R.drawable.tac

    humanMark = when {
        settings.randomMark -> if (Random.nextBoolean()) Mark.TIC else Mark.TAC
        settings.humanTic -> Mark.TIC
        else -> Mark.TAC
    }

    computerMark = if (humanMark == Mark.TIC) Mark.TAC else Mark.TIC
    val humanMove = getHumanMove()

    with(binding) {
        if (humanMark == Mark.TIC) {
            markHuman.setImageResource(tic)
            markComputer.setImageResource(tac)
        } else {
            markHuman.setImageResource(tac)
            markComputer.setImageResource(tic)
        }

        minimax = Minimax(board, computerMark, levelDifficulty * 4)
        gameBoard.addListener { r, c ->
            board[r, c] = humanMark
            computerTurn()
        }

        gameBoard.humanMark = if (humanMark == Mark.TIC) R.drawable.cell_tic else R.drawable.cell_tac

        if (humanMove) {
            humanTurn()
        } else {
            computerTurn()
        }
    }
}

    private fun getHumanMove(): Boolean {
        return when {
            settings.randomMove -> Random.nextBoolean()
            settings.humanMove -> true
            else -> false
        }
    }

    private fun humanTurn() {
        binding.apply {
            humanStatusView.setBackgroundResource(R.drawable.active_turn_background)
            txtHumanTurn.visibility = TextView.VISIBLE
        }
    }

    private fun computerTurn() {
        binding.apply {
            humanStatusView.setBackgroundResource(R.drawable.inactive_turn_background)
            txtHumanTurn.visibility = TextView.INVISIBLE
        }
        val (r, c) = minimax.findBestMove()
        board[r, c] = computerMark
        binding.gameBoard.setMove(r, c, computerMark)
        humanTurn()
    }

    private fun setImage(@DrawableRes imageResId: Int) {
        val drawable = ContextCompat.getDrawable(requireContext(), imageResId)
        binding.botAvatar.setImageDrawable(drawable)
    }

    private fun setInfo(@StringRes stringResId: Int) {
        binding.botTextView.text = getString(stringResId)
    }

    override fun getTitleRes(): Int = R.string.toolbar_bot

    companion object {
        private const val ARG_LEVEL = "levelDifficulty"

        fun newInstance(level: Int): BotGameFragment {
            val args = Bundle()
            args.putInt(ARG_LEVEL, level)
            val fragment = BotGameFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
package com.example.tictactoe.fragments

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.tictactoe.data.EntityCard
import com.example.tictactoe.R
import com.example.tictactoe.data.ResultGame
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.databinding.FragmentGameOverBinding

typealias OnRefreshClick = () -> Unit

class GameOverFragment : Fragment(), HasCustomTitle {
    private lateinit var binding: FragmentGameOverBinding

    var onRefreshClick: OnRefreshClick? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameOverBinding.inflate(inflater, container, false)

        val humanEntityCard =
            arguments.getParcelableCompat(HUMAN_ENTITY_CARD_KEY, EntityCard::class.java)
        val botEntityCard =
            arguments.getParcelableCompat(BOT_ENTITY_CARD_KEY, EntityCard::class.java)
        val resultGame = arguments.getParcelableCompat(RESULT_GAME_KEY, ResultGame::class.java)

        humanEntityCard?.let { binding.humanEntityCard.loadEntityCard(it) }

        botEntityCard?.let { binding.botEntityCard.loadEntityCard(it) }

        resultGame?.let { binding.resultGame.loadResultGame(it) }

        binding.btRefresh.setOnClickListener {
            parentFragmentManager.popBackStack()
            parentFragmentManager.popBackStack()
            onRefreshClick?.invoke()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    parentFragmentManager.popBackStack()
                    parentFragmentManager.popBackStack()
                }
            })
    }


    private fun <T : Parcelable> Bundle?.getParcelableCompat(key: String, clazz: Class<T>): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this?.getParcelable(key, clazz)
        } else {
            @Suppress("DEPRECATION")
            this?.getParcelable(key)
        }

    }

    override fun getTitleRes(): Int {
        return R.string.toolbar_game_result
    }

    companion object {
        private const val RESULT_GAME_KEY = "resultGame"
        private const val HUMAN_ENTITY_CARD_KEY = "humanEntityCard"
        private const val BOT_ENTITY_CARD_KEY = "botEntityCard"

        fun newInstance(
            humanEntityCard: EntityCard,
            botEntityCard: EntityCard,
            resultGame: ResultGame,
        ): GameOverFragment {
            val fragment = GameOverFragment()
            val args = Bundle().apply {
                putParcelable(HUMAN_ENTITY_CARD_KEY, humanEntityCard)
                putParcelable(BOT_ENTITY_CARD_KEY, botEntityCard)
                putParcelable(RESULT_GAME_KEY, resultGame)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
package com.example.tictactoe.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.tictactoe.App
import com.example.tictactoe.R
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.contract.navigator
import com.example.tictactoe.data.Profile
import com.example.tictactoe.data.RoomRepository
import com.example.tictactoe.data.RoomResponse
import com.example.tictactoe.databinding.FragmentLobbyBinding
import com.example.tictactoe.utils.AvatarManager
import com.example.tictactoe.utils.SnackBarUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LobbyFragment : Fragment(), HasCustomTitle {

    private lateinit var binding: FragmentLobbyBinding
    private lateinit var roomRepository: RoomRepository
    private lateinit var profile: Profile
    private var roomName: String = ""
    private var refreshJob: Job? = null
    private val refreshIntervalMs = 3_000L
    private var shouldExitLobby = false

    private val appLifecycleObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_STOP && isAdded) {
            deleteRoom()
            shouldExitLobby = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roomRepository = RoomRepository()

        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)

        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    deleteRoomAndLeaveLobby()
                }
            }
        )
        shouldExitLobby = savedInstanceState?.getBoolean(KEY_SHOULD_EXIT_LOBBY) == true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLobbyBinding.inflate(inflater, container, false)
        profile = (requireContext().applicationContext as App).profile
        roomName = arguments?.getString(KEY_ROOM_NAME).orEmpty()

        binding.tvRoom.text = roomName
        AvatarManager(profile).setAvatar(binding.ivAvatar)

        binding.btCancel.setOnClickListener {
            deleteRoomAndLeaveLobby()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        refreshJob = lifecycleScope.launch {
            while (true) {
                delay(refreshIntervalMs)
                val room = getCurrentRoom()
                if (room != null) {
                    if (room.secondPlayer != "") {
                        navigator().showMultiplayerGameScreen(
                            room.name,
                            room.secondPlayer,
                            room.createdBy
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        refreshJob?.cancel()
    }

    override fun onResume() {
        super.onResume()
        if (shouldExitLobby && isAdded) {
            parentFragmentManager.popBackStack()
            shouldExitLobby = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(appLifecycleObserver)
    }

    private suspend fun getCurrentRoom(): RoomResponse? {
        var roomResponse: RoomResponse? = null
        val result = roomRepository.getAllRooms()
        result
            .onSuccess { rooms ->
                roomResponse = rooms.find {
                    it.name == roomName && it.createdBy == profile.nickname
                }
            }
            .onFailure { e ->
                SnackBarUtils.showCustomSnackBar(
                    binding.root,
                    e.toString(),
                    e.toString(),
                    R.color.error_color
                )
            }
        return roomResponse
    }


    private fun deleteRoom() {
        lifecycleScope.launch {
            try {
                val room = getCurrentRoom()

                if (room != null) {
                    withContext(Dispatchers.IO) {
                        roomRepository.deleteRoom(room.id.toULong())
                    }
                } else {
                    SnackBarUtils.showCustomSnackBar(
                        binding.root,
                        getString(R.string.error_room_not_found),
                        "",
                        R.color.error_color
                    )
                }
            } catch (e: Exception) {
                SnackBarUtils.showCustomSnackBar(
                    binding.root,
                    getString(R.string.error_room_delete_failed),
                    e.localizedMessage ?: e.toString(),
                    R.color.error_color
                )
            }
        }
    }

    private fun deleteRoomAndLeaveLobby() {
        deleteRoom()
        parentFragmentManager.popBackStack()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_SHOULD_EXIT_LOBBY, shouldExitLobby)
    }

    override fun getTitleRes(): Int = R.string.toolbar_lobby

    companion object {
        private const val KEY_SHOULD_EXIT_LOBBY = "SHOULD_EXIT_LOBBY"
        const val KEY_ROOM_NAME = "ROOM_NAME"

        fun newInstance(roomName: String): LobbyFragment {
            return LobbyFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_ROOM_NAME, roomName)
                }
            }
        }
    }
}
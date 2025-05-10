package com.example.tictactoe.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tictactoe.App
import com.example.tictactoe.R
import com.example.tictactoe.contract.CustomAction
import com.example.tictactoe.contract.HasCustomAction
import com.example.tictactoe.contract.HasCustomTitle
import com.example.tictactoe.contract.navigator
import com.example.tictactoe.data.Profile
import com.example.tictactoe.data.RoomActionListener
import com.example.tictactoe.data.RoomRepository
import com.example.tictactoe.data.RoomRequest
import com.example.tictactoe.data.RoomResponse
import com.example.tictactoe.data.RoomsAdapter
import com.example.tictactoe.data.SignInRoomRequest
import com.example.tictactoe.databinding.FragmentMultiplayerBinding
import com.example.tictactoe.extensions.getObject
import com.example.tictactoe.utils.SnackBarUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MultiplayerFragment : Fragment(), HasCustomTitle, HasCustomAction {

    private lateinit var binding: FragmentMultiplayerBinding
    private lateinit var adapter: RoomsAdapter
    private lateinit var roomRepository: RoomRepository
    private var pendingRoom: SignInRoomRequest? = null

    private var refreshJob: Job? = null
    private val refreshIntervalMs = 3_000L
    private lateinit var currentRoomResponse: RoomResponse
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roomRepository = RoomRepository()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMultiplayerBinding.inflate(inflater, container, false)
        adapter = createAdapter()

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        observeEnterPasswordDialogResult()

        observeAddRoomDialogResult()

        binding.swiperefresh.setOnRefreshListener {
            refresh()
        }

        lifecycleScope.launch {
            delay(200)
            binding.shimmerLayout.startShimmer()
            refresh()
        }

        return binding.root
    }

    private fun observeEnterPasswordDialogResult() {
        parentFragmentManager.setFragmentResultListener(
            EnterPasswordDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val password = bundle.getString(EnterPasswordDialogFragment.KEY_PASSWORD) ?: ""
            if (pendingRoom != null) {
                Log.e("RoomService", "Pending is not null")
                val signInRoomRequest =
                    SignInRoomRequest(pendingRoom!!.name, password, pendingRoom!!.second_player)
                lifecycleScope.launch {
                    roomRepository.signInRoom(signInRoomRequest)
                        .onSuccess {
                            Log.d("RoomService", "Success: $it")
                            navigator().showMultiplayerGameScreen(
                                currentRoomResponse.name,
                                (requireContext().applicationContext as App).profile.nickname,
                                currentRoomResponse.createdBy
                            )
                        }
                        .onFailure {
                            val errorSignInRoomPassword =
                                getString(R.string.error_sign_in_room_password)
                            SnackBarUtils.showCustomSnackBar(
                                binding.root,
                                errorSignInRoomPassword,
                                errorSignInRoomPassword,
                                R.color.error_color
                            )
                        }
                }
            } else {
                Log.e("RoomService", "Pending room is null")
            }
        }
    }

    private fun observeAddRoomDialogResult() {
        parentFragmentManager.setFragmentResultListener(
            AddRoomDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            lifecycleScope.launch {
                val name = bundle.getString(AddRoomDialogFragment.KEY_NAME).orEmpty()
                val protected = bundle.getBoolean(AddRoomDialogFragment.KEY_IS_PROTECTED)
                val password = bundle.getString(AddRoomDialogFragment.KEY_PASSWORD).orEmpty()
                val sharedPref =
                    requireContext().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
                val profile = sharedPref.getObject(KEY_PROFILE, Profile.default(requireContext()))

                val roomRequest = RoomRequest(name, protected, password, profile.nickname, "")
                val result = roomRepository.createRoom(roomRequest)
                result
                    .onSuccess {
                        navigator().showLobbyScreen(name)
                    }
                    .onFailure {
                        val errorCreateRoom = getString(R.string.error_create_room)
                        SnackBarUtils.showCustomSnackBar(
                            binding.root,
                            errorCreateRoom,
                            errorCreateRoom,
                            ContextCompat.getColor(requireContext(), R.color.error_color)
                        )
                    }
            }

        }
    }

    private fun refresh() {
        lifecycleScope.launch {
            val result = roomRepository.getAllRooms()

            result
                .onSuccess { rooms ->
                    adapter.rooms = rooms
                    binding.shimmerLayout.apply {
                        stopShimmer()
                        visibility = View.GONE
                    }
                    binding.recyclerView.visibility = View.VISIBLE
                }
                .onFailure { e ->
                    Log.e("RoomService", "$e")
                    binding.shimmerLayout.apply {
                        startShimmer()
                        visibility = View.VISIBLE
                    }
                    binding.recyclerView.visibility = View.INVISIBLE
                    val error_fetch_rooms = getString(R.string.error_fetch_rooms)
                    SnackBarUtils.showCustomSnackBar(
                        binding.root,
                        error_fetch_rooms,
                        error_fetch_rooms,
                        R.color.error_color
                    )
                }
            binding.swiperefresh.isRefreshing = false
        }
    }

    override fun onStart() {
        super.onStart()
        refreshJob = lifecycleScope.launch {
            while (true) {
                delay(refreshIntervalMs)
                refresh()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        refreshJob?.cancel()
    }

    private fun createAdapter(): RoomsAdapter = RoomsAdapter(object : RoomActionListener {
        override fun onRoomPlay(roomResponse: RoomResponse) {
            if (roomResponse.secondPlayer != "") {
                return
            }
            pendingRoom = SignInRoomRequest(
                roomResponse.name,
                "",
                (requireContext().applicationContext as App).profile.nickname
            )
            if (roomResponse.open) {
                lifecycleScope.launch {
                    roomRepository.signInRoom(pendingRoom!!)
                        .onSuccess {
                            Log.d("RoomService", "Success: $it")
                            navigator().showMultiplayerGameScreen(
                                roomResponse.name,
                                (requireContext().applicationContext as App).profile.nickname,
                                roomResponse.createdBy
                            )
                        }
                        .onFailure {
                            val errorSignInRoom = getString(R.string.error_sign_in_room)
                            SnackBarUtils.showCustomSnackBar(
                                binding.root,
                                errorSignInRoom,
                                errorSignInRoom,
                                R.color.error_color
                            )
                        }
                }

            } else {
                currentRoomResponse = roomResponse
                val enterPasswordDialogFragment = EnterPasswordDialogFragment()
                enterPasswordDialogFragment.show(
                    parentFragmentManager,
                    EnterPasswordDialogFragment.TAG
                )
            }
        }

        override fun onRoom(roomResponse: RoomResponse) {}
    })

    override fun getTitleRes(): Int = R.string.toolbar_multiplayer

    override fun getCustomAction(): CustomAction {
        return CustomAction(R.drawable.ic_add, R.string.add_room) {
            val addRoomDialogFragment = AddRoomDialogFragment()
            addRoomDialogFragment.show(parentFragmentManager, AddRoomDialogFragment.TAG)
        }
    }
}
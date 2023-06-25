package com.example.inujungplayer.ui.home

import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.inujungplayer.R
import com.example.inujungplayer.adapter.MusicAdapter
import com.example.inujungplayer.constant.MusicConstants
import com.example.inujungplayer.constant.MusicDevice.isRadioOn
import com.example.inujungplayer.constant.MusicDevice.isVideo
import com.example.inujungplayer.constant.MusicDevice.musicIDList
import com.example.inujungplayer.constant.MusicDevice.musicList
import com.example.inujungplayer.constant.MusicDevice.oldMusic
import com.example.inujungplayer.constant.MusicDevice.songID
import com.example.inujungplayer.constant.PendinIntent
import com.example.inujungplayer.constant.PendinIntent.lPauseIntent
import com.example.inujungplayer.databinding.FragmentHomeBinding
import com.example.inujungplayer.model.Music
import com.example.inujungplayer.utils.Injector
import com.example.inujungplayer.utils.MyApplication
import com.example.inujungplayer.utils.fastscroller.FastScroller
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class HomeFragment : Fragment() {
    private lateinit var callback: OnBackPressedCallback
    //    val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
//    val homeViewModel: HomeViewModel by activityViewModels { ViewModelFactory(MyApplication.repository!!) }
    private val homeViewModel: HomeViewModel by viewModels {
        Injector.provideHomeListViewModelFactory(MyApplication.applicationContext())
    }
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    lateinit var permissions: Array<String>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var musicAdapter: MusicAdapter
    lateinit var handleView: ImageView
    lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        recyclerView = binding.recyclerViewList
        val searchView = binding.searchViewMusic
        searchView.bringToFront() // 서티뷰 터치시 아래 안보이게

        musicAdapter =  MusicAdapter( { music, view -> menuActions(music, view) },
            { music -> itemClick(music) }, { music -> artistLongClick(music)})

        recyclerView.itemAnimator = null // notifiitemchanged 화면깜빡임 방지
        recyclerView.setHasFixedSize(true)// notifiitemchanged 화면깜빡임 방지

        handleView = binding.handleView
        handleView.clipToOutline = true
        handleView.bringToFront()
//        childFragManager = ChildFra
        recyclerView.also { view ->
            view.layoutManager = GridLayoutManager(requireContext(), 1)
            view.adapter = musicAdapter
        }
        FastScroller(handleView).bind(recyclerView) /*, bubbleListener*/

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                homeViewModel.searchQueryMusics("%$query%").observeOnce(requireActivity()) {
                    musicAdapter.submitList(it)
                    musicList = it
                    musicIDList.clear()
                    for (m in musicList) {
                        musicIDList.add(m.id)
                        if (m.isSelected == true  && m.id != songID) {
                            m.isSelected = false
                            homeViewModel.updateMusicFalse(m)
                        }
                    }
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        binding.buttonAllmusic.setOnClickListener {// 처음실행시 불러오는 것으로 하면 false 초기화됨
//            isVideo = false
//            Log.d("buttonAllmusic","songID=> ${musicList[musicIDList.indexOf(songID)].id}")
            homeViewModel.reLoadMusics().observeOnce(this, Observer<List<Music>>  {
                if(it != null){
                    musicAdapter.submitList(it)
                    musicList = it
                    musicIDList.clear()
                    for (m in musicList) {
                        musicIDList.add(m.id)
                        if (m.isSelected == true && m.id != songID) {
                            m.isSelected = false
                            homeViewModel.updateMusicFalse(m)
//                            Log.d("buttonAllmusic","m id=> ${m.id}")
                        }
                    }
                }
            })
        }

        binding.imageViewShuffle.setOnClickListener {
            if (!isRadioOn) {
                val temp = musicList.shuffled()
                musicAdapter.submitList(temp)
                musicList = temp
                musicIDList.clear()
                for (m in musicList) {
                    musicIDList.add(m.id)
                }
            }
        }

        binding.imageViewGoto.setOnClickListener {
            recyclerView.scrollToPosition(musicIDList.indexOf(songID))
        }

        binding.imageViewFile.setOnClickListener {
            isVideo = true
            updateVideo()
        }
        permissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            arrayOf(android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_AUDIO)
        }//, Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
//                if (!isLoadAll) // 부팅시만 전체 로딩
//                    index = -1
                updateData()
                subscribeUi(musicAdapter)
                subscribeMusic(musicAdapter)

            } else {
                Toast.makeText(requireContext(), "권한 요청 실행해야지 앱 실행", Toast.LENGTH_SHORT).show()
                exitProcess(0)
            }
        }
        permissions.forEach { p ->
            requestPermissionLauncher.launch(p)
//            Log.d("FragmentListVM", " permissions.forEach, isLoadAll= $isLoadAll" )
        }

        MyApplication.musicRepository.songid.observe(viewLifecycleOwner) { id ->
            musicAdapter.submitList(musicList)
//            Log.d("songidobserve","${id}")
            itemUpdate(id)
        }


        // Inflate the layout for this fragment
        return root
    }

    private fun updateVideo() { // vedio 추가시
        CoroutineScope(Dispatchers.Default).launch {
            musicList = homeViewModel.setVideosData()
            musicIDList.clear()
            for (m in musicList) {
                musicIDList.add(m.id)
                if (m.isSelected == true && m.id != songID) { // 처음 로딩시 일찍 음악 클릭하면 그것까지 false 제외
                    m.isSelected = false
                    homeViewModel.updateMusicFalse(m)
                }
            }
            musicAdapter.submitList(musicList) // 처음 설치때 위해 submitlist
            /*
                        var i = 0
                        for (v in musicList) {
                            val file = File(v.path)
                            if (file.exists()) {
                                i+=1
                            }
                        }
                        Log.d("fileexist:","$i,  ${musicList.size}")*/
        }
    }
    private fun updateData() { // mp3 추가시
        CoroutineScope(Dispatchers.Default).launch {
//            homeViewModel.setMusicsData()
            musicList = homeViewModel.setMusicsData()
            Log.d("dddddddddddddd","updateData(home), ${musicList.size} ")
            musicIDList.clear()
            for (m in musicList) {
                musicList = homeViewModel.setMusicsData()
                Log.d("dddddddddddddd","updateData(home), ${m.title} ")
                musicIDList.add(m.id)
                if (m.isSelected == true && m.id != songID) { // 처음 로딩시 일찍 음악 클릭하면 그것까지 false 제외
                    m.isSelected = false
                    homeViewModel.updateMusicFalse(m)
                }
            }
            musicAdapter.submitList(musicList) // 처음 설치때 위해 submitlist
        }
    }


    private fun itemUpdate(id: Int) {
//        Log.d("itempUpdate(iss))", " music: $")
        val i = musicIDList.indexOf(id)
        if (i != -1) {
            val m = musicList[i]
            if (id != oldMusic?.id) {
                oldMusic?.isSelected = false
                if (oldMusic != null) homeViewModel.updateMusicFalse(oldMusic!!)
                musicAdapter.notifyChanged(musicIDList.indexOf(oldMusic?.id))

                m.isSelected = true
                homeViewModel.updateMusicTrue(m) // 룸변경시 전체로드됨, postValue만 함
//            musicAdapter.notifyChanged(m.id)
//                   Log.d("itemClicks(if))", " music: ${music.isSelected}: ${music.title}, ${oldMusic?.isSelected}:${oldMusic?.title},  ")
            } else {
                m.isSelected = false
                homeViewModel.updateMusicTrue(m)
//                   Log.d("itemClicks(else)", " music: ${music.isSelected}: ${music.title}, ${oldMusic?.isSelected}:${oldMusic?.title},  ")
            }
        }
    }

    private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }
    private fun subscribeUi(adapter: MusicAdapter) { // 처음 실행시 모두 false
        homeViewModel.musics.observeOnce(this, Observer<List<Music>>  {
            if(it != null){
                adapter.submitList(it)
                musicList = it
                musicIDList.clear()
                for (m in musicList) {
                    musicIDList.add(m.id)
                    if (m.isSelected == true) {
                        m.isSelected = false
                        homeViewModel.updateMusicFalse(m)
                    }
                }
            }
        })
    }

    private fun subscribeMusic(adapter: MusicAdapter) {
        homeViewModel.music.observe(viewLifecycleOwner) {
            adapter.notifyChanged(musicIDList.indexOf(it.id))
//            Log.d("itemClick(subscribeMusic)","${it.title}")
        }
    }
    // 위 _songid observer 용
    private fun itemClick(music: Music) {
//        MyApplication.musicRepository._songid.value = music.id
    }
    private fun artistLongClick(music: Music) {}

    private fun menuActions(music: Music, view: View) {

        val pop= PopupMenu(view.context, view)
        pop.inflate(R.menu.itemlist_menu)
        pop.setOnMenuItemClickListener { item->
            when(item.itemId)
            {
                R.id.action_info->{
                    homeViewModel.showInfo(view, music)
                }
                R.id.action_modify->{
//                    Log.d("performUpdateMusic","R.id.action_modify")
                    val updateView =  View.inflate(view.context, R.layout.update_dialog, null)
                    val editTextArtist = updateView.findViewById<EditText>(R.id.editTextArtistName)
                    val editTextTitle = updateView.findViewById<EditText>(R.id.editTextTitle)
                    editTextArtist.setText(music.artist, TextView.BufferType.SPANNABLE)
                    editTextTitle.setText(music.title, TextView.BufferType.SPANNABLE)
                    val dlg = MaterialAlertDialogBuilder(requireContext())
                    dlg.setView(updateView)
                        .setPositiveButton(R.string.update_dialog_positive, DialogInterface.OnClickListener
                        { dialog, which ->
//                            Log.d("performUpdateMusic", "currentposition=> ${music.currentPosition}, ${editTextArtist.text}, ${editTextTitle.text}")
                            music.title = editTextTitle.text.toString()
                            music.artist = editTextArtist.text.toString()
//                            homeViewModel.updateMusic(music)

                        })
                        .setNegativeButton(R.string.delete_dialog_negative, DialogInterface.OnClickListener { dialog, which ->
                            dialog.dismiss()
                        })
                        .show()
                }
                R.id.action_delete-> {
//                    Log.d("performUpdateMusic","R.id.action_delete")
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.delete_dialog_title)
                        .setMessage(getString(R.string.delete_dialog_message, music.title))
                        .setPositiveButton(R.string.delete_dialog_positive) { _: DialogInterface, _: Int ->
//                            homeViewModel.deleteMusic(music)
                        }
                        .setNegativeButton(R.string.delete_dialog_negative) { dialog: DialogInterface, _: Int ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
            true
        }
        pop.show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                activity?.supportFragmentManager?.popBackStack("file", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                childFragmentManager.popBackStack(Environment.getExternalStorageDirectory().absolutePath, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                Log.d("aaaaaaa", "occur back pressed event!!")
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }
    override fun onDetach() {
        super.onDetach()
        callback.remove()
        // 화면 올리면 서비스 프로세스 종료
        lPauseIntent.action = MusicConstants.ACTION.STOP_ACTION
//        MusicDevice.isPlaying = false
        val lPendingPauseIntent = PendingIntent.getService(requireContext(),0, PendinIntent.lPauseIntent, PendingIntent.FLAG_IMMUTABLE)
        try {
            lPendingPauseIntent.send()
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }
//        Log.d("MusicUpdate_rootView(else)", " onDetach")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
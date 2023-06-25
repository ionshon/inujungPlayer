package com.example.inujungplayer.ui.radio

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import com.example.inujungplayer.adapter.RadioAdapter
import com.example.inujungplayer.constant.MusicDevice.oldRadio
import com.example.inujungplayer.constant.MusicDevice.radioList
import com.example.inujungplayer.databinding.FragmentRadioBinding
import com.example.inujungplayer.repository.RadioRepo
import com.example.inujungplayer.utils.InjectorRadio
import com.example.inujungplayer.utils.MyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RadioFragment : Fragment() {
    private val radioViewModel: RadioViewModel by viewModels {
        InjectorRadio.provideRadioListViewModelFactory(requireContext())
    }

    private var _binding: FragmentRadioBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var radioAdapter: RadioAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRadioBinding.inflate(inflater, container, false)
        val root: View = binding.root
        radioAdapter = RadioAdapter { id -> itemClick(id) }
        binding.recyclerview.also { view ->
            view.layoutManager = GridLayoutManager(requireContext(), 2)
            view.adapter = radioAdapter
        }
        binding.recyclerview.itemAnimator = null // notifiitemchanged 화면깜빡임 방지

        MyApplication.radioRepo.radioid.observe(viewLifecycleOwner, Observer {
            radioAdapter.submitList(radioList)
            itempUpdate(it)
//            Log.d("radioidobserve(onserve)","${it}")
//            Log.d("itemClicksd(radioid.observe)", " radio: ${it}")

        })

        fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
            observe(lifecycleOwner, object : Observer<T> {
                override fun onChanged(t: T?) {
                    observer.onChanged(t)
                    removeObserver(this)
                }
            })
        }
        radioViewModel.radios.observeOnce(viewLifecycleOwner) {
            radioAdapter.submitList(it)
            radioList = it
//            Log.d("itemClicksdadio(radios.obse))", " radio: ${it.size},  ")

        }
        // 제일 처음 실행
        CoroutineScope(Dispatchers.Default).launch {
            radioViewModel.loadRadios("")
        }
        return root
    }

    private fun itemClick(id: Int) {
//        MyApplication.radioRepo._radioid.value = id
    }
    private fun itempUpdate(id: Int) { // id = index
        val r = radioList[id]
        if (id != oldRadio?.id) {
            oldRadio?.isSelected = false
            if (oldRadio != null) {
                radioViewModel.updateRadioFalse(oldRadio!!)
                radioAdapter.notifyChanged(oldRadio!!.id)
//                Log.d("itemClicksdadio(iffff))", " radio: ${r.isSelected}: ${r.title}, ${oldRadio?.isSelected}:${oldRadio?.title},  ")
            }

            r.isSelected = true
            radioViewModel.updateRadioTrue(r) // 룸변경시 전체로드됨, postValue만 함
            radioAdapter.notifyChanged(r.id)
//            Log.d("itemClicksdadio(if))", " radio: ${r.isSelected}: ${r.title}, ${oldRadio?.isSelected}:${oldRadio?.title},  ")
        } else {
            r.isSelected = false
            radioViewModel.updateRadioFalse(r)
            radioAdapter.notifyChanged(r.id)
//            Log.d("itemClicksdadio(else)", " radio: ${r.isSelected}: ${r.title}, ${oldRadio?.isSelected}:${oldRadio?.title},  ")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
//        Log.d("screenSet", "onDestroyView()  called")
    }
}

/**
 * Factory for creating a [PlantListViewModel] with a constructor that takes a [PlantRepository].
 */
class RadioViewModelFactory(
    private val repository: RadioRepo
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = RadioViewModel(repository) as T
}
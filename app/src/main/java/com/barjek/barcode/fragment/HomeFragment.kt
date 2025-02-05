package com.barjek.barcode.fragment

import android.graphics.Paint
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barjek.barcode.R
import com.barjek.barcode.adapter.HistoryAdapter
import com.barjek.barcode.api.APIRequest
import com.barjek.barcode.database.DatabaseHelper
import com.barjek.barcode.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = ""

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null

    private lateinit var binding: FragmentHomeBinding
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        db = DatabaseHelper(requireContext())
        binding = FragmentHomeBinding.inflate(layoutInflater)

        binding.shimmerRecyclerView.startShimmer()
        binding.recyclerHistory.layoutManager = LinearLayoutManager(requireContext())
        val sharedPref = requireActivity().getSharedPreferences("UserPref", MODE_PRIVATE)
        val id_user = sharedPref.getString("ID_USER", "")

        lifecycleScope.launch {
            try {
                val req = APIRequest("absensi/$id_user").execute()
                withContext(Dispatchers.Main) {
                    if (req.code in 200 until 300) {
                        binding.recyclerHistory.visibility = View.VISIBLE
                        binding.shimmerRecyclerView.stopShimmer()
                        binding.shimmerRecyclerView.visibility = View.GONE

                        val data = JSONArray(req.data)
                        binding.recyclerHistory.adapter = HistoryAdapter(data)
                    }
                }
            } catch (_: Exception) {}
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}
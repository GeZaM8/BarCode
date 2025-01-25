package com.barjek.barcode.fragment

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.lifecycle.lifecycleScope
import com.barjek.barcode.R
import com.barjek.barcode.activity.EditProfileActivity
import com.barjek.barcode.activity.LoginActivity
import com.barjek.barcode.api.APIRequest
import com.barjek.barcode.database.DatabaseHelper
import com.barjek.barcode.databinding.FragmentProfileBinding
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentProfileBinding

    private lateinit var userPref: SharedPreferences
    private var id_user: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        userPref = requireActivity().getSharedPreferences("UserPref", MODE_PRIVATE)
        id_user = userPref.getString("ID_USER", "0")

        loadData()

        binding.detailProfile.setOnClickListener {
            val intent = Intent(requireActivity(), EditProfileActivity::class.java)
            requireActivity().startActivity(intent)
        }

//        binding.btnLogout.setOnClickListener {
//            sharedPref.edit().clear().apply()
//            startActivity(Intent(view.context, LoginActivity::class.java))
//            finishAffinity(view.context as Activity)
//        }

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun loadData() {
        lifecycleScope.launch {

            withContext(Dispatchers.IO) {
                val req = APIRequest("siswa/$id_user", "GET").execute()

                withContext(Dispatchers.Main) {
                    if (req.code in 200 until 300) {
                        val siswa = JSONObject(req.data)

                        binding.apply {
                            tvName.text = siswa.getString("nama")
                            tvAbsen.text = siswa.getString("no_absen")
                            tvClass.text = siswa.getString("kelas")
                            tvNISN.text = siswa.getString("nama_jurusan")
                            tvNIS.text = siswa.getString("nis")
                        }
                        val foto = siswa.getString("foto")
                        Picasso.get()
                            .load(foto)
                            .placeholder(R.drawable.baseline_person_24)
                            .error(R.drawable.baseline_person_24)
                            .into(binding.ivProfile)
                    } else {
                        userPref.edit().clear().apply()
                        Log.d("PROFILE", req.data)
                        startActivity(Intent(requireActivity(), LoginActivity::class.java))
                        requireActivity().finish()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
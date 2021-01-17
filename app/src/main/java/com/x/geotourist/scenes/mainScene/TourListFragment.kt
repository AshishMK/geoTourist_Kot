package com.x.geotourist.scenes.mainScene

import android.Manifest
import android.app.Activity
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.x.geotourist.R
import com.x.geotourist.data.local.dao.TourDao
import com.x.geotourist.data.local.entity.TourDataEntity
import com.x.geotourist.services.BackgroundLocationService
import com.x.geotourist.utils.Utils
import com.x.geotourist.application.AppController
import com.x.geotourist.scenes.mainScene.adapter.TourListAdapter
import dagger.android.support.DaggerFragment
import java.util.*
import javax.inject.Inject


class TourListFragment : DaggerFragment(), TourListAdapter.ItemListener {
    @Inject
    lateinit var contentDao: TourDao
    lateinit var tourTitle: EditText
    private lateinit var sheetBehavior: BottomSheetBehavior<*>
    var root: View? = null
    lateinit var tourListListener: TourListListener
    lateinit var list: RecyclerView
    lateinit var adapter: TourListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_tour_list, container, false)
        tourTitle = root!!.findViewById<EditText>(R.id.edit)
        // Inflate the layout for this fragment
        sheetBehavior = BottomSheetBehavior.from<View>(root!!.findViewById(R.id.bottomSheet))
        sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let {
            tourListListener = it as TourListListener
        }
        setListeners()
        setRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        checkPerMission(false)
    }

    private fun setListeners() {
        root!!.findViewById<View>(R.id.fab).setOnClickListener {
            checkPerMission(true)

        }
        root!!.findViewById<View>(R.id.create).setOnClickListener {
            if (TextUtils.isEmpty(tourTitle.text)) {
                Toast.makeText(activity, R.string.valid_tour_name, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val tourEntity = TourDataEntity()
            tourEntity.createdAt = Date().time as java.lang.Long
            tourEntity.title = tourTitle.text!!.toString()

            adapter.addItem(contentDao.getTour(contentDao.insertContents(tourEntity)))
            sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            root!!.findViewById<View>(R.id.no_content).visibility =
                if (adapter.itemCount == 0) View.VISIBLE else View.GONE
            tourTitle.text = null
            Handler().postDelayed(Runnable {
                val imm: InputMethodManager =
                    activity!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

                imm.hideSoftInputFromWindow(tourTitle.windowToken, 0)
            }, 500)

        }
        root!!.findViewById<View>(R.id.cancel).setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        }
    }

    private fun setRecyclerView() {
        list = root!!.findViewById(R.id.list)
        list.layoutManager = LinearLayoutManager(activity)
        list.addItemDecoration(DividerItemDecoration(activity, RecyclerView.VERTICAL))
        adapter = TourListAdapter(activity!!, this)
        list.adapter = adapter
        adapter.setItems(contentDao.getTours() as ArrayList<TourDataEntity>)
        root!!.findViewById<View>(R.id.no_content).visibility =
            if (adapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    private fun checkPerMission(openBottomSheet: Boolean) {
        val PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val status: Utils.PermissionStatus = Utils.checkPermissions(
            requireActivity(),
            Utils.PERMISSION_REQUEST,
            PERMISSIONS,
            this, R.string.storage_permission_msg
        )
        if (status === Utils.PermissionStatus.SUCCESS) {
            if (openBottomSheet) {
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                startLocationService()
            }
        }
    }

    private fun startLocationService() {
        val service =
            activity!!.getSystemService(LOCATION_SERVICE) as LocationManager?
        val enabled = service!!.isProviderEnabled(LocationManager.GPS_PROVIDER)

// check if enabled and if not send user to the GSP settings
// Better solution would be to display a dialog and suggesting to
// go to the settings

        if (!enabled) {
            val intent = Intent(ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            ContextCompat.startForegroundService(
                AppController.getInstance(),
                Intent(AppController.getInstance(), BackgroundLocationService::class.java)
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size >= 2) {
            startLocationService()
            // sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onItemClickListener(entity: TourDataEntity, position: Int) {
        tourListListener.onTourClicked(entity)
    }

    override fun onItemDeleteClickListener(entity: TourDataEntity, position: Int) {

    }


}
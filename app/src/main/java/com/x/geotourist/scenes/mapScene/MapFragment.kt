package com.x.geotourist.scenes.mapScene

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PointF
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.here.android.mpa.common.*
import com.here.android.mpa.mapping.*
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapGesture.OnGestureListener.OnGestureListenerAdapter
import com.x.geotourist.R
import com.x.geotourist.data.local.dao.TourDao
import com.x.geotourist.data.local.entity.MarkerEntity
import com.x.geotourist.scenes.playerScene.PlayerActivity
import com.x.geotourist.utils.Utils
import com.x.geotourist.application.AppController
import com.x.geotourist.data.local.entity.TourDataEntity
import com.x.geotourist.scenes.mapScene.adapter.OrderListAdapter
import dagger.android.support.DaggerFragment
import timber.log.Timber
import java.io.File
import java.util.ArrayList
import javax.inject.Inject


class MapFragment : DaggerFragment(), OrderListAdapter.ItemListener {

    @Inject
    lateinit var contentDao: TourDao

    // map embedded in the map fragment
    private var map: com.here.android.mpa.mapping.Map? = null

    // map fragment embedded in this activity
    private var mapFragment: AndroidXMapFragment? = null
    private lateinit var sheetBehavior: BottomSheetBehavior<*>
    var root: View? = null
    lateinit var markerTitle: EditText
    var tourId: Long = 0
    var videoFile: String? = null
    lateinit var colorTextView: TextView
    lateinit var popupMenu: androidx.appcompat.widget.PopupMenu
    lateinit var colorNames: Array<String>
    var selectedGeoCoordinate: GeoCoordinate? = null
    var updateMarkerEntity: MarkerEntity? = null
    var mapMarkerUpdate: MapMarker? = null
    var gestureListenerAdded = false
    var lastLocation: android.location.Location? = null
    private var selectedColorIndex = -1
    lateinit var list: RecyclerView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var adapter: OrderListAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_map, container, false)
        // Inflate the layout for this fragment
        markerTitle = root!!.findViewById<EditText>(R.id.edit)
        colorTextView = root!!.findViewById<TextView>(R.id.selectColor)
        sheetBehavior = BottomSheetBehavior.from<View>(root!!.findViewById(R.id.bottomSheet))
        sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        list = root!!.findViewById(R.id.list)

        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        list.layoutManager = LinearLayoutManager(activity)
        list.addItemDecoration(DividerItemDecoration(activity, RecyclerView.VERTICAL))

        adapter = OrderListAdapter(activity!!, this)
        list.adapter = adapter
        if (ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: android.location.Location? ->
                // Got last known location. In some rare situations this can be null.
                lastLocation = location
                if (lastLocation == null) {
                    Toast.makeText(
                        AppController.getInstance(),
                        "Can't get location",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addOnSuccessListener
                }
                map?.let {
                    map!!.setCenter(
                        GeoCoordinate(lastLocation!!.latitude, lastLocation!!.longitude, 0.0),
                        Map.Animation.NONE
                    )
                }
            }

        colorNames = resources.getStringArray(R.array.color_names)
        setUpColorMenu()
        showSnackView()
        initialize()
        setListeners()
    }


    private fun setMarkers() {
        if (mapFragment!!.mapGesture != null && !gestureListenerAdded) {
            gestureListenerAdded = true
            mapFragment!!.mapGesture!!.addOnGestureListener(listener, 0, false)
        }
        val markers = contentDao.getMarkers(tourId)
        map!!.removeAllMapObjects()
        for (marker in markers) {
            addMarker(marker)
        }
        if (markers.isNotEmpty()) {
            //set map focus on first marker
            map!!.setCenter(
                GeoCoordinate(markers[0].lat.toDouble(), markers[0].lng.toDouble(), 0.0),
                Map.Animation.NONE
            )
        } else {
            // Set the map center to the Vancouver region (no animation)
            ///set map to users current location or a default when current location not found
            lastLocation?.let {
                map!!.setCenter(
                    GeoCoordinate(lastLocation!!.latitude, lastLocation!!.longitude, 0.0),
                    Map.Animation.NONE
                )
            } ?: run {
                map!!.setCenter(
                    GeoCoordinate(49.196261, -123.004773, 0.0),
                    Map.Animation.NONE
                )
            }
        }
    }

    public fun setTourIdFromActivity(idTour: Long) {
        tourId = idTour
        map?.let { map ->
            setMarkers()
        }
    }

    private fun setListeners() {
        root!!.findViewById<View>(R.id.cancel).setOnClickListener {
            updateMarkerEntity = null
            sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            resetSheet()
        }

        root!!.findViewById<View>(R.id.recordVideo).setOnClickListener {
            checkPerMission()
        }

        root!!.findViewById<View>(R.id.playVideo).setOnClickListener {
            videoFile?.let {
                startActivity(
                    Intent(
                        activity!!,
                        PlayerActivity::class.java
                    ).putExtra("url", videoFile)
                )
            } ?: run {
                // If video is null.
                Toast.makeText(
                    activity!!,
                    "Please record a video first before playing",
                    Toast.LENGTH_LONG
                ).show()
            }


        }

        colorTextView.setOnClickListener { popupMenu.show() }
        root!!.findViewById<View>(R.id.add).setOnClickListener {
            if (TextUtils.isEmpty(markerTitle.text)) {
                Toast.makeText(activity, R.string.valid_marker_name, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (selectedColorIndex == -1) {
                Toast.makeText(activity, R.string.valid_color_name, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            map?.let { mapOptional ->
                updateMarkerEntity?.let {
                    updateMarker()
                } ?: run {
                    createMarker()
                }
                resetSheet()
                sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

            } ?: run {
                // If map is null.
            }

        }
    }

    private fun resetSheet() {
        selectedColorIndex = -1
        markerTitle.text = null
        videoFile = null
        colorTextView.setText(R.string.select_color)

    }

    private fun updateMarker() {
        updateMarkerEntity!!.title = markerTitle.text!!.toString()
        updateMarkerEntity!!.color = selectedColorIndex as java.lang.Integer
        updateMarkerEntity!!.video = videoFile
        contentDao.updateMarker(updateMarkerEntity!!)
        map!!.removeMapObject(mapMarkerUpdate!!)
        addMarker(updateMarkerEntity!!)
        updateMarkerEntity = null
    }

    private fun createMarker() {
        val marker = MarkerEntity()
        marker.tourId = this.tourId as java.lang.Long
        marker.title = markerTitle.text!!.toString()
        marker.color = selectedColorIndex as java.lang.Integer
        marker.lat = selectedGeoCoordinate!!.latitude as java.lang.Double
        marker.lng = selectedGeoCoordinate!!.longitude as java.lang.Double
        marker.video = videoFile
        marker.markerOrder = (contentDao.getMarkerCount(tourId) + 1) as Integer
        contentDao.insertMarker(marker)

        //var lastMarker = contentDao.getLastMarker(this.tourId)
        //lastMarker.markerOrder = lastMarker.id.toInt() as Integer
        //contentDao.updateMarker(lastMarker)
        addMarker(contentDao.getLastMarker(this.tourId))

    }

    private fun addMarker(markerEntity: MarkerEntity) {
        val image =
            Image()
        image.setBitmap(
            Utils.getBitmapFromVectorDrawable(
                activity!!, R.drawable.ic_baseline_location_on_24,
                markerEntity.color.toInt()
            )
        )
        Timber.v(" markerInsertIndex setMarkers %s", markerEntity.id.toString())
        var marker =
            MapMarker(
                GeoCoordinate(markerEntity.lat.toDouble(), markerEntity.lng.toDouble(), 0.0),
                image
            )
        marker.title = markerEntity.title
        map!!.addMapObject(marker)
        marker.description = markerEntity.id.toString()
    }


    private fun initialize() {

        // Search for the map fragment to finish setup by calling init().
        mapFragment =
            childFragmentManager.findFragmentById(R.id.mapfragment) as AndroidXMapFragment?


        // Set up disk cache path for the map service for this application
        // It is recommended to use a path under your application folder for storing the disk cache
        val success =
            MapSettings.setIsolatedDiskCacheRootPath(
                AppController.getInstance().applicationContext
                    .getExternalFilesDir(null)!!.absolutePath + File.separator.toString() + ".here-maps"
            )
        if (!success) {
            Toast.makeText(
                AppController.getInstance(),
                "Unable to set isolated disk cache path.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            mapFragment!!.init(OnEngineInitListener { error ->
                root!!.findViewById<ContentLoadingProgressBar>(R.id.pb).visibility = View.GONE
                if (error == OnEngineInitListener.Error.NONE) {
                    if (mapFragment == null) {
                        return@OnEngineInitListener
                    }


                    // retrieve a reference of the map from the map fragment
                    map = mapFragment!!.map

                    // Set the zoom level to the average between min and max
                    map!!.zoomLevel = (map!!.maxZoomLevel + map!!.minZoomLevel) / 2

                    setMarkers()

                } else {
                    println("ERROR: Cannot initialize Map Fragment")
                }
            })
        }
    }

    private val listener: MapGesture.OnGestureListener = object : OnGestureListenerAdapter() {
        override fun onMapObjectsSelected(p0: MutableList<ViewObject>): Boolean {

            for (viewObject in p0) {
                if (viewObject.baseType === ViewObject.Type.USER_OBJECT) {
                    val mapObject: MapObject = viewObject as MapObject
                    if (mapObject.type === MapObject.Type.MARKER) {
                        mapMarkerUpdate = mapObject as MapMarker

                        showUpdateMarkerDialog(
                            contentDao.getMarker(
                                mapMarkerUpdate!!.description!!.toLong()
                            )
                        )
                        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                        return true

                    }
                }
            }
            return false
        }

        override fun onTapEvent(p0: PointF): Boolean {
            if (updateMarkerEntity == null) {
                selectedGeoCoordinate = map!!.pixelToGeo(p0)!!
                root!!.findViewById<Button>(R.id.add).setText(R.string.add)
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }

            return false
        }
    }

    private fun showSnackView() {
        Snackbar.make(root!!, R.string.add_marker_info, Snackbar.LENGTH_LONG).show()
    }

    private fun setUpColorMenu() {
        popupMenu = androidx.appcompat.widget.PopupMenu(activity!!, colorTextView)

        for (i in colorNames.indices) {
            popupMenu.menu.add(0, i, 0, colorNames[i])
        }
        popupMenu.setOnMenuItemClickListener { item ->
            selectedColorIndex = item.itemId
            colorTextView.text = colorNames[item.itemId]
            true
        }
    }

    private fun checkPerMission() {
        val PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val status: Utils.PermissionStatus = Utils.checkPermissions(
            requireActivity(),
            Utils.PERMISSION_REQUEST,
            PERMISSIONS,
            this, R.string.storage_permission_msg
        )
        if (status === Utils.PermissionStatus.SUCCESS) {
            videoFile = Utils.pickVideoFromCameraFragment(this@MapFragment)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size == 2) {
            videoFile = Utils.pickVideoFromCameraFragment(this@MapFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            videoFile = null
            return
        }


    }

    private fun showUpdateMarkerDialog(markerEntity: MarkerEntity) {
        updateMarkerEntity = markerEntity
        root!!.findViewById<Button>(R.id.add).setText(R.string.update)
        selectedColorIndex = markerEntity.color.toInt()
        colorTextView.text = colorNames[selectedColorIndex]
        markerTitle.setText(markerEntity.title)
        selectedGeoCoordinate =
            GeoCoordinate(markerEntity.lat.toDouble(), markerEntity.lng.toDouble(), 0.0)
        videoFile = markerEntity.video
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.marker_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.markerOrder) {
            if (list.visibility == View.GONE)
                setRecyclerView()
            else
                list.visibility = View.GONE
            return true;
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setRecyclerView() {


        adapter.setItems(contentDao.getMarkersByOrder(tourId) as ArrayList<MarkerEntity>)
        list.visibility = View.VISIBLE

    }

    override fun onItemClickListener(entity: MarkerEntity, position: Int, view: View) {
        setOrderMenu(view, entity, position)
    }

    override fun onItemDeleteClickListener(entity: MarkerEntity, position: Int) {

    }

    private fun setOrderMenu(view: View, entity: MarkerEntity, position: Int) {
        val popupMenu: androidx.appcompat.widget.PopupMenu =
            androidx.appcompat.widget.PopupMenu(activity!!, view)
        popupMenu.menu.add(
            0,
            -1,
            0,
            "select a index to change ${entity.title}'s order"
        )
        for ((i, item) in adapter.contents.withIndex()) {
            popupMenu.menu.add(
                0,
                i,
                0,
                if (item.markerOrder.toInt() == 0) (i + 1).toString() else item.markerOrder.toString()
            )
        }
        popupMenu.setOnMenuItemClickListener { item ->
            if (item.itemId > -1) {
                adapter.contents.get(position).markerOrder = (item.itemId + 1) as Integer
                contentDao.updateMarker(adapter.contents.get(position))
                if (item.itemId < position) {

                    for (i in item.itemId until position) {
                        if (i == -1) {
                            continue
                        }
                        adapter.contents.get(i).markerOrder = (i + 2) as Integer
                        contentDao.updateMarker(adapter.contents.get(i))
                    }
                }
                if (item.itemId > position) {

                    for (i in item.itemId downTo position + 1) {
                        if (i == -1) {
                            continue
                        }
                        adapter.contents.get(i).markerOrder = (i) as Integer
                        contentDao.updateMarker(adapter.contents.get(i))
                    }
                }
                setRecyclerView()
            }
            true
        }
        popupMenu.show()
    }

    public fun isListShowing(): Boolean {
        if (list.visibility == View.VISIBLE) {
            list.visibility = View.GONE
            return true
        }
        return false
    }


}
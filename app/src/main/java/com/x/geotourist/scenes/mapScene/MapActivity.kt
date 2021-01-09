package com.x.geotourist.scenes.mapScene

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.x.geotourist.R

class MapActivity : AppCompatActivity() {
    companion object {
        const val TOURID = "tourID"
        const val TITLE = "title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        if(resources.getBoolean(R.bool.has_two_panes)){
            finish()
            return
       }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = intent.getStringExtra(MapActivity.TITLE)
        val fragment: MapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as MapFragment
        fragment.setTourIdFromActivity(intent.getLongExtra(MapActivity.TOURID, 0))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
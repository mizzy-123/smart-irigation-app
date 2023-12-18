package com.iot.lilik

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {
    lateinit var mydb: DatabaseReference
    lateinit var hum: TextView
    lateinit var soil: TextView
    lateinit var buttonOnOf: RelativeLayout
    lateinit var statusPump: TextView
    lateinit var lineChartSoil: LineChart
//    lateinit var lineChartSuhu: LineChart
    lateinit var progress_soil: ProgressBar
    lateinit var tv_soil_persen: TextView
    lateinit var tv_suhu: TextView

    var status_pump: Int = 0

    var arraySoil = ArrayList<Entry>()

//    var intervalSoil = 0f
    var angkaSoil = 0
    var angkaHum = "0"

    private fun initComponents(){
        mydb = FirebaseDatabase.getInstance().reference.child("sensorData")
        hum = findViewById(R.id.humidity)
        soil = findViewById(R.id.soil)
        buttonOnOf = findViewById(R.id.buttonOnOf)
        statusPump = findViewById(R.id.statusPump)
        lineChartSoil = findViewById(R.id.lineChartSoil)
//        lineChartSuhu = findViewById(R.id.lineChartSuhu)
        progress_soil = findViewById(R.id.progres_soil)
        tv_soil_persen = findViewById(R.id.tv_soil_persen)
        tv_suhu = findViewById(R.id.tv_suhu)
    }

    private fun dataSoil(key: Float, soil: Float): ArrayList<Entry>{
        arraySoil.add(Entry(key, soil))
//        intervalSoil += 1f
        return arraySoil
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initComponents()

        try {
            mydb.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    arraySoil = ArrayList()
                    val humidiy: DataSnapshot? = snapshot.child("humidity")
//                    hum.setText(humidiy)
                    if (humidiy != null){
                        for (child in humidiy.children){
                            val hum: String = child.value.toString()
                            if (hum.isNotEmpty()){
                                angkaHum = hum
                            }
                        }

                        hum.text = angkaHum
                    }


//                    val soilHum: String = snapshot.child("soilHumidity").value.toString()
//                    soil.setText(soilHum)
                    val soilDataSnapshot: DataSnapshot? = snapshot.child("soilHumidity")
                    if (soilDataSnapshot != null) {
                        for (childSnapshot in soilDataSnapshot.children) {
                            val soilHum: String = childSnapshot.value.toString()
                            val soilHumKey: Float = childSnapshot.key.toString().toFloatOrNull() ?: 0.0f
                            if (soilHum.isNotEmpty() && soilHumKey != null) {
                                soil.text = soilHum
                                angkaSoil = soilHum.toInt()

                                // Tambahkan ke arraySoil
                                arraySoil.add(Entry(soilHumKey, soilHum.toFloat()))
                            }
                        }

                        val linedataset: LineDataSet = LineDataSet(arraySoil, "Soil")
                        val datasetSoil: ArrayList<ILineDataSet> = ArrayList()
                        datasetSoil.add(linedataset)
                        val chartDataSoil: LineData = LineData(datasetSoil)
                        lineChartSoil.data = chartDataSoil
                        lineChartSoil.invalidate()
                        progress_soil.progress = angkaSoil
                        tv_soil_persen.text = "$angkaSoil%"
                    }

                    val statuspump: Int = snapshot.child("relay").value.toString().toInt()
                    status_pump = statuspump
                    if (statuspump == 1){
                        statusPump.setText("ON")
                    }else {
                        statusPump.setText("OFF")
                    }

                    val suhu: String = snapshot.child("temperature").value.toString()
                    tv_suhu.text = "$suhu C"
//                    Log.d("masuk", humidiy)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        } catch (e: Exception){
            Log.e("firebase", e.toString())
        }

        buttonOnOf.setOnClickListener {
            if (status_pump == 1){
                mydb.child("relay").setValue(0)
            }else {
                mydb.child("relay").setValue(1)
            }
            Log.d("button", "button ditekan")
        }
    }
}
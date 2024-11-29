package com.example.compapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the layout from XML
        setContentView(R.layout.activity_main2)

        // List of container IDs
        val containerIds = listOf(
            R.id.dynamicContainer0,
            R.id.dynamicContainer1,
            R.id.dynamicContainer2,
            R.id.dynamicContainer3,
            R.id.dynamicContainer4
        )

        // List of dropdown options for each container
        val dropdownOptions = listOf(
            listOf("Single-ply, clear", "Single-ply, coat", "Double-ply, clear", "Double-ply, coat"),
            listOf("Wood", "Metal", "Acrylic"),
            listOf("12x8", "14x10", "16x14"),
            listOf("Sharp", "Rounded"),
            listOf("Flanged Hoops", "Die-Cast Hoops")
        )

        // Populate each container with a dropdown
        for ((index, containerId) in containerIds.withIndex()) {
            val dynamicContainer = findViewById<LinearLayout>(containerId)

            val textInputLayout = TextInputLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 16, 16, 16) // Add margins
                }
            }

            val autoCompleteTextView = AutoCompleteTextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                hint = "Select an option"
                val adapter = ArrayAdapter(
                    context,
                    android.R.layout.simple_dropdown_item_1line,
                    dropdownOptions[index]
                )
                setAdapter(adapter)
                setTextColor(getColor(R.color.black)) // Black text for visibility
                setHintTextColor(getColor(R.color.gray)) // Gray hint for visibility
                setBackgroundColor(getColor(R.color.white)) // White background for contrast
                dropDownHeight = LinearLayout.LayoutParams.WRAP_CONTENT // Ensure dropdown is fully visible
            }

            textInputLayout.addView(autoCompleteTextView)
            dynamicContainer.addView(textInputLayout)
        }
    }
}

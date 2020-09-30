package com.example.pda

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.example.pda.R
import com.example.pda.WelcomeActivity
import com.example.pda.adapters.ViewPagerAdapter
import kotlinx.android.synthetic.main.activity_on_board.*


@Suppress("DEPRECATION")
class OnBoardActivity : AppCompatActivity() {
    private lateinit var dots: Array<TextView?>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        )

        setContentView(R.layout.activity_on_board)
        init()
    }

    private fun init() {


        val adapter = ViewPagerAdapter(this)
        addDots(0)
        view_pager.addOnPageChangeListener(listener) // create this listener
        view_pager.adapter = adapter
        btnRight.setOnClickListener(View.OnClickListener {
            //if button text is next we will go to next page of viewpager
            if (btnRight.text.toString() == "Next") {
                view_pager.currentItem = view_pager.currentItem + 1
            } else {
                //else its finish we will start Auth activity
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        })
        btnLeft.setOnClickListener(View.OnClickListener {
            // if btn skip clicked then we go to page 3
            view_pager.currentItem = view_pager.currentItem + 2
        })
    }

    //method to create dots from html code
    private fun addDots(position: Int) {
        dotsLayout!!.removeAllViews()
        dots = arrayOfNulls(3)
        for (i in dots.indices) {
            dots[i] = TextView(this)
            //this code creates dot
            dots[i]!!.text = Html.fromHtml("&#8226")
            dots[i]!!.textSize = 35f
            dots[i]!!.setTextColor(resources.getColor(R.color.colorLightGrey))
            dotsLayout!!.addView(dots[i])
        }

        // ok now lets change the selected dot color
        if (dots.isNotEmpty()) {
            dots[position]!!.setTextColor(resources.getColor(R.color.colorGrey))
        }
    }

    private val listener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            addDots(position)
            //ok now we need to change the text of Next button to Finish if we reached page 3
            //and hide Skip button if we are not in page 1
            if (position == 0) {
                btnLeft!!.visibility = View.VISIBLE
                btnLeft!!.isEnabled = true
                btnRight!!.text = "Next"
            } else if (position == 1) {
                btnLeft!!.visibility = View.GONE
                btnLeft!!.isEnabled = false
                btnRight!!.text = "Next"
            } else {
                btnLeft!!.visibility = View.GONE
                btnLeft!!.isEnabled = false
                btnRight!!.text = "Finish"
            }
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }
}
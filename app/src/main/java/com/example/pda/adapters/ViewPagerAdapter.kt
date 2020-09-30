package com.example.pda.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.example.pda.R

class ViewPagerAdapter(private val context: Context) : PagerAdapter() {
    private var inflater: LayoutInflater? = null
    private val images = intArrayOf(
        R.drawable.carrent,
        R.drawable.driver,
        R.drawable.map,
    )

    private val titles = arrayOf(
        "What We Do",
        "Our Drivers",
        "Chauffeurs" )
    private val descs = arrayOf(
        "We at PDA spend heavily in driver protection programs and professional education.With our experienced, history and policing drivers, we want to make the driving easier for you.",
        "The driver is fitted with PPE and is expected to perform a complete procedural and precautionary examination.",
        "Our skilled chauffeurs are uniformly equipped to meet the highest quality level."
        )

    override fun getCount(): Int {
        return titles.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v: View = inflater.inflate(R.layout.view_pager, container, false)

        //init views
        val imageView = v.findViewById<ImageView>(R.id.imgViewPager)
        val txtTitle = v.findViewById<TextView>(R.id.txtTitleViewPager)
        val txtDesc = v.findViewById<TextView>(R.id.txtDescViewPager)
        imageView.setImageResource(images[position])
        txtTitle.text = titles[position].toString()
        txtDesc.text = descs[position].toString()
        container.addView(v)
        return v
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}
package com.example.drawkanji.ui

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.drawkanji.R

class KanjiStrokesAdapter : RecyclerView.Adapter<KanjiStrokesAdapter.ViewHolder>() {

    private var kanjiStrokesBitmaps: List<Bitmap> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_kanji_stroke, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = kanjiStrokesBitmaps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.image.context)
            .asBitmap()
            .load(kanjiStrokesBitmaps[position])
            .into(holder.image)
    }

    fun setKanjiStrokesBitmaps(kanjiStrokesBitmaps: List<Bitmap>) {
        this.kanjiStrokesBitmaps = kanjiStrokesBitmaps
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.kanjiStrokeImage)
    }
}
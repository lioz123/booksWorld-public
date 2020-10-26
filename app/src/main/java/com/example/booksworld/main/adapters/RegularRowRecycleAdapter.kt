package com.example.booksworld.main.adapters

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.example.booksworld.R
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

class RegularRowRecycleAdapter (var c:Context ,var splist:ArrayList<SeriesProperties>,var recyclerView: RecyclerView,var itemClickListener:View.OnClickListener): RecyclerView.Adapter<RegularRowRecycleAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v){
        lateinit var title : TextView
        lateinit var imageView: ImageView
        fun bindView(sp : SeriesProperties) {
            title.text = sp.name
            itemView.setTag(this)
            itemView.setOnClickListener(itemClickListener)
            if (sp.imageloaded) {
                var bitmap = sp.bitmap
                imageView.setImageBitmap(bitmap)
            } else {
                Picasso.get().load(sp.imgSRC).placeholder(R.mipmap.book_content_image_foreground)
                    .into(this.imageView, object :
                        Callback {
                        override fun onSuccess() {
                            sp.bitmap = (imageView.drawable as BitmapDrawable).toBitmap()
                            sp.imageloaded = true
                        }

                        override fun onError(e: Exception?) {
                            e?.printStackTrace()
                        }

                    })
            }
        }
        init {
            title = itemView.findViewById(R.id.book_item_title_textView)
            imageView = itemView.findViewById(R.id.book_item_imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        var inflater = c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var v = inflater.inflate( R.layout.book_item_layout,parent,false)
        var vh = ItemViewHolder(v)
        return vh
    }

    override fun getItemCount(): Int {
        return splist.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindView(splist[position])
    }
}
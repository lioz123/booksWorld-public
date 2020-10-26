package com.example.booksworld.main.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booksworld.R
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import com.example.booksworld.main.PropertiesObjects.SeriesRawProperties
import com.example.booksworld.main.activities.ActivityBooksItmesPresentation
import com.example.booksworld.main.activities.Explore_Activity
import com.example.booksworld.main.activities.ReciveSearhResults
import com.google.android.material.button.MaterialButton

class Explore_RecycleViewAdapter(var srlist:ArrayList<SeriesRawProperties>) : RecyclerView.Adapter<Explore_RecycleViewAdapter.RowViewHolder>() {
        companion object{
            val ACTION_SPECIFIC_SP ="SPECIFIC_SP"
        }


     inner class RowViewHolder(v: View) : RecyclerView.ViewHolder(v){
        lateinit var categoryTextView :TextView
        lateinit var recyclerRaw:RecyclerView
         lateinit var button: MaterialButton
         var onClickLister: View.OnClickListener

         init{
             onClickLister = View.OnClickListener {v->
                 var vt = v.tag
                 when(vt){
                     is RecyclerView.ViewHolder ->{

                         var position= vt.adapterPosition
                         var intent = Intent(v.context,ReciveSearhResults::class.java)
                         intent.action=ACTION_SPECIFIC_SP

                         intent.putExtra(SeriesProperties.SERIES_PROPERTIES_KEY,(recyclerRaw.adapter as BooksRawRecyclerAdapter).splist[position])
                         v.context.startActivity(intent)
                     }
                     is String ->{

                         var intent = Intent(v.context, ActivityBooksItmesPresentation::class.java)
                         intent.putExtra( ActivityBooksItmesPresentation.ACTIVITY_TO_OPEN,ReciveSearhResults::class.qualifiedName)
                         intent.putExtra(ActivityBooksItmesPresentation.CATEGORY_PRESENTATION_KEY,vt)
                         v.context.startActivity(intent)
                     }
                 }


             }
         }
         var initialziedRecycleView = false
        fun bindView(sr :SeriesRawProperties){
            categoryTextView.setText(sr.category)
            button.tag= sr.category
            button.setOnClickListener(onClickLister)
            if(!initialziedRecycleView){
                initialziedRecycleView=true
                Explore_Activity.InitializeRecyclerViewHoriznotal(itemView.context,recyclerRaw,BooksRawRecyclerAdapter(sr.splist,onClickLister) as RecyclerView.Adapter<RecyclerView.ViewHolder?>)

        }
}

init{
    categoryTextView = itemView.findViewById(R.id.category_title)
    recyclerRaw = itemView.findViewById(R.id.category_raw_recycleview)
    button = itemView.findViewById(R.id.view_more_category_raw_but)

}
}

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
    var v = LayoutInflater.from(parent.context).inflate( R.layout.category_row,parent,false)
    var vh = RowViewHolder(v)
    return vh
}

override fun getItemCount(): Int {
    return srlist.size
}

override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
    holder.bindView(srlist[position])
}
}
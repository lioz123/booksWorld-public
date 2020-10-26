package com.example.booksworld.main.adapters

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.booksworld.R
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import com.example.booksworld.main.PropertiesObjects.SeriesRawProperties
import com.example.booksworld.main.activities.Explore_Activity
import com.example.booksworld.main.adapters.Explore_RecycleViewAdapter.Companion.ACTION_SPECIFIC_SP
import android.util.Pair as UtilPair
class Category_adapter(var activity: Activity, var srlist:ArrayList<SeriesRawProperties>, var activityToOpen :String):RecyclerView.Adapter<Category_adapter.RowHolder>() {


            inner class RowHolder(v: View) : RecyclerView.ViewHolder(v){
                lateinit var recyclerRaw:RecyclerView
                var onClickLister = View.OnClickListener {v->
                    println("on click is called")
                    var vh = v.tag as RegularRowRecycleAdapter.ItemViewHolder

                    var position= vh.adapterPosition
            var sp =( recyclerRaw.adapter as RegularRowRecycleAdapter).splist[position]
                    println("RowHolder: has image loaded:${sp.imageloaded}")
            openBook(sp,  v.context,UtilPair.create(vh.imageView as View, v.context.getString(R.string.shared_image)))


        }
        fun openBook(sp:SeriesProperties,c:Context,vararg pairs: UtilPair<View,String>){
            println("sp:${sp.name}")
            var intent = Intent(c, Class.forName(activityToOpen))

         //   sp.imageloaded=false // if true app will crash because it is unable to parse the bit map ):
            intent.putExtra(SeriesProperties.SERIES_PROPERTIES_KEY,sp)
            intent.action=ACTION_SPECIFIC_SP

             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                 var options= ActivityOptions.makeSceneTransitionAnimation(activity, *pairs)

                c.startActivity(intent,options.toBundle())
                //    c.startActivity(intent)
            } else {
                c.startActivity(intent)
            }

        }
        var initialziedRecycleView = false
        fun bindView(sr :SeriesRawProperties){
            if(!initialziedRecycleView){
                initialziedRecycleView=true

               Explore_Activity.InitializeRecyclerViewHoriznotal(itemView.context,recyclerRaw,RegularRowRecycleAdapter(itemView.context,sr.splist,recyclerRaw,onClickLister) as RecyclerView.Adapter<RecyclerView.ViewHolder?>)

            }
        }

        init{
            recyclerRaw = itemView.findViewById(R.id.regular_raw_recycle_view)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowHolder {
        var v = LayoutInflater.from(parent.context).inflate( R.layout.regular_books_raw,parent,false)
        var vh = RowHolder(v)
        return vh
    }

    override fun getItemCount(): Int {
        return srlist.size
    }

    override fun onBindViewHolder(holder: RowHolder, position: Int) {
        holder.bindView(srlist[position])
    }


}
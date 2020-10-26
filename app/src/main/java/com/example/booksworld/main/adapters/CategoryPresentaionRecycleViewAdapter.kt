package com.example.booksworld.main.adapters

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.example.booksworld.R
import com.example.booksworld.main.DownloadRecivers.CategoryPresentationRecycleViewDownloadReciver
import com.example.booksworld.main.CustomUtils.FileHelper
import com.example.booksworld.main.Firebase.FirebaseAdapter
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import com.example.booksworld.main.adapters.Explore_RecycleViewAdapter.Companion.ACTION_SPECIFIC_SP
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.lang.IllegalArgumentException
import android.os.Handler

class CategoryPresentaionRecycleViewAdapter(
    var splist: ArrayList<SeriesProperties>,
    activityToOpen: String,
    val context: Context
) : RecyclerView.Adapter<CategoryPresentaionRecycleViewAdapter.ItemViewHolder>(){
lateinit var onClick :View.OnClickListener
val TAG = "CategoryPresentation.class"
    lateinit var catgerotyPresentationDownloadReciver : CategoryPresentationRecycleViewDownloadReciver
    init {
        initiateReciver(context)
        InitiatteOnClick(activityToOpen)
}
    fun initiateReciver(c:Context){
        catgerotyPresentationDownloadReciver = CategoryPresentationRecycleViewDownloadReciver(c,this, Handler(c.mainLooper))
    }

    private fun InitiatteOnClick(activityToOpen: String) {
        onClick = object : View.OnClickListener {
            override fun onClick(v: View?) {
                var vh = v?.tag as ItemViewHolder
                var position = vh.adapterPosition
                var sp = splist[position]
                if (sp.deleteMode == true) {
                    delete(sp, position, v)

                } else if (sp.didNotDownload) {
                    restoreBook(v, sp, 0)


                } else {
                    openBook(v, sp)
                }

            }


            private fun delete(
                sp: SeriesProperties,
                position: Int,
                v: View
            ) {
                DeleteFiles(position, v, sp)
                val adapter = FirebaseAdapter(v.context)
                adapter.removeBookFromDidNotDownloadList(sp)
                adapter.removeBookFromUnDownloadedList(sp)
            }

            private fun openBook(
                v: View,
                sp: SeriesProperties
            ) {
                var intent = Intent(v.context, Class.forName(activityToOpen))
                println("palying for keeps:${sp.getString()}")

                intent.putExtra(SeriesProperties.SERIES_PROPERTIES_KEY, sp)

                intent.action = ACTION_SPECIFIC_SP
                println("palying for keeps:${sp.getString()}")

                v.context.startActivity(intent)
            }
        }
    }

    fun downLoadBook(
        context: Context,
        sp: SeriesProperties,
        catgerotyPresentationDownloadReciver: CategoryPresentationRecycleViewDownloadReciver
    ){
        val firebaseAdapter: FirebaseAdapter = FirebaseAdapter(context)
        firebaseAdapter.downloadMyBook(sp,catgerotyPresentationDownloadReciver)
    }
    private fun View.OnClickListener.restoreBook(
        v: View,
        sp: SeriesProperties,
        index: Int
    ) {
        if(index>0){
            return
        }
        val firebaseAdapter = FirebaseAdapter(v.context)
        //firebaseAdapter.removeBookFromDidNotDownloadList(sp)
        firebaseAdapter.getUserBook(sp).apply {
            if (this == null) {
              //  var bookref = firebaseAdapter.getBook(sp.uid)
            }
        }?.addOnSuccessListener {
            downLoadBook(v.context, SeriesProperties.BuildFromSnapshot(it),catgerotyPresentationDownloadReciver)
        }?.addOnFailureListener {
            Toast.makeText(v.context, "Unable to get book", Toast.LENGTH_LONG).show()
            //  Log.e(TAG,"there is an error with getting the document")

            it.printStackTrace()
        }
    }

    private fun DeleteFiles(
        position: Int,
        v: View,
        sp: SeriesProperties
    ) {
        splist.removeAt(position)
        notifyItemRemoved(position)
        FileHelper.delete(v.context, sp)
        val firebaseAdapter = FirebaseAdapter(v.context)
        firebaseAdapter.remove(sp, splist)
    }

    inner class ItemViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
    var title:TextView
    var image:ImageView
    var deleteButton:ImageButton
    init{
        image= itemView.findViewById(R.id.book_item_imageView)
        title = itemView.findViewById(R.id.book_item_title_textView)
        deleteButton = itemView.findViewById(R.id.delete_image_button)
        itemView.setOnClickListener(onClick)

        deleteButton.setOnClickListener(onClick)
    }
    fun onBind(
        position: Int,
        categoryPresentaionRecycleViewAdapter: CategoryPresentaionRecycleViewAdapter
    ){
        itemView.tag=this
        deleteButton.tag=this
        var sp = splist[position]
        title.text=sp.name
        println("position:$position name:${sp.name}")
        if(sp.deleteMode){
                deleteButton.setImageResource(R.drawable.ic_baseline_delete_24_white)
                deleteButton.visibility=View.VISIBLE
        }else if(sp.didNotDownload){
            deleteButton.setImageResource(R.drawable.ic_baseline_arrow_downward_24)
          deleteButton.visibility=View.VISIBLE
        } else{
            deleteButton.visibility=View.GONE
        }

        if(sp.imageloaded){
            image.setImageBitmap(sp.bitmap)
        }else{
   //         sp.changeImage(image)
            LoadImage(sp)

        }

    }

    private fun LoadImage(sp: SeriesProperties) {
        try{
            image.setImageBitmap(null)
            Picasso.get().load(sp.imgSRC).into(this.image, object : Callback {
                override fun onSuccess() {
                    sp.bitmap = (image.drawable as BitmapDrawable).toBitmap()
                    sp.imageloaded = true
                }

                override fun onError(e: Exception?) {
                    e?.printStackTrace()
                }
            })
        }catch (e:IllegalArgumentException){
            e.printStackTrace()
        }

    }
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        var v = LayoutInflater.from(parent.context).inflate(R.layout.book_item_layout,parent,false)
        return ItemViewHolder(v)
    }

    override fun getItemCount(): Int {
          return  splist.size
        }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.onBind(position,this)
    }



    fun setEditMode(b:Boolean){
        splist.forEach {
            it.deleteMode=b
        }
        notifyDataSetChanged()
    }

    fun syncData(c:Context,syncButton:Button,progressBar:ProgressBar) {
        println("sync data was called")
        syncButton.visibility=View.GONE
        progressBar.visibility = View.VISIBLE
        val firebaseAdapter = FirebaseAdapter(c)
        splist.forEach {
            firebaseAdapter.addUserBook(it)
        }
        firebaseAdapter.updateUserBookList(splist)
        println("sync data was completed")

        syncButton.visibility=View.VISIBLE
        progressBar.visibility = View.GONE
        Toast.makeText(c,"finished sync", Toast.LENGTH_LONG).show()
    }

    fun update(uid: Int?) {
        splist.forEachIndexed {index,sp->
            if(sp.uid==uid){
                sp.didNotDownload=false
                notifyItemChanged(index)
            }
        }
    }


}
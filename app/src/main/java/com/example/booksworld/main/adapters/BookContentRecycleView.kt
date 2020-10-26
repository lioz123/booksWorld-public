package com.example.booksworld.main.adapters

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.example.booksworld.R
import com.example.booksworld.main.DownloadRecivers.BookContentDownloadReciver
import com.example.booksworld.main.Firebase.FirebaseAdapter
import com.example.booksworld.main.PropertiesObjects.SeriesProperties
import com.example.booksworld.main.Services.DownloadService.Companion.startDownloadSeries
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

class BookContentRecycleView(var splist :ArrayList<SeriesProperties> ): RecyclerView.Adapter<BookContentRecycleView.BookContentViewHolder>() {

    fun test(){
    }


class BookContentViewHolder(itemView: View,var c:Context,var recyclerAdapter:BookContentRecycleView) :RecyclerView.ViewHolder(itemView){
    var nameTextView:MaterialTextView
    var authorTextView:MaterialTextView
    var contentTextView:MaterialTextView
    var img: ImageView
    var downloadButton :MaterialButton
    val mFirebaseAdapter:FirebaseAdapter = FirebaseAdapter(c)


    lateinit var sp:SeriesProperties
    var tagsTextView:MaterialTextView = itemView.findViewById(R.id.book_content_tags_textView)
    var spannableString:SpannableString
    private var relievedFullResult=false
    val  contentDownloadReciver:BookContentDownloadReciver = BookContentDownloadReciver(c, Handler(c.mainLooper))
    fun bindView(sp:SeriesProperties,position: Int){

            println("position:${position} uid:${sp.uid}")
        this.sp=sp
            nameTextView.text= sp.name
            authorTextView.text= c.resources.getString(R.string.author) +sp.author
        spannableString= sp.spannable
        var clickableSpan = CustomClickableSpan(recyclerAdapter,position,contentTextView,sp)
        contentTextView.text=  sp.presentationDescription
        if(sp.imageloaded){
            img.setImageBitmap(sp.bitmap)
        }else{
            println("sp img src:${sp.imgSRC}")
            Picasso.get().load(sp.imgSRC).placeholder(R.mipmap.book_content_image_foreground)
                .into(this.img, object :
                    Callback {
                    override fun onSuccess() {
                        sp.bitmap = (img.drawable as BitmapDrawable).toBitmap()
                        sp.imageloaded = true
                    }

                    override fun onError(e: Exception?) {
                        println("back on loading image piccasso")
                        e?.printStackTrace()
                    }

                })
        }
        ShowSpan(sp, clickableSpan)

            tagsTextView.text=c.resources.getString(R.string.tags)+sp.tags
            if(!sp.hasUpdateData){
                UpdateSeries(sp,position)

            }else{
                downloadButton.visibility= View.VISIBLE
                downloadButton.setOnClickListener{
                    Toast.makeText(c,c.getString(R.string.start_downloading),Toast.LENGTH_LONG).show()
                    println("sp before downloading"+ sp.getString())
                    startDownloadSeries(c, sp,contentDownloadReciver)
                }


            }



    }

    private fun UpdateSeries(sp: SeriesProperties,position: Int) {
            mFirebaseAdapter.getBook(sp.uid).addSnapshotListener { snapshot, error ->
                if(error!=null){
                    println("there is an error in recieving books content")
                    error.printStackTrace()
                    return@addSnapshotListener
                }
                relievedFullResult = true
                sp.update(snapshot)

                recyclerAdapter.notifyItemChanged(position)
            }
    }

    private fun ShowSpan(
        sp: SeriesProperties,
        clickableSpan: CustomClickableSpan
    ) {
        if (sp.showSpannable) {
            spannableString.setSpan(
                clickableSpan,
                0,
                spannableString.length,
                Spanned.SPAN_MARK_MARK
            )
            contentTextView.append(spannableString)
            contentTextView.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun LoadImage(sp: SeriesProperties) {

    }


    inner class CustomClickableSpan(var booksRawRecyclerAdapter: BookContentRecycleView, var position:Int,var textView: MaterialTextView,var sp:SeriesProperties):ClickableSpan(){
        override fun onClick(widget: View) {
            println("spann clicked ${spannableString.toString()}")

            if(sp.presentationDescription.length>504){
                println("move to show more")
                sp.spannable=SpannableString(c.getString(R.string.show_more))
                sp.presentationDescription = sp.description.toString().substring(0,500) + "... "
            }else{
                println("move to show less")
                sp.spannable=SpannableString(c.getString(R.string.show_less))

                sp.presentationDescription=sp.description + System.getProperty("line.separator")

            }

            booksRawRecyclerAdapter.notifyItemChanged(position)
            booksRawRecyclerAdapter

        }

    }

    init{
        nameTextView= itemView.findViewById(R.id.book_content_name_textView)
        authorTextView= itemView.findViewById(R.id.book_content_author_textView)
        contentTextView= itemView.findViewById(R.id.book_content_description_content_textView)
        img= itemView.findViewById(R.id.book_content_book_image)
        spannableString= SpannableString(c.resources.getString(R.string.show_more))
        downloadButton = itemView.findViewById(R.id.nook_content_download_button)

    }
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookContentViewHolder {

        var v = LayoutInflater.from(parent.context).inflate(R.layout.book_content_layout,parent,false)
        var holder=BookContentViewHolder(v,parent.context,this)
        return holder
    }

    override fun getItemCount(): Int {
        return splist.size
    }

    override fun onBindViewHolder(holder: BookContentViewHolder, position: Int) {
        println("bind view holder: position${position}")
        holder.bindView(splist[position],position)
    }
}
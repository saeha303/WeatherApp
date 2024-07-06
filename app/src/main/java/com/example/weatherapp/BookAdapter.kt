package com.example.weatherapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import java.net.URL

class BookAdapter(private val context: Context, private val items: MutableList<Item>) :
    BaseAdapter() {
    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(p0: Int): Any {
        return items[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val item = items[position]
        println("item:")
        println(items)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.book_details, parent, false)

        val title: TextView = view.findViewById(R.id.title)
        val author:TextView = view.findViewById(R.id.author)
        val description: TextView = view.findViewById(R.id.description)
        val photo: ImageView = view.findViewById(R.id.photo)
        title.text = item.title
        author.text= item.authors
        description.text = item.description
        if (item.photo == "") {
            photo.setImageResource(R.drawable.placeholder)
        } else {
            DownloadImageTask(photo)
                .execute(item.photo)
        }
        return view
    }
    private class DownloadImageTask(bmImage: ImageView) :
        AsyncTask<String?, Void?, Bitmap?>() {
        val bmImage: ImageView

        init {
            println("here 6")
            this.bmImage = bmImage
            //            goto here 7
        }

        override fun doInBackground(vararg urls: String?): Bitmap? {
            println("here 7")
            val urldisplay = urls[0]
            var mIcon11: Bitmap? = null
            try {
                val `in` = URL(urldisplay).openStream()
                mIcon11 = BitmapFactory.decodeStream(`in`)
            } catch (e: Exception) {
                Log.e("Error", e.message)
                e.printStackTrace()
            }
            return mIcon11
        }

        override fun onPostExecute(result: Bitmap?) {
            println("here 8")
            bmImage.setImageBitmap(result)
        }

//        override fun doInBackground(vararg p0: String?): Bitmap? {
//            TODO("Not yet implemented")
//        }
    }
}

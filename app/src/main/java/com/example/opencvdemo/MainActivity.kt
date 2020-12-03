package com.example.opencvdemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.Core.BORDER_DEFAULT
import org.opencv.features2d.DescriptorExtractor
import org.opencv.features2d.DescriptorMatcher
import org.opencv.features2d.FeatureDetector
import org.opencv.imgproc.Imgproc


class MainActivity : AppCompatActivity() {


    companion object {
        init {
            System.loadLibrary("opencv_java3")
        }
    }


    private lateinit var demoImage:ImageView
    var RED = Scalar(255.0, 0.0, 0.0)
    var GREEN = Scalar(0.0, 255.0, 0.0)
    var detector: FeatureDetector? = null
    var descriptor: DescriptorExtractor? = null
    var matcher: DescriptorMatcher? = null
    var descriptors2: Mat? = null
    var descriptors1:Mat? = null
    var img1: Mat? = null
    var keypoints1: MatOfKeyPoint? = null
    var keypoints2:MatOfKeyPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        demoImage = findViewById(R.id.demoGalleryImage)

        getPermission()


    }

    private fun getPermission() {
        if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
        ) {
            requestPermissions(
                    arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    ), 1
            )
        }
        else
        {
           getAllGalleryImage()
        }
    }

    private fun getAllGalleryImage() {
        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, 2)

    }


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String?>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getAllGalleryImage()


        } else {
            getPermission()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == RESULT_OK && null != data) {
            val selectedImage: Uri? = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor? = contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
            cursor!!.moveToFirst()
            val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
            val picturePath: String = cursor.getString(columnIndex)
            cursor.close()


            val srcMat = Mat()
            Utils.bitmapToMat(BitmapFactory.decodeFile(picturePath), srcMat)
            val gray = Mat()
            Imgproc.cvtColor(srcMat, gray, Imgproc.COLOR_BGR2GRAY)

            Imgproc.GaussianBlur(gray, gray, Size(3.0, 3.0), 5.0, 10.0, BORDER_DEFAULT)
            Imgproc.Canny(gray, gray, 50.0, 200.0)
            val contours: List<MatOfPoint> = ArrayList()
            val hierarchy = Mat()
            Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE)

            if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
                // for each contour, display it in blue
                var idx = 0
                while (idx >= 0) {
                    Imgproc.drawContours(srcMat, contours, idx, Scalar(250.0, 0.0, 0.0 ))
                    idx = hierarchy[0, idx][0].toInt()
                }
            }

            val tempBmp1 = Bitmap.createBitmap(BitmapFactory.decodeFile(picturePath).width,
                    BitmapFactory.decodeFile(picturePath).height,
                    BitmapFactory.decodeFile(picturePath).config)
            Utils.matToBitmap(srcMat, tempBmp1)
            demoImage.setImageBitmap(tempBmp1)


        }
    }
}
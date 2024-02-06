package com.abdallah.ecommerce.utils

import com.abdallah.ecommerce.data.model.ColorModel
import com.abdallah.ecommerce.data.model.SizesModel

class ColorAndSizeConverter {
    fun toSizeList(data: ArrayList<String>): ArrayList<SizesModel>{
        val sizesModelList = ArrayList<SizesModel>()
        data.forEach {
            sizesModelList.add(SizesModel(it, false))
        }
        return sizesModelList
    }
    fun toColorList(data: ArrayList<Int>): ArrayList<ColorModel>{
        val colorModelList = ArrayList<ColorModel>()
        data.forEach {
            colorModelList.add(ColorModel(it, false))
        }
        return colorModelList
    }

}
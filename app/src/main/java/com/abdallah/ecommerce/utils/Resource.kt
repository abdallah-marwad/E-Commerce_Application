package com.abdallah.ecommerce.utils



sealed class Resource<T>(
     val data : T? = null ,
     val message : String ? = null
){
    class UnSpecified<T>(): Resource<T>()
    class Loading<T>(): Resource<T>()
    class Success<T>( data: T?): Resource<T>(data)
    class Failure<T>( message: String?): Resource<T>(message = message)
}

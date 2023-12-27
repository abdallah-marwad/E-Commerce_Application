package com.abdallah.ecommerce.data.firebase

import com.abdallah.ecommerce.data.model.Product

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import javax.inject.Inject

class FirebaseManager @Inject constructor(){

    fun addCollection(
        isInsideDocument : Boolean = false ,
        documentReference : DocumentReference? = null ,
        firestore: FirebaseFirestore ,
        collectionName : String
    ): Task<QuerySnapshot>{

        if(isInsideDocument && documentReference != null){
            return documentReference.collection(collectionName).get()
        }

        return firestore.collection(collectionName).get()
    }
    fun addField(
        isWithoutDocName : Boolean = false ,
        documentReference : DocumentReference? ,
        collectionReference: CollectionReference ? ,
        product : Product
    ): Task<*>? {

        if(isWithoutDocName && collectionReference != null){
            return collectionReference.add(product)
        }
        if(!isWithoutDocName && documentReference != null){
            return documentReference.set(product)
        }

        return null
    }

    fun addDocument (
        collectionReference: CollectionReference,
        documentName : String,
    ): DocumentReference{


        return collectionReference.document(documentName)
    }



    fun getProduct(
        firestore: FirebaseFirestore,
    )=
         firestore.collection("products").whereEqualTo( "hasOffer", true).get()




}
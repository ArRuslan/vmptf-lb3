package com.rdev.nure.vmptflb3.api.entities

import android.os.Parcel
import android.os.Parcelable

data class Article(
    val id: Long,
    val title: String,
    val text: String,
    val created_at: Long,
    val publisher: User,
    val category: Category,
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readParcelable<User>(User::class.java.classLoader)!!,
        parcel.readParcelable<Category>(Category::class.java.classLoader)!!,
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(text)
        parcel.writeLong(created_at)
        parcel.writeParcelable(publisher, flags)
        parcel.writeParcelable(category, flags)
    }

    override fun describeContents(): Int {
        return 0
    }
    companion object CREATOR : Parcelable.Creator<Article> {
        override fun createFromParcel(parcel: Parcel): Article {
            return Article(parcel)
        }

        override fun newArray(size: Int): Array<Article?> {
            return arrayOfNulls(size)
        }
    }
}
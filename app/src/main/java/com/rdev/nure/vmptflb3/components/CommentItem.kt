package com.rdev.nure.vmptflb3.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rdev.nure.vmptflb3.api.entities.Comment
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
private val dateFormat = SimpleDateFormat("dd.MM.yyyy 'at' HH:mm")

@Composable
fun CommentItem(comment: Comment) {
    Column(
        modifier = Modifier.padding(4.dp).fillMaxWidth()
    ) {
        Text(text = comment.user.name, fontSize = 18.sp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = dateFormat.format(comment.created_at * 1000L),
                fontSize = 12.sp,
                color = Color.Gray,
            )
        }
        Text(text = comment.text, fontSize = 14.sp, color = Color.DarkGray)
    }
}
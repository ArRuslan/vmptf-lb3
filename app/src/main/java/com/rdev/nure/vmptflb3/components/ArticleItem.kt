package com.rdev.nure.vmptflb3.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.rdev.nure.vmptflb3.api.entities.Article
import java.text.SimpleDateFormat

@SuppressLint("SimpleDateFormat")
val dateFormat = SimpleDateFormat("dd.MM.yyyy 'at' HH:mm")

@Composable
fun ArticleItem(article: Article) {
    Column {
        Text(text = article.title, fontSize = 18.sp)
        Row(
            //horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = dateFormat.format(article.created_at * 1000L),
                fontSize = 12.sp,
                color = Color.Gray,
            )
            ClickableText(
                text = AnnotatedString(
                    " by ${article.publisher.name}",
                    spanStyles = listOf(
                        AnnotatedString.Range(
                            SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold),
                            4,
                            4 + article.publisher.name.length,
                        ),
                    ),
                ),
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color.Gray,
                ),
                onClick = {}
            )
            ClickableText(
                text = AnnotatedString(
                    " in ${article.category.name}",
                    spanStyles = listOf(
                        AnnotatedString.Range(
                            SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold),
                            4,
                            4 + article.category.name.length,
                        ),
                    ),
                ),
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color.Gray,
                ),
                onClick = {}
            )
        }
        Text(text = article.text, fontSize = 14.sp, color = Color.DarkGray)
    }
}
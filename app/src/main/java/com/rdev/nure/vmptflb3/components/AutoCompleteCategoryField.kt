package com.rdev.nure.vmptflb3.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.window.PopupProperties
import com.rdev.nure.vmptflb3.api.entities.Category
import com.rdev.nure.vmptflb3.api.getApiClient
import com.rdev.nure.vmptflb3.api.services.CategoryService
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val categoriesApi: CategoryService = getApiClient().create(CategoryService::class.java)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteCategoryField(
    modifier: Modifier = Modifier,
    fieldLabel: String,
    onSuggestionSelected: (selectedSuggestion: Category) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf(emptyList<Category>()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(text) {
        debounceJob?.cancel()
        debounceJob = launch {
            val categories = categoriesApi.fetchArticles(text, 1, 10).body()
            if(categories == null) {
                suggestions = emptyList()
                return@launch
            }

            suggestions = categories.result
        }
    }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = isDropdownExpanded,
        onExpandedChange = { expanded ->
            isDropdownExpanded = expanded
        }
    ) {
        TextField(
            label = { Text(fieldLabel) },
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (!it.isFocused) {
                        isDropdownExpanded = false
                    }
                }
                .menuAnchor(),
            onValueChange = {
                text = it
                isDropdownExpanded = it.isNotEmpty()
            },
            readOnly = false,
            value = text
        )

        if (suggestions.isNotEmpty()) {
            DropdownMenu(
                modifier = Modifier.exposedDropdownSize(),
                expanded = isDropdownExpanded,
                onDismissRequest = {
                    isDropdownExpanded = false
                },
                properties = PopupProperties(focusable = false)
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion.name) },
                        onClick = {
                            onSuggestionSelected(suggestion)
                            text = suggestion.name
                            isDropdownExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

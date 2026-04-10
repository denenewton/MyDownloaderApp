package com.example.boilerplate.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.boilerplate.theme.AppleRed

/**
 * A highly refined search bar component.
 * Features Apple Music-inspired design with adjusted border and cursor thickness.
 */
@Composable
fun MusicSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSearchAction: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isFocused) {
        Color.Transparent
    } else {
        if (isDark) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isFocused) AppleRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val borderThickness = if (isFocused) 2.dp else 1.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp) // Aumentado levemente para acomodar a borda mais grossa
            .background(backgroundColor, RoundedCornerShape(10.dp))
            .border(borderThickness, borderColor, RoundedCornerShape(10.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isFocused) AppleRed else Color.Gray.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.width(10.dp))

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Gray.copy(alpha = 0.6f),
                            fontSize = 15.sp
                        )
                    )
                }
                
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    singleLine = true,
                    cursorBrush = SolidColor(AppleRed),
                    // O BasicTextField não tem um parâmetro direto para espessura do cursor,
                    // mas podemos usar um TextStyle personalizado se necessário.
                    // Para o cursor, a cor SolidColor já ajuda na visibilidade.
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = if (onSearchAction != null) ImeAction.Search else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSearchAction?.invoke()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (value.isNotEmpty() && onClear != null) {
                IconButton(
                    onClick = {
                        onClear()
                    },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Gray.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

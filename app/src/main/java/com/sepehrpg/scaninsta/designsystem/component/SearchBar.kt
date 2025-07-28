package com.sepehrpg.scaninsta.designsystem.component

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sepehrpg.scaninsta.R


@Composable
fun AppCustomSearchBarBasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.padding(vertical = 0.dp, horizontal = 0.dp),
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        imeAction = ImeAction.Done
    ),
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: Brush = SolidColor(Color.Black),
    brush: Brush = Brush.horizontalGradient(listOf(Color(0xFFF5F5F5), Color(0xFFF1F1F1))),
    shape: Shape = RoundedCornerShape(16.dp),
    shadowValue: Dp = 0.dp,
    height: Dp = 54.dp,
    clearTextField: () -> Unit = {},
    placeholder: @Composable (() -> Unit)? = {
        Text(
            text = "What are you craving?",
            style = TextStyle(fontSize = 14.sp, color = Color(0xFFBDBDBD), textAlign = TextAlign.Center)
        )
    },
    customDecorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit =
        { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .shadow(elevation = shadowValue, shape)
                    .clip(shape)
                    .background(brush), contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 10.dp, horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(R.drawable.search),
                        contentDescription = "Search Icon",
                        tint = Color(0xFFBDBDBD),
                    )
                    Spacer(Modifier.width(7.dp))
                    Box(modifier = Modifier
                        .weight(1f)
                        .padding(top = 2.dp)) {
                        innerTextField()
                    }
                    if (value.isNotEmpty()){
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Icon",
                            tint = Color(0xFFBDBDBD),
                            modifier = Modifier.clickableWithNoRipple {
                                onValueChange("")
                            }
                        )
                    }
                    else{
                        Row(verticalAlignment = Alignment.CenterVertically){
                            Icon(
                                painter = painterResource(R.drawable.filter),
                                contentDescription = "Close Icon",
                                tint = Color.Gray,
                                modifier = Modifier.clickableWithNoRipple {

                                }.size(22.dp)
                            )
                            Spacer(Modifier.width(15.dp))
                            Icon(
                                painter = painterResource(R.drawable.menu),
                                contentDescription = "Close Icon",
                                tint = Color.Gray,
                                modifier = Modifier.clickableWithNoRipple {

                                }.size(27.dp)
                            )

                        }
                    }
                }
            }
        }
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions  = KeyboardActions(
            onDone = {
                keyboardController?.hide() // Hide the keyboard
                focusManager.clearFocus()
            }
        ),
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        visualTransformation = visualTransformation,
        onTextLayout = onTextLayout,
        interactionSource = interactionSource ?: remember { MutableInteractionSource() },
        cursorBrush = cursorBrush,
        decorationBox = { innerTextField ->
            customDecorationBox {
                Box {
                    if (value.isEmpty() && placeholder != null) {
                        placeholder()
                    }
                    innerTextField()
                }
            }
        }
    )
}
@SuppressLint("UnrememberedMutableInteractionSource")
fun Modifier.clickableWithNoRipple(onClick: () -> Unit): Modifier = composed {
    this.clickable(
        indication = null, // No ripple effect
        interactionSource = MutableInteractionSource(), // Required for handling interactions
        onClick = onClick
    )
}



@Preview()
@Composable
private fun AppTextFieldPreview1() {
    var text by remember { mutableStateOf("") }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 25.dp, horizontal = 10.dp)){

        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp)){
            AppCustomSearchBarBasicTextField(
                value = text,
                onValueChange = { text = it },
            )
        }
    }
}

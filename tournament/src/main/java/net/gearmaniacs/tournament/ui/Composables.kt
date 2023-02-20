package net.gearmaniacs.tournament.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.gearmaniacs.tournament.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableItem(
    title: AnnotatedString,
    subtitle: String,
    description: String,
    onEditAction: () -> Unit,
    onDeleteAction: () -> Unit,
    extraIcon: (@Composable (() -> Unit))? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = { expanded = !expanded }, onLongClick = onEditAction)
            .heightIn(min = 64.dp)
            .padding(16.dp)
    ) {

        Row(Modifier.fillMaxWidth()) {
            Text(
                title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            val rotationAngle by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

            Icon(
                painterResource(R.drawable.ic_expand_more),
                contentDescription = null,
                modifier = Modifier.rotate(rotationAngle)
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth()) {
            Text(subtitle, fontSize = 17.5.sp, modifier = Modifier.weight(1f))

            extraIcon?.invoke()
        }

        AnimatedVisibility(visible = expanded) {
            Column(Modifier.fillMaxWidth()) {
                Text(description, fontSize = 16.sp)

                Spacer(Modifier.height(8.dp))

                Row {
                    Button(
                        onClick = onEditAction,
                        colors = ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Text(stringResource(R.string.action_edit))
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = onDeleteAction,
                        colors = ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Text(stringResource(R.string.action_delete))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberField(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    maxLength: Int,
) {
    val isError = value.isNotEmpty() && value.toIntOrNull() == null

    OutlinedTextField(
        modifier = modifier.onFocusChanged {
            it.hasFocus
        },
        value = value,
        onValueChange = {
            if (it.length <= maxLength) onValueChange(it)
        },
        singleLine = true,
        label = { Text(hint) },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
        ),
        trailingIcon = {
            if (isError) Icon(
                painterResource(R.drawable.ic_error_outline),
                "error",
                tint = MaterialTheme.colorScheme.error
            )
        },
        isError = isError
    )
}

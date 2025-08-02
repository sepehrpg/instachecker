package com.sepehrpg.scaninsta.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sepehrpg.scaninsta.R
import com.sepehrpg.scaninsta.ui.SortOrder


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    sheetState: SheetState,
    currentSortOrder: SortOrder,
    onDismiss: () -> Unit,
    onSortOrderSelected: (SortOrder) -> Unit
) {
    ModalBottomSheet(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = Color.White,
        sheetState = sheetState,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Icon(painter = painterResource(R.drawable.filter), contentDescription = "Sort", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sort By", style = MaterialTheme.typography.titleLarge)
            }

            SortOrderItem(
                text = "Alphabetical (A-Z)",
                isSelected = currentSortOrder == SortOrder.ASC,
                onClick = { onSortOrderSelected(SortOrder.ASC) }
            )
            HorizontalDivider()
            SortOrderItem(
                text = "Alphabetical (Z-A)",
                isSelected = currentSortOrder == SortOrder.DESC,
                onClick = { onSortOrderSelected(SortOrder.DESC) }
            )
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}



@Composable
fun SortOrderItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
package me.ash.reader.domain.model.group

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * TODO: Add class description
 */
@Entity(tableName = "group")
data class Group(
    @PrimaryKey
    var id: String,
    @ColumnInfo
    var name: String,
    @ColumnInfo(index = true)
    var accountId: Int,
    @ColumnInfo(defaultValue = "0")
    var priority: Int = 0
) {
    @Ignore
    var important: Int? = 0
}

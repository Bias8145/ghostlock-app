package com.ghostlock.app.data.route

data class MulticastConfig(
    val geometry: MulticastGeometry,
) : RouteConfig {
    override fun entries(): List<Pair<String, Long>> = listOf(
        "mcast_waiter_off" to geometry.waiterOff,
        "mcast_buffer_size" to geometry.bufferSize.toLong(),
        "mcast_task_offset" to geometry.taskOffset.toLong(),
        "mcast_lock_offset" to geometry.lockOffset.toLong(),
    )

    override fun apply(key: String, value: Long): RouteConfig = when (key) {
        "mcast_waiter_off" -> copy(geometry = geometry.copy(waiterOff = value))
        "mcast_buffer_size" -> copy(geometry = geometry.copy(bufferSize = value.toConfigUInt()))
        "mcast_task_offset" -> copy(geometry = geometry.copy(taskOffset = value.toConfigUInt()))
        "mcast_lock_offset" -> copy(geometry = geometry.copy(lockOffset = value.toConfigUInt()))
        else -> this
    }

    companion object {
        val EMPTY = MulticastConfig(
            geometry = MulticastGeometry(0L, 0u, 0u, 0u),
        )

        fun from(value: (String) -> Long): MulticastConfig = MulticastConfig(
            geometry = MulticastGeometry(
                waiterOff = value("mcast.waiter_off"),
                bufferSize = value("mcast.buffer_size").toConfigUInt(),
                taskOffset = value("mcast.task_offset").toConfigUInt(),
                lockOffset = value("mcast.lock_offset").toConfigUInt(),
            ),
        )
    }
}

data class MulticastGeometry(
    val waiterOff: Long,
    val bufferSize: UInt,
    val taskOffset: UInt,
    val lockOffset: UInt,
)

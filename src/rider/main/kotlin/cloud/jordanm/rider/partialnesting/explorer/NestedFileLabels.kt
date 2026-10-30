package cloud.jordanm.rider.partialnesting.explorer

internal object NestedFileLabels {
    fun forChildren(parentName: String, names: List<String>, existingNames: List<String> = emptyList()): List<String> {
        val stem = parentName.substringBeforeLast('.')
        if (stem.isEmpty()) return names

        val prefix = "$stem."
        val labels = names.map { name ->
            val shortened = name.removePrefix(prefix)
            if (shortened.substringBeforeLast('.', "").isNotEmpty()) shortened else name
        }.toMutableList()

        // Restoring a full name can collide with another shortened label.
        do {
            val collisions = (labels + existingNames).groupingBy { it }.eachCount().filterValues { it > 1 }.keys
            var changed = false
            for (index in labels.indices) {
                if (labels[index] != names[index] && labels[index] in collisions) {
                    labels[index] = names[index]
                    changed = true
                }
            }
        } while (changed)

        return labels
    }
}

package cloud.jordanm.rider.partialnesting.explorer

import kotlin.test.Test
import kotlin.test.assertEquals

class NestedFileLabelsTest {
    @Test
    fun shortensMatchingChildNames() {
        assertEquals(
            listOf("Inventory.cs", "Inventory.Queries.cs"),
            NestedFileLabels.forChildren("BatchRepository.cs", listOf(
                "BatchRepository.Inventory.cs", "BatchRepository.Inventory.Queries.cs",
            )),
        )
    }

    @Test
    fun keepsUnrelatedNamesAndRequiresTheDotSeparator() {
        val names = listOf("Queries.cs", "BatchRepositoryInventory.cs", "Other.BatchRepository.Inventory.cs")
        assertEquals(names, NestedFileLabels.forChildren("BatchRepository.cs", names))
    }

    @Test
    fun matchesCaseExactly() {
        val names = listOf("batchRepository.Inventory.cs", "BATCHREPOSITORY.Inventory.cs")
        assertEquals(names, NestedFileLabels.forChildren("BatchRepository.cs", names))
    }

    @Test
    fun preservesDotsInTheParentStem() {
        assertEquals(
            listOf("Inventory.cs"),
            NestedFileLabels.forChildren("Batch.Repository.cs", listOf("Batch.Repository.Inventory.cs")),
        )
    }

    @Test
    fun doesNotProduceAnEmptyFilename() {
        val names = listOf("BatchRepository.cs", "BatchRepository..cs", "BatchRepository.")
        assertEquals(names, NestedFileLabels.forChildren("BatchRepository.cs", names))
    }

    @Test
    fun doesNotStripAnEmptyParentStem() {
        assertEquals(listOf(".Inventory.cs"), NestedFileLabels.forChildren(".cs", listOf(".Inventory.cs")))
    }

    @Test
    fun restoresFullNamesWhenAShortenedNameCollidesWithAnUnrelatedChild() {
        val names = listOf("BatchRepository.Inventory.cs", "Inventory.cs", "BatchRepository.Read.cs")
        assertEquals(
            listOf("BatchRepository.Inventory.cs", "Inventory.cs", "Read.cs"),
            NestedFileLabels.forChildren("BatchRepository.cs", names),
        )
    }

    @Test
    fun checksExistingNativeChildrenForCollisions() {
        val names = listOf("BatchRepository.Inventory.cs")
        assertEquals(names, NestedFileLabels.forChildren("BatchRepository.cs", names, listOf("Inventory.cs")))
    }

    @Test
    fun resolvesCollisionsIntroducedByRestoringFullNames() {
        val names = listOf("BatchRepository.BatchRepository.Inventory.cs", "BatchRepository.Inventory.cs")
        assertEquals(names, NestedFileLabels.forChildren("BatchRepository.cs", names, listOf("Inventory.cs")))
    }

    @Test
    fun allowsDistinctShortenedNamesEvenWhenOneMatchesAnOriginalName() {
        assertEquals(
            listOf("BatchRepository.Inventory.cs", "Inventory.cs"),
            NestedFileLabels.forChildren("BatchRepository.cs", listOf(
                "BatchRepository.BatchRepository.Inventory.cs", "BatchRepository.Inventory.cs",
            )),
        )
    }

    @Test
    fun leavesExistingDuplicateFullNamesAlone() {
        val names = listOf("BatchRepository.Inventory.cs", "BatchRepository.Inventory.cs")
        assertEquals(names, NestedFileLabels.forChildren("BatchRepository.cs", names))
    }
}

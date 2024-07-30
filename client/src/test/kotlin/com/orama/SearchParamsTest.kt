package com.orama

import com.orama.model.search.SearchParams
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchParamsTest {

    @Test fun builder() {
        val searchParams = SearchParams.builder(
            term = "red shoes",
            mode = SearchParams.Mode.FULLTEXT
        )
        .where(
            listOf(
                SearchParams.Condition("price", SearchParams.ConditionType.LessThan(200.00)),
                SearchParams.Condition("price", SearchParams.ConditionType.GreaterThan(100.00)),
                SearchParams.Condition("price", SearchParams.ConditionType.Between(listOf(100.00, 200.00))),
                SearchParams.Condition("country", SearchParams.ConditionType.In(listOf("Brazil", "Italy")))
            )
        )
        .limit(10)
        .offset(0)
        .returning(listOf("title", "description"))
        .build()

        assertEquals(10, searchParams.limit)
        assertEquals(0, searchParams.offset)
        assertEquals("red shoes", searchParams.term)
        assertEquals(SearchParams.Mode.FULLTEXT, searchParams.mode)
        assertEquals(listOf("title", "description"), searchParams.returning)
    }
}
package com.saintpatrck.logging.troupe

import kotlin.test.AfterTest
import kotlin.test.BeforeTest


class TroupeTest {

    @BeforeTest
    @AfterTest
    fun setUpAndTearDown() {
        Troupe.disbandAll()
    }
}

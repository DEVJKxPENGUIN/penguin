package com.devjk.penguin.helper

import com.devjk.penguin.PenguinAuthTester
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class JwtTest : PenguinAuthTester() {

    @Test
    @DisplayName(
        """
            JWT public key 의 n, e 값이 제대로 생성된다.
        """
    )
    fun jwtPublicKeyTest() {
        val n = jwtUtils.getJwksN()
        val e = jwtUtils.getJwksE()

        assertThat(n).isEqualTo("xan0rMySrm-EXxnt-RgyTuSDHPJd0ZPQR7RS4N7fH-ceBKilHuYoZIHGItN6D2NXQZAvCUU_FCNW1FPMQ1BC2WTSiV35c855G-UOC3O2iI6PCHONq4A1F4D_VGFCuqKgaA9jz7291o-NV7NflxnHgN24fEi4UIqq3cqjXePkg1nGmFYytZIVlxcXJ_B0RaX9UwDn4IsFh8fB8HBL5qeFkn5VBmRLHSm-UzTL1swgNsEtIjr5QkysPw4qI-9wCZHU5XzJ-6SqmhchsuoDHTAWu8uSGJPHdFV_LrBlH50QZamojyZHpFJx8JtbDv_YZldtcVQjM1sfCAYVuohIc6inNQ")
        assertThat(e).isEqualTo("AQAB")
    }

}
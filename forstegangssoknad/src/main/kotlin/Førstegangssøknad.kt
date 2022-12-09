package no.nav.helse

import java.time.LocalDateTime

class Førstegangssøknad {


    fun handle(søknad: Søknad): Boolean {
        return true
    }
}

data class Søknad(val fom: LocalDateTime, val tom: LocalDateTime, val arbeidGjenopptatt: LocalDateTime)
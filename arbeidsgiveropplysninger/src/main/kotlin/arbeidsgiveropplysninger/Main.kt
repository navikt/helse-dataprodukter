package no.nav.helse.arbeidsgiveropplysninger

import arbeidsgiveropplysninger.arbeidsgiveropplysningerkorrigert.ArbeidsgiveropplysningerKorrigertDao
import arbeidsgiveropplysninger.arbeidsgiveropplysningerkorrigert.ArbeidsgiveropplysningerKorrigertRiver
import arbeidsgiveropplysninger.inntektsmeldingaktivitet.InntektsmeldingAktivitetDao
import arbeidsgiveropplysninger.inntektsmeldingaktivitet.InntektsmeldingAktiviteterRiver
import arbeidsgiveropplysninger.inntektsmeldingaktivitet.SykepengegrunnlagAvvikAktivitetRiver
import arbeidsgiveropplysninger.inntektsmeldinghåndtert.InntektsmeldingHåndtertDao
import arbeidsgiveropplysninger.inntektsmeldinghåndtert.InntektsmeldingHåndtertRiver
import arbeidsgiveropplysninger.inntektsmeldingregistrert.InntektsmeldingRegistrertDao
import arbeidsgiveropplysninger.inntektsmeldingregistrert.InntektsmeldingRegistrertRiver
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import no.nav.helse.datasource
import no.nav.helse.migrate
import no.nav.helse.rapids_rivers.RapidApplication

fun main() {
    val env = System.getenv()
    val datasource = datasource(
        env["DATABASE_USERNAME"] ?: throw IllegalArgumentException("Missing envvar"),
        env["DATABASE_PASSWORD"] ?: throw IllegalArgumentException("Missing envvar"),
        String.format(
            "jdbc:postgresql://%s:%s/%s",
            requireNotNull(env["DATABASE_HOST"]) { "database host must be set" },
            requireNotNull(env["DATABASE_PORT"]) { "database port must be set" },
            requireNotNull(env["DATABASE_DATABASE"]) { "database name must be set" })
    )

    val inntektsmeldingHåndtertDao = InntektsmeldingHåndtertDao(datasource)
    val inntektsmeldingAktivitetDao = InntektsmeldingAktivitetDao(datasource)
    val inntektsmeldingRegistrertDao = InntektsmeldingRegistrertDao(datasource)
    val arbeidsgiveropplysningerKorrigertDao = ArbeidsgiveropplysningerKorrigertDao(datasource)

    RapidApplication.create(env).apply {
        InntektsmeldingHåndtertRiver(this, inntektsmeldingHåndtertDao)
        InntektsmeldingAktiviteterRiver(this, inntektsmeldingAktivitetDao)
        InntektsmeldingRegistrertRiver(this, inntektsmeldingRegistrertDao)
        SykepengegrunnlagAvvikAktivitetRiver(this, inntektsmeldingAktivitetDao, inntektsmeldingHåndtertDao)
        ArbeidsgiveropplysningerKorrigertRiver(this, arbeidsgiveropplysningerKorrigertDao)
    }.apply {
        register(object : RapidsConnection.StatusListener {
            override fun onStartup(rapidsConnection: RapidsConnection) {
                migrate(datasource)
            }
        })
    }.start()
}

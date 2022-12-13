import TestDatasource.migratedDb
import no.nav.helse.FørstegangsbehandlingDao
import no.nav.helse.SøkandMediator
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.rapids_rivers.toUUID
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class E2ETest {

    private val db = migratedDb
    private val dao = FørstegangsbehandlingDao(db)
    private val rapid = TestRapid()


    @BeforeEach
    fun reset() = resetDatabase()

    @Test
    fun `Person ender opp i databasen`() {
        val mediator = SøkandMediator(rapid, dao)
        rapid.sendTestMessage(testSøknad)
        val result = dao.refFor("27845899830", "805824352")
        assertEquals(1L, result)
    }
    @Test
    fun `Førstegangsbehandling ender i databasen`() {
        val mediator = SøkandMediator(rapid, dao)
        rapid.sendTestMessage(testSøknad)
        val ref = dao.refFor("27845899830", "805824352")
        val søknader = dao.hentSøknader(ref)
        assertEquals("f93baf8c-3782-4dcb-9704-bfeb44e44e74".toUUID(), søknader.first().id)
    }

}

@Language("JSON")
val testSøknad = """
    {
      "id": "8d7d8de1-5d4e-3750-81d4-8d0adfecbc2a",
      "type": "ARBEIDSTAKERE",
      "status": "SENDT",
      "fnr": "27845899830",
      "sykmeldingId": "b14ff260-ace6-4625-8fa7-860a6f54c706",
      "arbeidsgiver": {
        "navn": "Vegansk Slakteri",
        "orgnummer": "805824352"
      },
      "arbeidssituasjon": "ARBEIDSTAKER",
      "korrigerer": null,
      "korrigertAv": null,
      "soktUtenlandsopphold": null,
      "arbeidsgiverForskutterer": null,
      "fom": "2022-10-01",
      "tom": "2022-10-31",
      "dodsdato": null,
      "startSyketilfelle": "2022-08-01",
      "arbeidGjenopptatt": null,
      "sykmeldingSkrevet": "2022-10-01T02:00:00",
      "opprettet": "2022-12-09T14:55:17.106944",
      "opprinneligSendt": null,
      "sendtNav": "2022-12-09T14:56:50.945681886",
      "sendtArbeidsgiver": "2022-12-09T14:56:50.945681886",
      "egenmeldinger": [],
      "fravarForSykmeldingen": [],
      "papirsykmeldinger": [],
      "fravar": [],
      "andreInntektskilder": [],
      "soknadsperioder": [
        {
          "fom": "2022-10-01",
          "tom": "2022-10-31",
          "sykmeldingsgrad": 80,
          "faktiskGrad": null,
          "avtaltTimer": null,
          "faktiskTimer": null,
          "sykmeldingstype": "GRADERT",
          "grad": 80
        }
      ],
      "sporsmal": [],
      "avsendertype": "BRUKER",
      "ettersending": false,
      "mottaker": "ARBEIDSGIVER_OG_NAV",
      "egenmeldtSykmelding": false,
      "arbeidUtenforNorge": null,
      "harRedusertVenteperiode": null,
      "behandlingsdager": null,
      "permitteringer": [],
      "merknaderFraSykmelding": null,
      "merknader": null,
      "sendTilGosys": null,
      "@id": "f93baf8c-3782-4dcb-9704-bfeb44e44e74",
      "@opprettet": "2022-12-09T14:56:50.945681886",
      "@event_name": "sendt_søknad_nav"
    }
""".trimIndent()
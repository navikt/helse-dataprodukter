plugins {
    id("no.nav.sykepenger.deployable")
}

sykepengerDeployable {
    mainClass = "no.nav.helse.MainKt"
    imageName = "${rootProject.name}-forstegangsbehandling"
}

dependencies {
    implementation(libs.rapidsAndRivers)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.kotliquery)
    implementation(libs.hikariCP)
    implementation(libs.postgresql)

    testImplementation(libs.tbdLibs.rapidsAndRiversTest)
    testImplementation(libs.tbdLibs.postgresTestdatabaser)
}
